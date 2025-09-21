package team.kaleni.ping.tower.backend.statistics_service.service;

import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseNode;
import com.clickhouse.client.ClickHouseResponse;
import com.clickhouse.data.ClickHouseRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.statistics_service.dto.ChartDataPointDto;
import team.kaleni.ping.tower.backend.statistics_service.dto.PingResultDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickHouseStatisticsService {

    private final ClickHouseClient clickHouseClient;
    private final ClickHouseNode clickHouseNode;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Получить все данные пинга за период для конкретного монитора
     */
    public List<PingResultDto> getPingResults(Long monitorId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting ping results for monitor {} from {} to {}", monitorId, startTime, endTime);

        String startTimeStr = startTime.format(FORMATTER);
        String endTimeStr = endTime.format(FORMATTER);

        String query = String.format("""
                SELECT 
                    monitor_id,
                    ping_timestamp,
                    status,
                    response_time_ms,
                    response_code,
                    error_message,
                    url
                FROM ping_history.ping_results
                WHERE monitor_id = %d
                  AND ping_timestamp >= '%s'
                  AND ping_timestamp <= '%s'
                ORDER BY ping_timestamp
                FORMAT TabSeparated
                """, monitorId, startTimeStr, endTimeStr);

        List<PingResultDto> results = new ArrayList<>();

        try {
            log.debug("Executing query: {}", query);

            try (ClickHouseResponse response = clickHouseClient
                    .read(clickHouseNode)
                    .query(query)
                    .executeAndWait()) {

                // Парсим TabSeparated построчно
                for (ClickHouseRecord record : response.records()) {
                    String fullRow = record.getValue(0).asString();
                    if (fullRow.length() < 2) {
                        continue;
                    }
                    log.debug("Raw row: {}", fullRow);

                    // Разбиваем строку по табам
                    String[] parts = fullRow.split("\\t");

                    if (parts.length >= 7) {
                        PingResultDto dto = PingResultDto.builder()
                                .monitorId(Long.parseLong(parts[0]))
                                .pingTimestamp(LocalDateTime.parse(parts[1], DateTimeFormatter.ofPattern("yyyy-MM-dd " +
                                        "HH:mm:ss.SSS")))
                                .status(parts[2])
                                // Правильная обработка NULL значений
                                .responseTimeMs(isNullValue(parts[3]) ? null : Integer.parseInt(parts[3]))
                                .responseCode(isNullValue(parts[4]) ? null : Integer.parseInt(parts[4]))
                                .errorMessage(isNullValue(parts[5]) ? null : parts[5])
                                .url(parts[6])
                                .build();
                        results.add(dto);
                    }
                }

                log.info("Retrieved {} ping results for monitor {}", results.size(), monitorId);

            }
        } catch (Exception e) {
            log.error("Error getting ping results for monitor {}: {}", monitorId, e.getMessage(), e);
        }

        return results;
    }


    /**
     * Получить данные для графика (только нужные поля)
     */
    public List<ChartDataPointDto> getChartData(Long monitorId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting chart data for monitor {} from {} to {}", monitorId, startTime, endTime);

        String startTimeStr = startTime.format(FORMATTER);
        String endTimeStr = endTime.format(FORMATTER);

        String query = String.format("""
        SELECT DISTINCT
        ping_timestamp,
        status,
        response_time_ms,
        response_code
        FROM ping_history.ping_results
        WHERE monitor_id = %d
        AND ping_timestamp >= '%s'
        AND ping_timestamp <= '%s'
        ORDER BY ping_timestamp
        FORMAT TabSeparated
        """, monitorId, startTimeStr, endTimeStr);

        List<ChartDataPointDto> results = new ArrayList<>();

        try {
            log.debug("Executing chart data query: {}", query);

            try (ClickHouseResponse response = clickHouseClient
                    .read(clickHouseNode)
                    .query(query)
                    .executeAndWait()) {

                for (ClickHouseRecord record : response.records()) {
                    String fullRow = record.getValue(0).asString();
                    if (fullRow.length() < 2) {
                        continue;
                    }

                    log.debug("Raw chart row: {}", fullRow);

                    String[] parts = fullRow.split("\\t");
                    if (parts.length >= 4) {
                        try {
                            // Более надежная обработка пустых значений
                            Integer responseTimeMs = null;
                            if (parts.length > 2 && !isNullValue(parts[2])) {
                                responseTimeMs = Integer.parseInt(parts[2]);
                            }

                            Integer responseCode = null;
                            if (parts.length > 3 && !isNullValue(parts[3])) {
                                responseCode = Integer.parseInt(parts[3]);
                            }

                            ChartDataPointDto dto = ChartDataPointDto.builder()
                                    .pingTimestamp(LocalDateTime.parse(parts[0],
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")))
                                    .status(parts[1])
                                    .responseTimeMs(responseTimeMs)
                                    .responseCode(responseCode)
                                    .build();

                            results.add(dto);
                            log.debug("Parsed chart data point: {}", dto);

                        } catch (Exception e) {
                            log.warn("Error parsing chart data row: {} - {}", fullRow, e.getMessage());
                            continue;
                        }
                    }
                }
            }

            log.info("Retrieved {} chart data points for monitor {}", results.size(), monitorId);

        } catch (Exception e) {
            log.error("Error getting chart data for monitor {}: {}", monitorId, e.getMessage(), e);
        }

        return results;
    }


    // Добавить метод для проверки NULL значений
    private boolean isNullValue(String value) {
        return value == null ||
                "\\N".equals(value) ||
                value.trim().isEmpty() ||
                "NULL".equalsIgnoreCase(value.trim());
    }


}
