package team.kaleni.ping.tower.backend.url_service.dto.inner;

import lombok.Builder;
import lombok.Data;
import team.kaleni.ping.tower.backend.url_service.entity.PingStatus;

import java.util.Map;

@Data
@Builder
public class PingResultDTO {
    private PingStatus status;
    private Integer responseCode;
    private Integer responseTimeMs;
    private String errorMessage;
    private Map<String, Object> metadata;
    private boolean fromCache;
}
