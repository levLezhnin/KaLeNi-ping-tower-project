package team.kaleni.notificationservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import team.kaleni.notificationservice.dto.NotificationType;
import team.kaleni.notificationservice.sender.NotificationSender;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationTypeInjector {

    private final Map<String, NotificationSender> senderMap;

    @PostConstruct
    public void injectSenders() {
        for (NotificationType type : NotificationType.values()) {
            type.setNotificationSender(senderMap.get(type.getBeanName()));
        }
    }
}
