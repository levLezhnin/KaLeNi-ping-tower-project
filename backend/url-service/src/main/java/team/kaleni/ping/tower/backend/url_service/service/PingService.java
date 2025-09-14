package team.kaleni.ping.tower.backend.url_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class PingService {

    private final WebClient webClient;
    private final URLNormalizer urlNormalizer;

    @Value("${ping.timeout:5000}")
    private int timeoutMs;

    @Value("${ping.retry.attempts:3}")
    private int retryAttempts;

    @Value("${ping.retry.delay:1000}")
    private long retryDelayMs;

    public PingService(URLNormalizer urlNormalizer) {
        this.urlNormalizer = urlNormalizer;
        // Configure connection pool
        ConnectionProvider connectionProvider = ConnectionProvider.builder("ping-pool")
                .maxConnections(50)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .build();
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofMillis(timeoutMs))
                .followRedirect(true);
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * Checks URL availability with improved error handling and retry mechanism
     * @param url string with URL to check
     * @return true if ping is successful (response code 200-399), false otherwise
     */
    public boolean pingURL(String url) {
        if (!isValidInput(url)) {
            return false;
        }
        try {
            String normalizedUrl = urlNormalizer.normalize(url);
            log.debug("Pinging normalized URL: {}", normalizedUrl);
            Boolean result = webClient.get()
                    .uri(normalizedUrl)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofMillis(timeoutMs))
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofMillis(retryDelayMs))
                            .filter(this::isRetryableException))
                    .map(response -> {
                        HttpStatus status = (HttpStatus) response.getStatusCode();
                        boolean success = status.is2xxSuccessful() || status.is3xxRedirection();
                        logPingResult(normalizedUrl, status.value(), status.getReasonPhrase(), success);
                        return success;
                    })
                    .onErrorComplete(a -> {
                        handlePingError(normalizedUrl, a);
                        return false;
                    })
                    .block();
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Unexpected error while pinging URL: {} - {}", url, e.getMessage(), e);
            return false;
        }
    }

    private boolean isValidInput(String url) {
        if (url == null || url.trim().isEmpty()) {
            log.warn("URL is null or empty");
            return false;
        }
        return true;
    }

    private boolean isRetryableException(Throwable throwable) {
        return throwable instanceof TimeoutException ||
                throwable instanceof java.net.ConnectException ||
                (throwable instanceof WebClientResponseException &&
                        ((WebClientResponseException) throwable).getStatusCode().is5xxServerError());
    }

    private void handlePingError(String url, Object throwab) {
        Throwable throwable = (Throwable) throwab;
        if (throwab instanceof WebClientResponseException) {
            WebClientResponseException webEx = (WebClientResponseException) throwable;
            HttpStatus status = (HttpStatus) webEx.getStatusCode();
            if (status.is4xxClientError()) {
                log.warn("Client error for URL: {} - Response: {} {}",
                        url, status.value(), status.getReasonPhrase());
            } else if (status.is5xxServerError()) {
                log.warn("Server error for URL: {} - Response: {} {}",
                        url, status.value(), status.getReasonPhrase());
            }
        } else if (throwable instanceof TimeoutException) {
            log.warn("Timeout exceeded for URL: {}", url);
        } else if (throwable instanceof java.net.ConnectException) {
            log.warn("Failed to connect to resource: {} - {}", url, throwable.getMessage());
        } else if (throwable instanceof java.net.UnknownHostException) {
            log.warn("Unknown host: {} - {}", url, throwable.getMessage());
        } else {
            log.warn("Network error for URL: {} - {}", url, throwable.getMessage());
        }
    }

    private void logPingResult(String url, int responseCode, String responseMessage, boolean success) {
        if (success) {
            log.debug("Ping successful for URL: {} - Response: {} {}", url, responseCode, responseMessage);
        } else {
            log.warn("Ping failed for URL: {} - Response: {} {}", url, responseCode, responseMessage);
        }
    }
}
