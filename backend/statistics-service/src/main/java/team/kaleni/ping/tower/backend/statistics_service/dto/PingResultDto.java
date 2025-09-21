package team.kaleni.ping.tower.backend.statistics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PingResultDto {
    private Long monitorId;
    private LocalDateTime pingTimestamp;
    private String status;
    private Integer responseTimeMs;
    private Integer responseCode;
    private String errorMessage;
    private String url;

    @Override
    public String toString() {
        return "PingResultDto{" +
                "monitorId=" + monitorId +
                ", pingTimestamp=" + pingTimestamp +
                ", status='" + status + '\'' +
                ", responseTimeMs=" + responseTimeMs +
                ", responseCode=" + responseCode +
                ", errorMessage='" + errorMessage + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}

