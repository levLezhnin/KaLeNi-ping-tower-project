package team.kaleni.ping.tower.backend.url_service.service;


import io.netty.channel.ChannelOption;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;
import team.kaleni.ping.tower.backend.url_service.dto.inner.PingResultDTO;
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

    @Value("${ping.max-header-size:32768}") // Add this configuration
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
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // Connection timeout
                .responseTimeout(Duration.ofSeconds(30)) // HTTP response timeout
                .compress(true)
                .httpResponseDecoder(spec -> spec.maxHeaderSize(maxHeaderSize)); // Now maxHeaderSize = 32768

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * Performs detailed HTTP ping with comprehensive result capture
     */
    public PingResultDTO pingURL(String url, int timeoutMs) {
        if (url == null || url.trim().isEmpty()) {
            return PingResultDTO.builder()
                    .status(PingStatus.ERROR)
                    .errorMessage("URL is null or empty")
                    .responseTimeMs(0)
                    .build();
        }
        long startTime = System.currentTimeMillis();
        try {
            return webClient.head()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofMillis(timeoutMs))
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofMillis(retryDelayMs))
                            .filter(this::isRetryableException))
                    .map(response -> {
                        long responseTime = System.currentTimeMillis() - startTime;
                        HttpStatus status = (HttpStatus) response.getStatusCode();
                        Map<String, Object> metadata = new HashMap<>();
                        // Safely capture response headers (limit size to prevent memory issues)
                        response.getHeaders().forEach((key, values) -> {
                            if (values.size() == 1) {
                                metadata.put(key, values.getFirst());
                            } else {
                                metadata.put(key, values);
                            }
                        });
                        PingStatus pingStatus = determinePingStatus(status.value());
                        metadata.clear(); //todo check out this, by default it takes sooo much space...
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
            log.error("Unexpected error pinging URL {}: {}", url, e.getMessage());
            return createErrorResult(e, (int) responseTime);
        }
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

    private boolean isRetryableException(Throwable throwable) {
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
            }
        }
        return PingResultDTO.builder()
                .status(status)
                .responseTimeMs(responseTime)
                .errorMessage(errorMessage)
                .build();
    }
}
