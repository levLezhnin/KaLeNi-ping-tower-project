package team.kaleni.notificationservice.telegram.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import team.kaleni.notificationservice.config.TelegramBotResponses;
import team.kaleni.notificationservice.service.TelegramService;

@Component
@RequiredArgsConstructor
public class UnsubscribeCommand implements Command {

    private final TelegramService telegramService;

    @Override
    public SendMessage apply(Update update) {

        long chatId = update.getMessage().getChatId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        String msg;

        try {
            telegramService.unsubscribeUserByChatId(chatId);
            msg = TelegramBotResponses.MSG_UNSUBSCRIBE_OK;
        } catch (Exception e) {
            msg = TelegramBotResponses.MSG_UNSUBSCRIBE_NOT_OK;
        }

        sendMessage.setText(msg);

        return sendMessage;
    }

}
