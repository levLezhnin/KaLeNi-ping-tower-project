package team.kaleni.notificationservice.dto.notification;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Value
@Builder
public class NotificationDTO {
    String notificationMessage;
    String type;
    String recipient;
}
