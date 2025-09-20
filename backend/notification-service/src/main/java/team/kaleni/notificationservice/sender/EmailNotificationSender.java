package team.kaleni.notificationservice.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private static final String SUBJECT = "Ping Tower notification";

    private final JavaMailSender javaMailSender;

    @Override
    public void send(String recipient, String message) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(recipient);
            simpleMailMessage.setSubject(SUBJECT);
            simpleMailMessage.setText(message);
            javaMailSender.send(simpleMailMessage);
        } catch (MailException mailException) {
            log.warn("Couldn't send email: {}", mailException.getMessage());
        }
    }
}
