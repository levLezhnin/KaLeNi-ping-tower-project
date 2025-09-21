package team.kaleni.ping.tower.backend.statistics_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.statistics_service.dto.HourlyStatsDto;
import team.kaleni.ping.tower.backend.statistics_service.dto.PingResultDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final ClickHouseStatisticsService clickHouseService;

    /**
     * Получить почасовую статистику за последние 24 часа
     */
    public List<HourlyStatsDto> getHourlyStatistics24h(Long monitorId) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(24);
        log.info("Now we have time {} at the server", endTime);
        return getHourlyStatistics(monitorId, startTime, endTime);
    }

    /**
     * Получить почасовую статистику за период
     */
    public List<HourlyStatsDto> getHourlyStatistics(Long monitorId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Calculating hourly statistics for monitor {} from {} to {}", monitorId, startTime, endTime);

        // Получаем сырые данные из ClickHouse
        List<PingResultDto> rawData = clickHouseService.getPingResults(monitorId, startTime, endTime);

        if (rawData.isEmpty()) {
            log.warn("No ping data found for monitor {} in period {} - {}", monitorId, startTime, endTime);
            return new ArrayList<>();
        }

        // Группируем по часам
        Map<LocalDateTime, List<PingResultDto>> groupedByHour = rawData.stream()
                .collect(Collectors.groupingBy(result ->
                        result.getPingTimestamp().truncatedTo(ChronoUnit.HOURS)));

        // Вычисляем статистику для каждого часа
        List<HourlyStatsDto> hourlyStats = groupedByHour.entrySet().stream()
                .map(entry -> calculateHourlyStats(monitorId, entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(HourlyStatsDto::getHour))
                .collect(Collectors.toList());

        log.info("Calculated statistics for {} hours", hourlyStats.size());
        return hourlyStats;
    }

    /**
     * Вычислить статистику для одного часа
     */
    private HourlyStatsDto calculateHourlyStats(Long monitorId, LocalDateTime hour, List<PingResultDto> pings) {
        long totalPings = pings.size();
        log.info(pings.getFirst().toString());
        long successfulPings = pings.stream()
                .mapToLong(ping -> "UP".equals(ping.getStatus()) ? 1L : 0L)
                .sum();
        long failedPings = totalPings - successfulPings;

        double uptimePercentage = totalPings > 0 ? (successfulPings * 100.0) / totalPings : 0.0;

        // Считаем статистику по времени ответа только для успешных пингов
        List<Integer> responseTimes = pings.stream()
                .filter(ping -> "UP".equals(ping.getStatus()) && ping.getResponseTimeMs() != null)
                .map(PingResultDto::getResponseTimeMs)
                .collect(Collectors.toList());

        Double averageResponseTime = null;
        Integer minResponseTime = null;
        Integer maxResponseTime = null;

        if (!responseTimes.isEmpty()) {
            averageResponseTime = responseTimes.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            minResponseTime = Collections.min(responseTimes);
            maxResponseTime = Collections.max(responseTimes);
        }

        return HourlyStatsDto.builder()
                .monitorId(monitorId)
                .hour(hour)
                .totalPings(totalPings)
                .successfulPings(successfulPings)
                .failedPings(failedPings)
                .uptimePercentage(uptimePercentage)
                .averageResponseTime(averageResponseTime)
                .minResponseTime(minResponseTime)
                .maxResponseTime(maxResponseTime)
                .build();
    }
}
