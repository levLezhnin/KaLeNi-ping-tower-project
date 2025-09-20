package team.kaleni.ping.tower.backend.ping_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.kaleni.ping.tower.backend.ping_service.enums.PingStatus;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorStatusDto{
    private PingStatus status;
    private Instant lastCheckedAt;
    private Integer responseTimeMs;
    private Integer responseCode;
    private String errorMessage;
}
