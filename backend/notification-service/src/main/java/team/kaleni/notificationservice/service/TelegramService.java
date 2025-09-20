package team.kaleni.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TelegramService {

    @Value("${bot.link}")
    private String prefixLink;

    private final UserUUIDMapping userUUIDMapping;

    public UUID generateUUIDByUserId(Long userId) {
        return userUUIDMapping.generateUUIDForUserWithId(userId);
    }

    public String generateSubscriptionLink(Long userId) {
        return prefixLink + "?start=" + generateUUIDByUserId(userId);
    }

    public Long getUserIdByUUID(UUID uuid) {
        return userUUIDMapping.getUserIdByUUID(uuid);
    }

    public void subscribeUserByUUID(UUID uuid) {
        //TODO: обращение к user-service
        System.out.println("Пользователь с id: " + getUserIdByUUID(uuid) + " вызвал метод подписки на уведомления с uuid: " + uuid);
        //TODO: remove UUID mapping
    }

    public void unsubscribeUserByChatId(Long chatId) {
        //TODO: обращение к user-service
        System.out.println("Пользователь с chatId: " + chatId + " вызвал метод отвязки уведомлений.");
    }
}
