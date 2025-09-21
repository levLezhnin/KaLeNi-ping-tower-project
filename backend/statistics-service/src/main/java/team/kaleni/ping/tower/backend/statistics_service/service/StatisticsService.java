package team.kaleni.ping.tower.backend.statistics_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.statistics_service.dto.HourlyStatsDto;
import team.kaleni.ping.tower.backend.statistics_service.dto.MonitorStatisticsDto;
import team.kaleni.ping.tower.backend.statistics_service.dto.UptimeReportDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final ClickHouseStatisticsService clickHouseStatisticsService;

    /**
     * Получить статистику монитора за последние 24 часа
     */
    public MonitorStatisticsDto getMonitorStatistics24h(Long monitorId) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minus(24, ChronoUnit.HOURS);

        log.info("Getting 24h statistics for monitor {}", monitorId);
        return clickHouseStatisticsService.getMonitorStatistics(monitorId, startDate, endDate);
    }

    /**
     * Получить статистику монитора за последние 7 дней
     */
    public MonitorStatisticsDto getMonitorStatistics7d(Long monitorId) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minus(7, ChronoUnit.DAYS);

        log.info("Getting 7d statistics for monitor {}", monitorId);
        return clickHouseStatisticsService.getMonitorStatistics(monitorId, startDate, endDate);
    }

    /**
     * Получить статистику монитора за период
     */
    public MonitorStatisticsDto getMonitorStatistics(Long monitorId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        log.info("Getting statistics for monitor {} from {} to {}", monitorId, startDate, endDate);
        return clickHouseStatisticsService.getMonitorStatistics(monitorId, startDate, endDate);
    }

    /**
     * Получить почасовую статистику за последние 24 часа
     */
    public List<HourlyStatsDto> getHourlyStatistics24h(Long monitorId) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minus(24, ChronoUnit.HOURS);

        log.info("Getting hourly statistics for monitor {} for last 24h", monitorId);
        return clickHouseStatisticsService.getHourlyStatistics(monitorId, startDate, endDate);
    }

    /**
     * Получить почасовую статистику за период
     */
    public List<HourlyStatsDto> getHourlyStatistics(Long monitorId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        log.info("Getting hourly statistics for monitor {} from {} to {}", monitorId, startDate, endDate);
        return clickHouseStatisticsService.getHourlyStatistics(monitorId, startDate, endDate);
    }

    /**
     * Получить uptime отчет за период
     */
    public UptimeReportDto getUptimeReport(Long monitorId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        log.info("Getting uptime report for monitor {} from {} to {}", monitorId, startDate, endDate);
        return clickHouseStatisticsService.getUptimeReport(monitorId, startDate, endDate);
    }

    /**
     * Получить uptime отчет за последние 7 дней
     */
    public UptimeReportDto getUptimeReport7d(Long monitorId) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minus(7, ChronoUnit.DAYS);

        return getUptimeReport(monitorId, startDate, endDate);
    }
}

