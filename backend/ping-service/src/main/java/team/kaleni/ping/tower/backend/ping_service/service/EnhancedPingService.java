package team.kaleni.ping.tower.backend.ping_service.service;

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
import team.kaleni.ping.tower.backend.ping_service.dto.MonitorConfigDto;
import team.kaleni.ping.tower.backend.ping_service.dto.PingResultDto;
import team.kaleni.ping.tower.backend.ping_service.enums.HttpMethod;
import team.kaleni.ping.tower.backend.ping_service.enums.PingStatus;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
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

    @PostConstruct
    public void initializeWebClient() {
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
                .compress(true);

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        log.info("Enhanced ping service initialized with connection pool");
    }

    /**
     * 🔥 Главный метод для пинга монитора
     */
    public PingResultDto pingMonitor(MonitorConfigDto config) {
        if (config.getUrl() == null || config.getUrl().trim().isEmpty()) {
            return PingResultDto.builder()
                    .monitorId(config.getMonitorId())
                    .status(PingStatus.ERROR)
                    .errorMessage("URL is null or empty")
                    .responseTimeMs(0)
                    .timestamp(Instant.now())
                    .url(config.getUrl())
                    .build();
        }

        long startTime = System.currentTimeMillis();
        try {
            WebClient.RequestHeadersSpec<?> requestSpec = buildRequest(config);

            return requestSpec
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofMillis(config.getTimeoutMs()))
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofMillis(retryDelayMs))
                            .filter(this::isRetriableException))
                    .map(response -> {
                        long responseTime = System.currentTimeMillis() - startTime;
                        HttpStatus status = (HttpStatus) response.getStatusCode();

                        Map<String, Object> metadata = new HashMap<>();
                        response.getHeaders().forEach((key, values) -> {
                            if ("content-type".equalsIgnoreCase(key) ||
                                    "server".equalsIgnoreCase(key)) {
                                metadata.put(key, values.size() == 1 ? values.get(0) : values);
                            }
                        });

                        PingStatus pingStatus = determinePingStatus(status.value());

                        return PingResultDto.builder()
                                .monitorId(config.getMonitorId())
                                .status(pingStatus)
                                .responseCode(status.value())
                                .responseTimeMs((int) responseTime)
                                .metadata(metadata)
                                .fromCache(false)
                                .timestamp(Instant.now())
                                .url(config.getUrl())
                                .build();
                    })
                    .onErrorResume(throwable -> {
                        long responseTime = System.currentTimeMillis() - startTime;
                        PingResultDto errorResult = createErrorResult(config, throwable, (int) responseTime);
                        return Mono.just(errorResult);
                    })
                    .block();

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Unexpected error pinging monitor {}: {}", config.getMonitorId(), e.getMessage());
            return createErrorResult(config, e, (int) responseTime);
        }
    }

    private WebClient.RequestHeadersSpec<?> buildRequest(MonitorConfigDto config) {
        String url = config.getUrl();
        HttpMethod method = config.getMethod() != null ? config.getMethod() : HttpMethod.GET;
        Map<String, String> headers = config.getHeaders();
        String requestBody = config.getRequestBody();
        String contentType = config.getContentType();

        WebClient.RequestHeadersSpec<?> headersSpec;

        switch (method) {
            case GET -> headersSpec = webClient.get().uri(url);
            case POST -> {
                WebClient.RequestBodyUriSpec postSpec = webClient.post();
                if (requestBody != null && !requestBody.trim().isEmpty()) {
                    String mediaType = contentType != null ? contentType : "application/json";
                    headersSpec = postSpec.uri(url)
                            .contentType(MediaType.parseMediaType(mediaType))
                            .bodyValue(requestBody);
                } else {
                    headersSpec = postSpec.uri(url);
                }
            }
            case HEAD -> headersSpec = webClient.head().uri(url);
            default -> headersSpec = webClient.get().uri(url);
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
            return PingStatus.DOWN;
        } else {
            return PingStatus.DOWN;
        }
    }

    private boolean isRetriableException(Throwable throwable) {
        return throwable instanceof TimeoutException ||
                throwable instanceof ConnectException ||
                (throwable instanceof WebClientResponseException &&
                        ((WebClientResponseException) throwable).getStatusCode().is5xxServerError());
    }

    private PingResultDto createErrorResult(MonitorConfigDto config, Throwable throwable, int responseTime) {
        PingStatus status = PingStatus.ERROR;
        String errorMessage = throwable.getMessage();
        int code = 500;

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
                return PingResultDto.builder()
                        .monitorId(config.getMonitorId())
                        .status(determinePingStatus(webEx.getStatusCode().value()))
                        .responseCode(webEx.getStatusCode().value())
                        .responseTimeMs(responseTime)
                        .errorMessage(webEx.getMessage())
                        .timestamp(Instant.now())
                        .url(config.getUrl())
                        .build();
            }
            default -> { /* Handle other exceptions */ }
        }

        return PingResultDto.builder()
                .monitorId(config.getMonitorId())
                .status(status)
                .responseCode(code)
                .responseTimeMs(responseTime)
                .errorMessage(errorMessage)
                .timestamp(Instant.now())
                .url(config.getUrl())
                .build();
    }
}
