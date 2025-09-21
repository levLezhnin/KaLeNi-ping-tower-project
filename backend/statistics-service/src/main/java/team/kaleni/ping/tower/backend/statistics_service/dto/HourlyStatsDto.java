package team.kaleni.ping.tower.backend.statistics_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyStatsDto {
    private Long monitorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime hour;

    private Long totalPings;
    private Long successfulPings;
    private Long failedPings;
    private Double uptimePercentage;
    private Double averageResponseTime;
    private Integer minResponseTime;
    private Integer maxResponseTime;
}

