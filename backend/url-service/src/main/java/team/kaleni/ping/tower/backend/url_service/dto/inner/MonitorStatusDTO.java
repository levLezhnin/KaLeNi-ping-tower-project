package team.kaleni.ping.tower.backend.url_service.dto.inner;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.kaleni.ping.tower.backend.url_service.entity.PingStatus;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorStatusDTO {
    private PingStatus status;
    private Instant lastCheckedAt;
    private Integer responseTimeMs;
    private Integer responseCode;
    private String errorMessage;
}

