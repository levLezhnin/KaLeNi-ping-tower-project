package team.kaleni.notificationservice.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import team.kaleni.notificationservice.telegram.TelegramBot;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationSender implements NotificationSender {

    private final TelegramBot telegramBot;

    @Override
    public void send(String recipient, String message) {
        try {
            telegramBot.sendMessage(Long.parseLong(recipient), message);
        } catch (NumberFormatException e) {
            log.error("Can't send notification to a chat with id: {}", recipient);
        }
    }
}
