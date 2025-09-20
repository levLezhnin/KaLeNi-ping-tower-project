package team.kaleni.ping.tower.backend.url_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

@Component
@Slf4j
public class URLNormalizer {

    private static final Pattern PROTOCOL_PATTERN = Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);
    private static final String DEFAULT_SCHEME = "https://";

    /**
     * Normalizes URL while preserving case-sensitive components
     * @param url the URL to normalize
     * @return normalized URL or original URL if normalization fails
     */
    public String normalize(String url) {
        if (url == null || url.trim().isEmpty()) {
            log.warn("Cannot normalize null or empty URL");
            return url;
        }
        try {
            String trimmed = url.trim();
            // Add protocol if missing
            if (!PROTOCOL_PATTERN.matcher(trimmed).find()) {
                trimmed = DEFAULT_SCHEME + trimmed;
                log.debug("Added default scheme to URL: {}", trimmed);
            }
            URI uri = new URI(trimmed);
            // Normalize scheme to lowercase
            String scheme = uri.getScheme().toLowerCase();

            // Normalize host to lowercase (hosts are case-insensitive)
            String host = uri.getHost();
            if (host != null) {
                host = host.toLowerCase();
                // Only remove www if it's safe to do so
                if (shouldRemoveWww(host)) {
                    host = host.substring(4);
                    log.debug("Removed www from host: {}", host);
                }
            }
            // Normalize path (remove trailing slash only for root path)
            String path = uri.getPath();
            if (path != null && path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            // Preserve query parameters and fragment as-is (they can be case-sensitive)
            URI normalizedUri = new URI(
                    scheme,
                    uri.getUserInfo(),
                    host,
                    uri.getPort(),
                    path,
                    uri.getQuery(),
                    uri.getFragment()
            );
            String result = normalizedUri.toString();
            log.debug("URL normalized from '{}' to '{}'", url, result);
            return result;
        } catch (URISyntaxException e) {
            log.warn("Failed to normalize URL '{}': {}", url, e.getMessage());
            // Try basic cleanup for malformed URLs
            return basicCleanup(url);
        }
    }

    private boolean shouldRemoveWww(String host) {
        // Don't remove www from domains that are known to require it
        // This is a simplified check - you might want to expand this based on your needs
        return host.startsWith("www.") &&
                !host.equals("www.com") &&
                !host.equals("www.org");
    }

    private String basicCleanup(String url) {
        String cleaned = url.trim();
        // Add protocol if clearly missing
        if (!cleaned.contains("://")) {
            cleaned = DEFAULT_SCHEME + cleaned;
        }
        return cleaned;
    }

    /**
     * Validates if a URL has a valid structure
     * @param url the URL to validate
     * @return true if URL structure is valid
     */
    public boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(normalize(url));
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
