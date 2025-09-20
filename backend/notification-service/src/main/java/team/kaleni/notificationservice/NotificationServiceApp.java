package team.kaleni.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.support.serializer.JsonSerializer;
import team.kaleni.notificationservice.dto.notification.NotificationDTO;
import team.kaleni.notificationservice.dto.notification.NotificationType;

@SpringBootApplication
public class NotificationServiceApp {
    public static void main(String[] args) {
        JsonSerializer<NotificationDTO> jsonSerializer = new JsonSerializer<>();
        String string = new String(
                jsonSerializer.serialize(
                "notification-topic",
                        NotificationDTO.builder()
                                .notificationMessage("message")
                                .type(NotificationType.EMAIL.name())
                                .recipient("lezhnin.lev78@gmail.com")
                                .build()
                )
        );
        System.out.println(string);
        SpringApplication.run(NotificationServiceApp.class, args);
    }
}
