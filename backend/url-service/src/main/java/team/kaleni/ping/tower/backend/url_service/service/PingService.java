package team.kaleni.ping.tower.backend.url_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;

@Service
@Slf4j
public class PingService {

    @Value("${ping.timeout:5000}")
    private int timeout; // читается из application.yml, по умолчанию 5 секунд

    /**
     * Проверяет доступность URL
     * @param url строка с URL для проверки
     * @return true если пинг успешен (код ответа 200-399), false если URL невалидный или недоступен
     */
    public boolean pingURL(String url) {
        if (url == null || url.trim().isEmpty()) {
            log.warn("URL is null or empty");
            return false;
        }
        boolean isPingSucceeded = false;
        try {
            String normalizedUrl = URLNormalizer.normalize(url);
            HttpURLConnection connection = (HttpURLConnection) new URI(normalizedUrl).toURL().openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            isPingSucceeded = (responseCode >= 200 && responseCode <= 399);
            logPingResult(url, responseCode, connection.getResponseMessage(), isPingSucceeded);
            connection.disconnect();
        } catch (URISyntaxException | MalformedURLException e) {
            log.warn("Invalid URL format: {} - {}", url, e.getMessage());
        } catch (ConnectException e) {
            log.warn("Failed to connect to resource: {} - {}", url, e.getMessage());
        } catch (SocketTimeoutException e) {
            log.warn("Timeout exceeded for URL: {} - {}", url, e.getMessage());
        } catch (UnknownHostException e) {
            log.warn("Unknown host: {} - {}", url, e.getMessage());
        } catch (IOException e) {
            log.warn("IO error for URL: {} - {}", url, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while pinging URL: {} - {}", url, e.getMessage(), e);
        }
        return isPingSucceeded;
    }

    private void logPingResult(String url, int responseCode, String responseMessage, boolean success) {
        if (success) {
            log.debug("Ping successful for URL: {} - Response: {} {}", url, responseCode, responseMessage);
        } else {
            if (responseCode >= 400 && responseCode <= 499) {
                log.warn("Client error for URL: {} - Response: {} {}", url, responseCode, responseMessage);
            } else if (responseCode >= 500 && responseCode <= 599) {
                log.warn("Server error for URL: {} - Response: {} {}", url, responseCode, responseMessage);
            } else {
                log.warn("Unexpected response for URL: {} - Response: {} {}", url, responseCode, responseMessage);
            }
        }
    }
}
