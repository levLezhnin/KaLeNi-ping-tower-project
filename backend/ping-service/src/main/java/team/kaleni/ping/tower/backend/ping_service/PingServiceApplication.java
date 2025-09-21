package team.kaleni.ping.tower.backend.ping_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import team.kaleni.ping.tower.backend.ping_service.dto.NotificationDTO;
import team.kaleni.ping.tower.backend.ping_service.producer.NotificationProducer;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PingServiceApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(PingServiceApplication.class, args);
		NotificationProducer notificationProducer = context.getBean(NotificationProducer.class);
		notificationProducer.sendNotification(new NotificationDTO("Test", 1L));
	}

}
