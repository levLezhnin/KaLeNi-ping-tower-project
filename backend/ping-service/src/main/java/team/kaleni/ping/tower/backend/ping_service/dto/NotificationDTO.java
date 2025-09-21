package team.kaleni.ping.tower.backend.ping_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String notificationMessage;
    private Long userId;
}
