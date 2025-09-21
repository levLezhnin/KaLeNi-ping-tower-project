package team.kaleni.ping.tower.backend.statistics_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.kaleni.ping.tower.backend.statistics_service.dto.ChartDataPointDto;
import team.kaleni.ping.tower.backend.statistics_service.dto.HourlyStatsDto;
import team.kaleni.ping.tower.backend.statistics_service.service.StatisticsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Получить почасовую статистику за последние 24 часа
     */
    @GetMapping("/monitors/{monitorId}/hourly/24h")
    public ResponseEntity<List<HourlyStatsDto>> getHourlyStatistics24h(
            @PathVariable Long monitorId) {

        log.info("Getting hourly statistics for monitor {} for last 24h", monitorId);

        try {
            List<HourlyStatsDto> stats = statisticsService.getHourlyStatistics24h(monitorId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting hourly statistics for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получить почасовую статистику за произвольный период
     */
    @GetMapping("/monitors/{monitorId}/hourly")
    public ResponseEntity<List<HourlyStatsDto>> getHourlyStatistics(
            @PathVariable Long monitorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime) {

        log.info("Getting hourly statistics for monitor {} from {} to {}", monitorId, startTime, endTime);

        try {
            List<HourlyStatsDto> stats = statisticsService.getHourlyStatistics(monitorId, startTime, endTime);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting hourly statistics for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получить данные для графика за последние 24 часа
     */
    @GetMapping("/monitors/{monitorId}/chart/24h")
    public ResponseEntity<List<ChartDataPointDto>> getChartData24h(@PathVariable Long monitorId) {
        log.info("Getting chart data for monitor {} for last 24h", monitorId);

        try {
            List<ChartDataPointDto> data = statisticsService.getChartData24h(monitorId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error getting chart data for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получить данные для графика за произвольный период
     */
    @GetMapping("/monitors/{monitorId}/chart")
    public ResponseEntity<List<ChartDataPointDto>> getChartData(
            @PathVariable Long monitorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime) {

        log.info("Getting chart data for monitor {} from {} to {}", monitorId, startTime, endTime);

        try {
            List<ChartDataPointDto> data = statisticsService.getChartData(monitorId, startTime, endTime);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error getting chart data for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
