package team.kaleni.ping.tower.backend.ping_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.ping_service.dto.PingResultDto;
import team.kaleni.ping.tower.backend.ping_service.entity.PingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
@Slf4j
public class PingHistoryService {

    private final ClickHouseService clickHouseService;
    private final ConcurrentLinkedQueue<PingResult> pendingResults = new ConcurrentLinkedQueue<>();

    @Value("${ping.batch.size:50}")
    private int batchSize;

    /**
     * Добавить результат пинга в батч для сохранения
     */
    @Async
    public void addToBatch(PingResultDto pingResultDto) {
        try {
            PingResult pingResult = PingResult.fromPingResultDto(pingResultDto);
            pendingResults.offer(pingResult);

            log.debug("Added ping result to batch for monitor {}, queue size: {}",
                    pingResult.getMonitorId(), pendingResults.size());

        } catch (Exception e) {
            log.error("Error adding ping result to batch for monitor {}: {}",
                    pingResultDto.getMonitorId(), e.getMessage(), e);
        }
    }

    /**
     * Получить размер батча (вызывается из scheduler для статистики)
     */
    public int getBatchSize() {
        return pendingResults.size();
    }

    /**
     * Обработать накопленные результаты пингов (вызывается по расписанию)
     */
    @Scheduled(fixedRateString = "${ping.batch.process.interval:10000}")
    public void processPendingResults() {
        if (pendingResults.isEmpty()) {
            return;
        }

        List<PingResult> batch = new ArrayList<>();

        for (int i = 0; i < batchSize && !pendingResults.isEmpty(); i++) {
            PingResult result = pendingResults.poll();
            if (result != null) {
                batch.add(result);
            }
        }

        if (!batch.isEmpty()) {
            try {
                clickHouseService.savePingResultsBatchOptimized(batch);

                log.info("Processed batch of {} ping results (remaining: {})",
                        batch.size(), pendingResults.size());
                PingResult sample = batch.get(0);
                log.debug("Sample ping result: monitor={}, status={}, responseTime={} ms, url={}",
                        sample.getMonitorId(), sample.getStatus(), sample.getResponseTimeMs(), sample.getUrl());

            } catch (Exception e) {
                log.error("Error processing ping results batch: {}", e.getMessage(), e);
                batch.forEach(pendingResults::offer);
            }
        }
    }

}
