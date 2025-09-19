package team.kaleni.ping.tower.backend.url_service.service;

import io.netty.channel.ChannelOption;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;
import team.kaleni.ping.tower.backend.url_service.dto.inner.PingResultDTO;
import team.kaleni.ping.tower.backend.url_service.entity.HttpMethod;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;
import team.kaleni.ping.tower.backend.url_service.entity.PingStatus;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class EnhancedPingService {

    private WebClient webClient;

    @Value("${ping.retry.attempts:2}")
    private int retryAttempts;

    @Value("${ping.retry.delay:500}")
    private long retryDelayMs;

    @Value("${ping.max-header-size:32768}")
    private int maxHeaderSize;

    @PostConstruct
    public void initializeWebClient() {
        // Optimized connection pool for high-throughput pinging
        ConnectionProvider connectionProvider = ConnectionProvider.builder("ping-pool")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .build();
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .followRedirect(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(30))
                .compress(true)
                .httpResponseDecoder(spec -> spec.maxHeaderSize(maxHeaderSize));
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * Enhanced ping method that supports all HTTP methods and configurations from Monitor
     */
    public PingResultDTO pingMonitor(Monitor monitor) {
        if (monitor.getUrl() == null || monitor.getUrl().trim().isEmpty()) {
            return PingResultDTO.builder()
                    .status(PingStatus.ERROR)
                    .errorMessage("URL is null or empty")
                    .responseTimeMs(0)
                    .build();
        }
        long startTime = System.currentTimeMillis();
        try {
            WebClient.RequestHeadersSpec<?> requestSpec = buildRequest(monitor);
            return requestSpec
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofMillis(monitor.getTimeoutMs()))
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofMillis(retryDelayMs))
                            .filter(this::isRetriableException))
                    .map(response -> {
                        long responseTime = System.currentTimeMillis() - startTime;
                        HttpStatus status = (HttpStatus) response.getStatusCode();

                        Map<String, Object> metadata = new HashMap<>();
                        // Capture response headers (but keep them minimal)
                        response.getHeaders().forEach((key, values) -> {
                            if ("content-type".equalsIgnoreCase(key) ||
                                    "server".equalsIgnoreCase(key) ||
                                    "date".equalsIgnoreCase(key)) {
                                metadata.put(key, values.size() == 1 ? values.get(0) : values);
                            }
                        });
                        PingStatus pingStatus = determinePingStatus(status.value());
                        return PingResultDTO.builder()
                                .status(pingStatus)
                                .responseCode(status.value())
                                .responseTimeMs((int) responseTime)
                                .metadata(metadata)
                                .fromCache(false)
                                .build();
                    })
                    .onErrorResume(throwable -> {
                        long responseTime = System.currentTimeMillis() - startTime;
                        PingResultDTO errorResult = createErrorResult(throwable, (int) responseTime);
                        return Mono.just(errorResult);
                    })
                    .block();

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Unexpected error pinging monitor {}: {}", monitor.getId(), e.getMessage());
            return createErrorResult(e, (int) responseTime);
        }
    }

    /**
     * Build request with full HTTP method, headers, and body support
     */
    private WebClient.RequestHeadersSpec<?> buildRequest(Monitor monitor) {
        String url = monitor.getUrl();
        HttpMethod method = monitor.getMethod();
        Map<String, String> headers = monitor.getHeaders();
        String requestBody = monitor.getRequestBody();
        String contentType = monitor.getContentType();

        // Build the base request spec based on HTTP method
        WebClient.RequestHeadersSpec<?> headersSpec;

        switch (method) {
            case GET -> headersSpec = webClient.get().uri(url);
            case POST -> {
                WebClient.RequestBodyUriSpec postSpec = webClient.post();
                headersSpec = postSpec.uri(url);

                // Handle POST body
                if (requestBody != null && !requestBody.trim().isEmpty()) {
                    String mediaType = contentType != null ? contentType : "application/json";
                    return postSpec.uri(url)
                            .contentType(MediaType.parseMediaType(mediaType))
                            .bodyValue(requestBody);
                }
            }
            case HEAD -> headersSpec = webClient.head().uri(url);
            default -> headersSpec = webClient.get().uri(url); // fallback to GET
        }

        // Add custom headers
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                headersSpec = headersSpec.header(header.getKey(), header.getValue());
            }
        }

        return headersSpec;
    }

    private PingStatus determinePingStatus(int responseCode) {
        if (responseCode >= 200 && responseCode <= 399) {
            return PingStatus.UP;
        } else if (responseCode >= 400 && responseCode <= 499) {
            return PingStatus.DOWN; // Client error still means service is responding
        } else {
            return PingStatus.DOWN; // Server error
        }
    }

    private boolean isRetriableException(Throwable throwable) {
        return throwable instanceof TimeoutException ||
                throwable instanceof ConnectException ||
                (throwable instanceof WebClientResponseException &&
                        ((WebClientResponseException) throwable).getStatusCode().is5xxServerError());
    }

    private PingResultDTO createErrorResult(Throwable throwable, int responseTime) {
        PingStatus status = PingStatus.ERROR;
        String errorMessage = throwable.getMessage();

        switch (throwable) {
            case TimeoutException timeoutException -> {
                status = PingStatus.TIMEOUT;
                errorMessage = "Request timeout";
            }
            case ConnectException connectException -> {
                status = PingStatus.DOWN;
                errorMessage = "Connection refused: " + throwable.getMessage();
            }
            case UnknownHostException unknownHostException -> {
                status = PingStatus.DOWN;
                errorMessage = "Unknown host: " + throwable.getMessage();
            }
            case WebClientResponseException webEx -> {
                return PingResultDTO.builder()
                        .status(determinePingStatus(webEx.getStatusCode().value()))
                        .responseCode(webEx.getStatusCode().value())
                        .responseTimeMs(responseTime)
                        .errorMessage(webEx.getMessage())
                        .build();
            }
            default -> {
                // Handle other exceptions
            }
        }

        return PingResultDTO.builder()
                .status(status)
                .responseTimeMs(responseTime)
                .errorMessage(errorMessage)
                .build();
    }
}
