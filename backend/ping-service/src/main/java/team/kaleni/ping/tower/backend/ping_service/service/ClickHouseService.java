package team.kaleni.ping.tower.backend.ping_service.service;

import com.clickhouse.client.*;
import com.clickhouse.data.ClickHouseRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.ping_service.entity.PingResult;
import team.kaleni.ping.tower.backend.ping_service.enums.PingStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickHouseService {

    private final ClickHouseClient clickHouseClient;
    private final ClickHouseNode clickHouseNode;

    private static final String INSERT_PING_RESULT = """
        INSERT INTO ping_results (
            monitor_id, ping_timestamp, status, response_time_ms,
            response_code, error_message, url, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    /**
     * Сохранить один результат пинга в ClickHouse
     */
    public void savePingResult(PingResult pingResult) {
        try (ClickHouseResponse response = clickHouseClient
                .read(clickHouseNode)
                .write()
                .query(INSERT_PING_RESULT)
                .params(
                        pingResult.getMonitorId(),
                        formatInstant(pingResult.getPingTimestamp()),
                        pingResult.getStatus().name(),
                        pingResult.getResponseTimeMs(),
                        pingResult.getResponseCode(),
                        pingResult.getErrorMessage(),
                        pingResult.getUrl(),
                        formatInstant(pingResult.getCreatedAt())
                )
                .executeAndWait()) {

            log.debug("Saved ping result for monitor {} to ClickHouse", pingResult.getMonitorId());
        } catch (Exception e) {
            log.error("Error saving ping result for monitor {} to ClickHouse: {}",
                    pingResult.getMonitorId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save ping result", e);
        }
    }

    /**
     * Пакетное сохранение результатов пингов
     */
    public void savePingResultsBatch(List<PingResult> pingResults) {
        if (pingResults == null || pingResults.isEmpty()) {
            return;
        }

        try {
            for (PingResult result : pingResults) {
                savePingResult(result);
            }
            log.info("Saved batch of {} ping results to ClickHouse", pingResults.size());

        } catch (Exception e) {
            log.error("Error saving batch of {} ping results to ClickHouse: {}",
                    pingResults.size(), e.getMessage(), e);
            throw new RuntimeException("Failed to save ping results batch", e);
        }
    }

    public void savePingResultsBatchOptimized(List<PingResult> pingResults) {
        if (pingResults == null || pingResults.isEmpty()) {
            return;
        }

        try {
            StringBuilder queryBuilder = new StringBuilder(
                    "INSERT INTO ping_results (monitor_id, ping_timestamp, status, response_time_ms, " +
                            "response_code, error_message, url, created_at) VALUES ");

            for (int i = 0; i < pingResults.size(); i++) {
                if (i > 0) queryBuilder.append(", ");

                PingResult result = pingResults.get(i);
                queryBuilder.append("(")
                        .append(result.getMonitorId()).append(", ")
                        .append("'").append(formatInstant(result.getPingTimestamp())).append("', ")
                        .append("'").append(result.getStatus().name()).append("', ")
                        .append(result.getResponseTimeMs() != null ? result.getResponseTimeMs() : "NULL").append(", ")
                        .append(result.getResponseCode() != null ? result.getResponseCode() : "NULL").append(", ")
                        .append(result.getErrorMessage() != null ? "'" + result.getErrorMessage().replace("'", "\\'") + "'" : "NULL").append(", ")
                        .append("'").append(result.getUrl()).append("', ")
                        .append("'").append(formatInstant(result.getCreatedAt())).append("'")
                        .append(")");
            }

            try (ClickHouseResponse response = clickHouseClient
                    .read(clickHouseNode)
                    .write()
                    .query(queryBuilder.toString())
                    .executeAndWait()) {

                log.info("Saved optimized batch of {} ping results to ClickHouse", pingResults.size());
            }

        } catch (Exception e) {
            log.error("Error saving optimized batch of {} ping results to ClickHouse: {}",
                    pingResults.size(), e.getMessage(), e);
            throw new RuntimeException("Failed to save ping results batch", e);
        }
    }

    /**
     * Получить последние результаты пингов для монитора
     */
    public List<PingResult> getRecentPingResults(Long monitorId, int limit) {
        String query = """
            SELECT monitor_id, ping_timestamp, status, response_time_ms,
                   response_code, error_message, url, created_at
            FROM ping_results
            WHERE monitor_id = ?
            ORDER BY ping_timestamp DESC
            LIMIT ?
            """;

        try {
            List<PingResult> results = new ArrayList<>();
            try (ClickHouseResponse response = clickHouseClient
                    .read(clickHouseNode)
                    .query(query)
                    .params(monitorId, limit)
                    .executeAndWait()) {

                for (ClickHouseRecord record : response.records()) {
                    PingResult result = PingResult.builder()
                            .monitorId(record.getValue(0).asLong())
                            .pingTimestamp(parseInstant(record.getValue(1).asString()))
                            .status(PingStatus.valueOf(record.getValue(2).asString()))
                            .responseTimeMs(record.getValue(3).isNullOrEmpty() ? null : record.getValue(3).asInteger())
                            .responseCode(record.getValue(4).isNullOrEmpty() ? null : record.getValue(4).asInteger())
                            .errorMessage(record.getValue(5).isNullOrEmpty() ? null : record.getValue(5).asString())
                            .url(record.getValue(6).asString())
                            .createdAt(parseInstant(record.getValue(7).asString()))
                            .build();
                    results.add(result);
                }
            }

            log.debug("Retrieved {} recent ping results for monitor {}", results.size(), monitorId);
            return results;
        } catch (Exception e) {
            log.error("Error getting recent ping results for monitor {}: {}", monitorId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Проверить соединение с ClickHouse
     */
    public boolean isHealthy() {
        try {
            try (ClickHouseResponse response = clickHouseClient
                    .read(clickHouseNode)
                    .query("SELECT 1")
                    .executeAndWait()) {

                return response.records().iterator().hasNext();
            }
        } catch (Exception e) {
            log.error("ClickHouse health check failed: {}", e.getMessage());
            return false;
        }
    }

    // Helper methods
    private String formatInstant(Instant instant) {
        if (instant == null) return null;
        return instant.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    private Instant parseInstant(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) return null;
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                .atOffset(ZoneOffset.UTC)
                .toInstant();
    }
}
