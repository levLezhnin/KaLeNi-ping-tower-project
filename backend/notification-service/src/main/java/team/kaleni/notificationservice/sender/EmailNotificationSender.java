package team.kaleni.notificationservice.sender;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private static final String SUBJECT = "Ping Tower notification";

    private final JavaMailSender mailSender;

    @Override
    public void send(String recipient, String message) {
        try {
            MimeMessage htmlMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(htmlMessage, true);

            helper.setTo(recipient);
            helper.setSubject(SUBJECT);

            try (var inputStream = Objects.requireNonNull(
                    getClass().getResourceAsStream("/html-templates/email-content.html"))) {

                String htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).formatted(message);
                helper.setText(htmlContent, true);
            } catch (IOException e) {
                log.error("HTML-файл формы не найден!");
                return;
            }

            mailSender.send(htmlMessage);

            log.info("Notification has been successfully sent through mail!");

        } catch (Exception e) {
            log.error("Error sending mail notification: {}", e.getMessage());
        }
    }
}
