package team.kaleni.ping.tower.backend.statistics_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UptimeReportDto {
    private Long monitorId;
    private String url;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportEnd;

    private Double overallUptimePercentage;
    private Long totalDowntime; // в секундах
    private List<HourlyStatsDto> hourlyStats;
    private List<OutageDto> outages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutageDto {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;

        private Long durationSeconds;
        private String reason;
    }
}

