package team.kaleni.ping.tower.backend.ping_service.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import team.kaleni.ping.tower.backend.ping_service.dto.NotificationDTO;
import team.kaleni.ping.tower.backend.ping_service.dto.PingResultDto;
import team.kaleni.ping.tower.backend.ping_service.producer.NotificationProducer;

@Component
@RequiredArgsConstructor
public class NotificationSender {

    private final String INFO = """
            Название сервиса: %s
            URL сервиса: %s
            Статус: %s
            Код ответа сервера: %s
            Ответ сервера: %s
            Время ответа сервиса (мс): %s
            Получено в: %s
            """;

    private final String BAD_REQUEST = """
            ❗ Некорректно составлен запрос! ❗
            
            """ + INFO;

    private final String ERROR = """
            ❗❗❗ Сервис упал! ❗❗❗
            
            """ + INFO;

    private String format(String template, String monitorName, PingResultDto pingResultDto) {
        return template.formatted(
                monitorName,
                pingResultDto.getUrl(),
                pingResultDto.getStatus().name(),
                pingResultDto.getResponseCode().toString(),
                pingResultDto.getErrorMessage(),
                pingResultDto.getResponseTimeMs().toString(),
                pingResultDto.getTimestamp().toString()
        );
    }

    private final NotificationProducer notificationProducer;

    public void sendNotification(Long userId, String monitorName, PingResultDto pingResultDto) {
        if (400 <= pingResultDto.getResponseCode() && pingResultDto.getResponseCode() <= 499) {
            notificationProducer.sendNotification(
                    new NotificationDTO(
                            format(BAD_REQUEST, monitorName, pingResultDto),
                            userId
                    )
            );
        }
        if (500 <= pingResultDto.getResponseCode()) {
            notificationProducer.sendNotification(
                    new NotificationDTO(
                            format(ERROR, monitorName, pingResultDto),
                            userId
                    )
            );
        }
    }
}
