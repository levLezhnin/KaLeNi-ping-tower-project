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
public class MonitorStatisticsDto {
    private Long monitorId;
    private String url;
    private Long totalPings;
    private Long successfulPings;
    private Long failedPings;
    private Double uptimePercentage;
    private Double averageResponseTime;
    private Integer minResponseTime;
    private Integer maxResponseTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastPingTime;

    private String currentStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;
}

