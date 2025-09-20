package team.kaleni.notificationservice.sender;

import org.springframework.stereotype.Component;

@Component
public class TelegramNotificationSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {

    }
}
