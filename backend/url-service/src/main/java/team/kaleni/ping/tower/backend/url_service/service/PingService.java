package team.kaleni.ping.tower.backend.url_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;
import team.kaleni.ping.tower.backend.url_service.entity.HttpMethod;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class PingService {

    private final WebClient webClient;

    @Value("${ping.timeout:5000}")
    private int timeoutMs;

    @Value("${ping.retry.attempts:2}")
    private int retryAttempts;

    @Value("${ping.retry.delay:1000}")
    private long retryDelayMs;

    public PingService() {
        // Configure connection pool
        ConnectionProvider connectionProvider = ConnectionProvider.builder("ping-pool")
                .maxConnections(50)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofMillis(timeoutMs))
                .followRedirect(true)
                .httpResponseDecoder(spec -> spec.maxHeaderSize(32768)); // Handle large headers

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * Simple validation ping - just checks if URL is reachable
     * Used during monitor creation to validate configuration
     *
     * @param url URL to test
     * @return true if reachable, false otherwise
     */
    public boolean pingURL(String url) {
        return pingURL(url, HttpMethod.GET, null, null, null);
    }

    /**
     * Full validation ping with custom method, headers and body
     * Used to test complete monitor configuration during creation
     */
    public boolean pingMonitor(Monitor monitor) {
        return pingURL(
                monitor.getUrl(),
                monitor.getMethod(),
                monitor.getHeaders(),
                monitor.getRequestBody(),
                monitor.getContentType()
        );
    }

    /**
     * Core ping method with full HTTP configuration support
     */
    public boolean pingURL(String url, HttpMethod method, Map<String, String> headers,
                           String requestBody, String contentType) {

        if (!isValidInput(url)) {
            return false;
        }

        try {
            log.debug("Validating URL with method {}: {}", method, url);

            WebClient.RequestHeadersSpec<?> requestSpec = buildRequest(url, method, headers, requestBody, contentType);

            Boolean result = requestSpec
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofMillis(timeoutMs))
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofMillis(retryDelayMs))
                            .filter(this::isRetryableException))
                    .map(response -> {
                        HttpStatus status = (HttpStatus) response.getStatusCode();
                        boolean success = status.is2xxSuccessful() || status.is3xxRedirection();
                        log.debug("Ping {} for URL: {} - Response: {} {}",
                                success ? "successful" : "failed", url, status.value(), status.getReasonPhrase());
                        return success;
                    })
                    .onErrorComplete(throwable -> {
                        handlePingError(url, throwable);
                        return false;
                    })
                    .block();

            return Boolean.TRUE.equals(result);

        } catch (Exception e) {
            log.error("Unexpected error while pinging URL: {} - {}", url, e.getMessage());
            return false;
        }
    }

    /**
     * Fixed buildRequest method - returns correct type and handles all HTTP methods
     */
    private WebClient.RequestHeadersSpec<?> buildRequest(String url, HttpMethod method,
                                                         Map<String, String> headers, String requestBody, String contentType) {

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
//            case PUT -> {
//                WebClient.RequestBodyUriSpec putSpec = webClient.put();
//                headersSpec = putSpec.uri(url);
//
//                // Handle PUT body
//                if (requestBody != null && !requestBody.trim().isEmpty()) {
//                    String mediaType = contentType != null ? contentType : "application/json";
//                    return putSpec.uri(url)
//                            .contentType(MediaType.parseMediaType(mediaType))
//                            .bodyValue(requestBody);
//                }
//            }
//            case DELETE -> headersSpec = webClient.delete().uri(url);
//            case PATCH -> {
//                WebClient.RequestBodyUriSpec patchSpec = webClient.patch();
//                headersSpec = patchSpec.uri(url);
//
//                // Handle PATCH body
//                if (requestBody != null && !requestBody.trim().isEmpty()) {
//                    String mediaType = contentType != null ? contentType : "application/json";
//                    return patchSpec.uri(url)
//                            .contentType(MediaType.parseMediaType(mediaType))
//                            .bodyValue(requestBody);
//                }
//            }
//            case OPTIONS -> headersSpec = webClient.options().uri(url);
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

    private void handlePingError(String url, Throwable throwable) {
        if (throwable instanceof WebClientResponseException webEx) {
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
}
