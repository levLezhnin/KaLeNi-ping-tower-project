package team.kaleni.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {

    @Value("${bot.link}")
    private String prefixLink;

    @Value("${spring.application.subscribe-path}")
    private String telegramServiceSubscribePath;

    @Value("${spring.application.unsubscribe-path}")
    private String telegramServiceUnsubscribePath;

    private final RestClient restClient;
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

    public void subscribeUserByUUID(UUID uuid, Long chatId) {
        Long userId = getUserIdByUUID(uuid);
        try {
            restClient.callMicroservice(telegramServiceSubscribePath, userId, chatId);
            log.info("Пользователь успешно подписан на уведомления в Telegram!");
        } finally {
            userUUIDMapping.removeMapping(uuid);
        }
    }

    public void unsubscribeUserByChatId(Long chatId) {
        restClient.callMicroservice(telegramServiceUnsubscribePath, chatId, null);
        log.info("Пользователь успешно отписался от уведомлений в Telegram!");
    }
}
