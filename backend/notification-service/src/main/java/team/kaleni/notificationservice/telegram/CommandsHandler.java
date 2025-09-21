package team.kaleni.notificationservice.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import team.kaleni.notificationservice.config.TelegramBotResponses;
import team.kaleni.notificationservice.telegram.commands.Command;
import team.kaleni.notificationservice.telegram.commands.StartCommand;
import team.kaleni.notificationservice.telegram.commands.UnsubscribeCommand;

import java.util.Map;

@Component
@Slf4j
public class CommandsHandler {

    private final Map<String, Command> commands;

    public CommandsHandler(@Autowired StartCommand startCommand, @Autowired UnsubscribeCommand unsubscribeCommand) {
        this.commands = Map.of(
                "/start", startCommand,
                "/unsubscribe", unsubscribeCommand
        );
    }

    public SendMessage handleCommands(Update update) {
        String messageText = update.getMessage().getText();
        String command = messageText.split(" ")[0];
        long chatId = update.getMessage().getChatId();

        var commandHandler = commands.get(command);
        if (commandHandler != null) {
            return commandHandler.apply(update);
        } else {
            return new SendMessage(String.valueOf(chatId), TelegramBotResponses.MSG_BAD_REQUEST);
        }
    }

}