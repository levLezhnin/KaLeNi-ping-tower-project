package team.kaleni.notificationservice.dto.notification;

import lombok.Getter;
import lombok.Setter;
import team.kaleni.notificationservice.sender.NotificationSender;

public enum NotificationType {
    TELEGRAM("telegramNotificationSender"),
    EMAIL("emailNotificationSender");

    @Getter
    private String beanName;

    @Getter
    @Setter
    private NotificationSender notificationSender;

    NotificationType(String beanName) {
        this.beanName = beanName;
    }
}
