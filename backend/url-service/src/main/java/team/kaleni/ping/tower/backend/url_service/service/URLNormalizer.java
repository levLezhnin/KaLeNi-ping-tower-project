package team.kaleni.ping.tower.backend.url_service.service;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;


@Component
public class URLNormalizer {
    public static String normalize(String url) {
        try {
            URI uri = new URI(url.trim().toLowerCase());

            if (uri.getScheme() == null) {
                uri = new URI("https://" + url.trim().toLowerCase());
            }

            String host = uri.getHost();
            if (host != null && host.startsWith("www.")) {
                host = host.substring(4);
            }

            String path = uri.getPath();
            if (path != null && path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            return new URI(uri.getScheme(), null, host, uri.getPort(), path, uri.getQuery(), null).toString();

        } catch (URISyntaxException e) {
            return url.trim().toLowerCase();
        }
    }
}

