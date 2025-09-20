package team.kaleni.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {

    @Value("${bot.name}")
    private String botName;

    @Bean
    public String botName() {
        return botName;
    }
}
