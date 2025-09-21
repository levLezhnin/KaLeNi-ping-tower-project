package team.kaleni.notificationservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import team.kaleni.notificationservice.dto.NotificationDTO;
import team.kaleni.notificationservice.sender.EmailNotificationSender;
import team.kaleni.notificationservice.sender.TelegramNotificationSender;
import team.kaleni.notificationservice.service.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final EmailNotificationSender emailNotificationSender;
    private final TelegramNotificationSender telegramNotificationSender;
    private final RestClient restClient;

    @Value("{spring.application.get-user-path}")
    private String getUserPath;

    @KafkaListener(topics = "${spring.kafka.notification-topic}", groupId = "${spring.kafka.consumer-group-id}")
    @Async
    public void listen(NotificationDTO notificationDTO) {
        log.info("Consumed: {}", notificationDTO);
        ResponseEntity<String> userInfo = restClient.callMicroservice(getUserPath, notificationDTO.getUserId(), "");
        try {
            JsonNode root = objectMapper.readTree(userInfo.getBody());
            String email = root.get("email").toString();
            String chatId = root.get("telegramChatId").toString();

            log.info("{} {}", root.get("email").toString(), root.get("telegramChatId").toString());
            emailNotificationSender.send(email, notificationDTO.getNotificationMessage());

            if (chatId != null && !chatId.isEmpty() && !chatId.equals("null")) {
                telegramNotificationSender.send(chatId, notificationDTO.getNotificationMessage());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}