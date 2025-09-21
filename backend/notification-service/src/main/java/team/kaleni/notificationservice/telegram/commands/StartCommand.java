package team.kaleni.notificationservice.telegram.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import team.kaleni.notificationservice.config.TelegramBotResponses;
import team.kaleni.notificationservice.service.TelegramService;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartCommand implements Command {

    private final TelegramService telegramService;

    @Override
    public SendMessage apply(Update update) {

        long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chatId);

        String resultTextMessage;

        Stream<String> commandArguments = Arrays.stream(update.getMessage().getText().split(" ")).skip(1);
        Optional<String> uuidString = commandArguments.findFirst();

        if (uuidString.isPresent()) {
            try {

                UUID uuid = UUID.fromString(uuidString.get());

                telegramService.subscribeUserByUUID(uuid, chatId);
                resultTextMessage = TelegramBotResponses.MSG_SUBSCRIBE_OK;

            } catch (Exception e) {
                log.error("Something went wrong");
                e.printStackTrace();
                resultTextMessage = TelegramBotResponses.MSG_SUBSCRIBE_FAILED;
            }
        } else {
            resultTextMessage = TelegramBotResponses.MSG_SUBSCRIBE_NOT_OK;
        }

        sendMessage.setText(resultTextMessage);

        return sendMessage;
    }
}
