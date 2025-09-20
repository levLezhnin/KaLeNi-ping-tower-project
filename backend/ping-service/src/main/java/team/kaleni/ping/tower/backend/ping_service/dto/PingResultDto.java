package team.kaleni.ping.tower.backend.ping_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.kaleni.ping.tower.backend.ping_service.enums.PingStatus;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PingResultDto {
    private Long monitorId;
    private PingStatus status;
    private Integer responseTimeMs;
    private Integer responseCode;
    private String errorMessage;
    private Instant timestamp;
    private String url;
    private Map<String, Object> metadata;
    private boolean fromCache;
}
