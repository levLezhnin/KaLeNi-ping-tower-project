package team.kaleni.ping.tower.backend.statistics_service.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import team.kaleni.ping.tower.backend.statistics_service.dto.HourlyStatsDto;
import team.kaleni.ping.tower.backend.statistics_service.dto.MonitorStatisticsDto;
import team.kaleni.ping.tower.backend.statistics_service.dto.UptimeReportDto;
import team.kaleni.ping.tower.backend.statistics_service.service.StatisticsService;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Statistics", description = "Мониторинг статистики и аналитики пингов")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Получить общую статистику монитора за последние 24 часа
     */
    @GetMapping("/monitor/{monitorId}/24h")
    @Operation(summary = "Статистика за 24 часа", description = "Получить общую статистику монитора за последние 24 часа")
    public ResponseEntity<MonitorStatisticsDto> getMonitorStatistics24h(
            @Parameter(description = "ID монитора", required = true)
            @PathVariable @NotNull @Positive Long monitorId,

            @Parameter(description = "ID владельца (пользователя)")
            @RequestHeader(value = "X-Owner-Id", required = false) String ownerId) {

        log.info("Getting 24h statistics for monitor {} (owner: {})", monitorId, ownerId);

        try {
            MonitorStatisticsDto statistics = statisticsService.getMonitorStatistics24h(monitorId);

            if (statistics == null) {
                log.warn("No statistics found for monitor {} in last 24h", monitorId);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting 24h statistics for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить общую статистику монитора за последние 7 дней
     */
    @GetMapping("/monitor/{monitorId}/7d")
    @Operation(summary = "Статистика за 7 дней", description = "Получить общую статистику монитора за последние 7 дней")
    public ResponseEntity<MonitorStatisticsDto> getMonitorStatistics7d(
            @Parameter(description = "ID монитора", required = true)
            @PathVariable @NotNull @Positive Long monitorId,

            @Parameter(description = "ID владельца (пользователя)")
            @RequestHeader(value = "X-Owner-Id", required = false) String ownerId) {

        log.info("Getting 7d statistics for monitor {} (owner: {})", monitorId, ownerId);

        try {
            MonitorStatisticsDto statistics = statisticsService.getMonitorStatistics7d(monitorId);

            if (statistics == null) {
                log.warn("No statistics found for monitor {} in last 7 days", monitorId);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting 7d statistics for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить статистику монитора за кастомный период
     */
    @GetMapping("/monitor/{monitorId}/custom")
    @Operation(summary = "Статистика за период", description = "Получить статистику монитора за указанный период")
    public ResponseEntity<MonitorStatisticsDto> getMonitorStatisticsCustom(
            @Parameter(description = "ID монитора", required = true)
            @PathVariable @NotNull @Positive Long monitorId,

            @Parameter(description = "Дата начала (yyyy-MM-dd'T'HH:mm:ss)", required = true, example = "2025-09-20T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Дата окончания (yyyy-MM-dd'T'HH:mm:ss)", required = true, example = "2025-09-21T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "ID владельца (пользователя)")
            @RequestHeader(value = "X-Owner-Id", required = false) String ownerId) {

        log.info("Getting custom statistics for monitor {} from {} to {} (owner: {})",
                monitorId, startDate, endDate, ownerId);

        try {
            MonitorStatisticsDto statistics = statisticsService.getMonitorStatistics(monitorId, startDate, endDate);

            if (statistics == null) {
                log.warn("No statistics found for monitor {} in period {} - {}", monitorId, startDate, endDate);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid date range for monitor {}: {}", monitorId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting custom statistics for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить почасовую статистику за последние 24 часа
     */
    @GetMapping("/monitor/{monitorId}/hourly/24h")
    @Operation(summary = "Почасовая статистика за 24 часа", description = "Получить почасовую статистику за последние 24 часа")
    public ResponseEntity<List<HourlyStatsDto>> getHourlyStatistics24h(
            @Parameter(description = "ID монитора", required = true)
            @PathVariable @NotNull @Positive Long monitorId,

            @Parameter(description = "ID владельца (пользователя)")
            @RequestHeader(value = "X-Owner-Id", required = false) String ownerId) {

        log.info("Getting hourly statistics for monitor {} for last 24h (owner: {})", monitorId, ownerId);

        try {
            List<HourlyStatsDto> hourlyStats = statisticsService.getHourlyStatistics24h(monitorId);

            if (hourlyStats.isEmpty()) {
                log.warn("No hourly statistics found for monitor {} in last 24h", monitorId);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(hourlyStats);
        } catch (Exception e) {
            log.error("Error getting hourly statistics for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить почасовую статистику за период
     */
    @GetMapping("/monitor/{monitorId}/hourly/custom")
    @Operation(summary = "Почасовая статистика за период", description = "Получить почасовую статистику за указанный период")
    public ResponseEntity<List<HourlyStatsDto>> getHourlyStatisticsCustom(
            @Parameter(description = "ID монитора", required = true)
            @PathVariable @NotNull @Positive Long monitorId,

            @Parameter(description = "Дата начала (yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Дата окончания (yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "ID владельца (пользователя)")
            @RequestHeader(value = "X-Owner-Id", required = false) String ownerId) {

        log.info("Getting hourly statistics for monitor {} from {} to {} (owner: {})",
                monitorId, startDate, endDate, ownerId);

        try {
            List<HourlyStatsDto> hourlyStats = statisticsService.getHourlyStatistics(monitorId, startDate, endDate);

            if (hourlyStats.isEmpty()) {
                log.warn("No hourly statistics found for monitor {} in period {} - {}", monitorId, startDate, endDate);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(hourlyStats);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid date range for monitor {}: {}", monitorId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting hourly statistics for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить uptime отчет за последние 7 дней
     */
    @GetMapping("/monitor/{monitorId}/uptime/7d")
    @Operation(summary = "Uptime отчет за 7 дней", description = "Получить подробный отчет о времени работы за последние 7 дней")
    public ResponseEntity<UptimeReportDto> getUptimeReport7d(
            @Parameter(description = "ID монитора", required = true)
            @PathVariable @NotNull @Positive Long monitorId,

            @Parameter(description = "ID владельца (пользователя)")
            @RequestHeader(value = "X-Owner-Id", required = false) String ownerId) {

        log.info("Getting uptime report for monitor {} for last 7d (owner: {})", monitorId, ownerId);

        try {
            UptimeReportDto uptimeReport = statisticsService.getUptimeReport7d(monitorId);

            if (uptimeReport == null) {
                log.warn("No uptime data found for monitor {} in last 7 days", monitorId);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(uptimeReport);
        } catch (Exception e) {
            log.error("Error getting uptime report for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить uptime отчет за период
     */
    @GetMapping("/monitor/{monitorId}/uptime/custom")
    @Operation(summary = "Uptime отчет за период", description = "Получить подробный отчет о времени работы за указанный период")
    public ResponseEntity<UptimeReportDto> getUptimeReportCustom(
            @Parameter(description = "ID монитора", required = true)
            @PathVariable @NotNull @Positive Long monitorId,

            @Parameter(description = "Дата начала (yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Дата окончания (yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "ID владельца (пользователя)")
            @RequestHeader(value = "X-Owner-Id", required = false) String ownerId) {

        log.info("Getting uptime report for monitor {} from {} to {} (owner: {})",
                monitorId, startDate, endDate, ownerId);

        try {
            UptimeReportDto uptimeReport = statisticsService.getUptimeReport(monitorId, startDate, endDate);

            if (uptimeReport == null) {
                log.warn("No uptime data found for monitor {} in period {} - {}", monitorId, startDate, endDate);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(uptimeReport);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid date range for monitor {}: {}", monitorId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting uptime report for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Проверка здоровья", description = "Проверить статус подключения к ClickHouse")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        // Здесь можно добавить проверку ClickHouse
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "statistics-service",
                "timestamp", LocalDateTime.now()
        ));
    }
}

