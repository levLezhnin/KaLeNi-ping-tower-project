package team.kaleni.notificationservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import team.kaleni.notificationservice.dto.notification.NotificationDTO;
import team.kaleni.notificationservice.dto.notification.NotificationType;

@Service
@Slf4j
public class NotificationConsumer {
    @KafkaListener(topics = "notificationsTopic", groupId = "notification-consumer-group")
    @Async
    public void listen(NotificationDTO notificationDTO) {
        log.info("Consumed: {}", notificationDTO);
        NotificationType notificationType = NotificationType.valueOf(notificationDTO.getType());
        notificationType.getNotificationSender().send(notificationDTO.getRecipient(), notificationDTO.getNotificationMessage());
    }
}