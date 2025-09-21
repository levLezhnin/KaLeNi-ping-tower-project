package team.kaleni.ping.tower.backend.ping_service.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.ping_service.dto.NotificationDTO;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationDTO> kafkaTemplate;

    @Value("${spring.kafka.notification-topic}")
    private String notificationTopic;

    @SendTo()
    public void sendNotification(NotificationDTO notificationDTO) {
        kafkaTemplate.send(notificationTopic, notificationDTO);
    }
}
