package team.kaleni.ping.tower.backend.ping_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.kaleni.ping.tower.backend.ping_service.enums.PingStatus;
import team.kaleni.ping.tower.backend.ping_service.dto.PingResultDto;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PingResult {

    private Long monitorId;
    private Instant pingTimestamp;
    private PingStatus status;
    private Integer responseTimeMs;
    private Integer responseCode;
    private String errorMessage;
    private String url;
    private Instant createdAt;

    /**
     * 🔥 Создать PingResult из DTO
     */
    public static PingResult fromPingResultDto(PingResultDto dto) {
        return PingResult.builder()
                .monitorId(dto.getMonitorId())
                .pingTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now())
                .status(dto.getStatus())
                .responseTimeMs(dto.getResponseTimeMs())
                .responseCode(dto.getResponseCode())
                .errorMessage(dto.getErrorMessage())
                .url(dto.getUrl())
                .createdAt(Instant.now())
                .build();
    }
}
