package team.kaleni.notificationservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import team.kaleni.notificationservice.dto.NotificationDTO;
import team.kaleni.notificationservice.dto.NotificationType;

@Service
@Slf4j
public class NotificationConsumer {

    @KafkaListener(topics = "${spring.kafka.notification-topic}", groupId = "${spring.kafka.consumer-group-id}")
    @Async
    public void listen(NotificationDTO notificationDTO) {
        log.info("Consumed: {}", notificationDTO);
        NotificationType notificationType = NotificationType.valueOf(notificationDTO.getType());
        notificationType.getNotificationSender().send(notificationDTO.getRecipient(), notificationDTO.getNotificationMessage());
    }
}