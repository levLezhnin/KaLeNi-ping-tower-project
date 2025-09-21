package team.kaleni.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class RestClient {

    private final RestTemplate restTemplate;

    public ResponseEntity<String> callMicroservice(String path, Long userId, Long body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Long> request = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
                path,
                HttpMethod.PUT,
                request,
                String.class,
                userId
        );
    }

}
