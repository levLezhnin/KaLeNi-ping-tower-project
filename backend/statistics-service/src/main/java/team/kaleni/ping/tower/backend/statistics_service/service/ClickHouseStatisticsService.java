package team.kaleni.ping.tower.backend.statistics_service.service;

import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseNode;
import com.clickhouse.client.ClickHouseResponse;
import com.clickhouse.data.ClickHouseRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.statistics_service.dto.HourlyStatsDto;
import team.kaleni.ping.tower.backend.statistics_service.dto.MonitorStatisticsDto;
import team.kaleni.ping.tower.backend.statistics_service.dto.UptimeReportDto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickHouseStatisticsService {

    private final ClickHouseClient clickHouseClient;
    private final ClickHouseNode clickHouseNode;

    /**
     * Получить общую статистику по монитору за период
     */
    public MonitorStatisticsDto getMonitorStatistics(Long monitorId, LocalDateTime startDate, LocalDateTime endDate) {
        String query = """
            SELECT 
                monitor_id,
                any(url) as url,
                count(*) as total_pings,
                countIf(status = 'UP') as successful_pings,
                countIf(status != 'UP') as failed_pings,
                (countIf(status = 'UP') * 100.0) / count(*) as uptime_percentage,
                avgIf(response_time_ms, response_time_ms IS NOT NULL) as avg_response_time,
                minIf(response_time_ms, response_time_ms IS NOT NULL) as min_response_time,
                maxIf(response_time_ms, response_time_ms IS NOT NULL) as max_response_time,
                max(ping_timestamp) as last_ping_time,
                any(status) as current_status
            FROM ping_results 
            WHERE monitor_id = ? 
              AND ping_timestamp >= ? 
              AND ping_timestamp <= ?
            GROUP BY monitor_id
            """;

        try (ClickHouseResponse response = clickHouseClient
                .read(clickHouseNode)
                .query(query)
                .params(monitorId, formatDateTime(startDate), formatDateTime(endDate))
                .executeAndWait()) {

            for (ClickHouseRecord record : response.records()) {
                return MonitorStatisticsDto.builder()
                        .monitorId(record.getValue(0).asLong())
                        .url(record.getValue(1).asString())
                        .totalPings(record.getValue(2).asLong())
                        .successfulPings(record.getValue(3).asLong())
                        .failedPings(record.getValue(4).asLong())
                        .uptimePercentage(record.getValue(5).asDouble())
                        .averageResponseTime(record.getValue(6).isNullOrEmpty() ? null : record.getValue(6).asDouble())
                        .minResponseTime(record.getValue(7).isNullOrEmpty() ? null : record.getValue(7).asInteger())
                        .maxResponseTime(record.getValue(8).isNullOrEmpty() ? null : record.getValue(8).asInteger())
                        .lastPingTime(parseDateTime(record.getValue(9).asString()))
                        .currentStatus(record.getValue(10).asString())
                        .periodStart(startDate)
                        .periodEnd(endDate)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error getting monitor statistics for monitor {}: {}", monitorId, e.getMessage(), e);
        }

        return null;
    }

    /**
     * Получить почасовую статистику за период
     */
    public List<HourlyStatsDto> getHourlyStatistics(Long monitorId, LocalDateTime startDate, LocalDateTime endDate) {
        String query = """
            SELECT 
                monitor_id,
                toStartOfHour(ping_timestamp) as hour,
                count(*) as total_pings,
                countIf(status = 'UP') as successful_pings,
                countIf(status != 'UP') as failed_pings,
                (countIf(status = 'UP') * 100.0) / count(*) as uptime_percentage,
                avgIf(response_time_ms, response_time_ms IS NOT NULL) as avg_response_time,
                minIf(response_time_ms, response_time_ms IS NOT NULL) as min_response_time,
                maxIf(response_time_ms, response_time_ms IS NOT NULL) as max_response_time
            FROM ping_results 
            WHERE monitor_id = ? 
              AND ping_timestamp >= ? 
              AND ping_timestamp <= ?
            GROUP BY monitor_id, hour
            ORDER BY hour
            """;

        List<HourlyStatsDto> result = new ArrayList<>();
        try (ClickHouseResponse response = clickHouseClient
                .read(clickHouseNode)
                .query(query)
                .params(monitorId, formatDateTime(startDate), formatDateTime(endDate))
                .executeAndWait()) {

            for (ClickHouseRecord record : response.records()) {
                result.add(HourlyStatsDto.builder()
                        .monitorId(record.getValue(0).asLong())
                        .hour(parseDateTime(record.getValue(1).asString()))
                        .totalPings(record.getValue(2).asLong())
                        .successfulPings(record.getValue(3).asLong())
                        .failedPings(record.getValue(4).asLong())
                        .uptimePercentage(record.getValue(5).asDouble())
                        .averageResponseTime(record.getValue(6).isNullOrEmpty() ? null : record.getValue(6).asDouble())
                        .minResponseTime(record.getValue(7).isNullOrEmpty() ? null : record.getValue(7).asInteger())
                        .maxResponseTime(record.getValue(8).isNullOrEmpty() ? null : record.getValue(8).asInteger())
                        .build());
            }
        } catch (Exception e) {
            log.error("Error getting hourly statistics for monitor {}: {}", monitorId, e.getMessage(), e);
        }

        return result;
    }

    /**
     * Получить отчет об uptime с простыми нарушениями
     */
    public UptimeReportDto getUptimeReport(Long monitorId, LocalDateTime startDate, LocalDateTime endDate) {
        MonitorStatisticsDto overall = getMonitorStatistics(monitorId, startDate, endDate);
        List<HourlyStatsDto> hourlyStats = getHourlyStatistics(monitorId, startDate, endDate);

        if (overall == null) {
            return null;
        }

        // Простой расчет downtime
        Long totalDowntime = calculateDowntime(monitorId, startDate, endDate);

        return UptimeReportDto.builder()
                .monitorId(monitorId)
                .url(overall.getUrl())
                .reportStart(startDate)
                .reportEnd(endDate)
                .overallUptimePercentage(overall.getUptimePercentage())
                .totalDowntime(totalDowntime)
                .hourlyStats(hourlyStats)
                .outages(new ArrayList<>()) // Простая версия без детального анализа outages
                .build();
    }

    /**
     * Простой расчет времени недоступности
     */
    private Long calculateDowntime(Long monitorId, LocalDateTime startDate, LocalDateTime endDate) {
        String query = """
            SELECT count(*) * 30 as downtime_seconds
            FROM ping_results 
            WHERE monitor_id = ? 
              AND status != 'UP'
              AND ping_timestamp >= ? 
              AND ping_timestamp <= ?
            """;

        try (ClickHouseResponse response = clickHouseClient
                .read(clickHouseNode)
                .query(query)
                .params(monitorId, formatDateTime(startDate), formatDateTime(endDate))
                .executeAndWait()) {

            for (ClickHouseRecord record : response.records()) {
                return record.getValue(0).asLong();
            }
        } catch (Exception e) {
            log.error("Error calculating downtime for monitor {}: {}", monitorId, e.getMessage(), e);
        }

        return 0L;
    }

    /**
     * Проверить здоровье подключения
     */
    public boolean isHealthy() {
        try (ClickHouseResponse response = clickHouseClient
                .read(clickHouseNode)
                .query("SELECT 1")
                .executeAndWait()) {

            return response.records().iterator().hasNext();
        } catch (Exception e) {
            log.error("ClickHouse health check failed: {}", e.getMessage());
            return false;
        }
    }

    // Helper methods
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) return null;
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

