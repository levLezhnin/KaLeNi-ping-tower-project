package team.kaleni.ping.tower.backend.ping_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.ping_service.dto.PingResultDto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class PingHistoryService {

    @Value("${ping.clickhouse.batch.size:1000}")
    private int batchSize;

    @Value("${ping.clickhouse.batch.timeout:60000}")
    private long batchTimeoutMs;

    private final ConcurrentLinkedQueue<PingResultDto> pendingResults = new ConcurrentLinkedQueue<>();
    private final AtomicLong totalBatched = new AtomicLong(0);
    private volatile long lastBatchTime = System.currentTimeMillis();

    /**
     * 🔥 Добавить результат пинга в батч
     */
    public void addToBatch(PingResultDto pingResult) {
        pendingResults.offer(pingResult);

        // Если достигли размера батча, обрабатываем немедленно
        if (pendingResults.size() >= batchSize) {
            processBatch();
        }
    }

    /**
     * ⏰ Периодическая обработка батчей (каждые 30 секунд)
     */
    @Scheduled(fixedRate = 30000)
    public void processScheduledBatch() {
        long currentTime = System.currentTimeMillis();

        // Обрабатываем если есть данные и прошел timeout
        if (!pendingResults.isEmpty() && (currentTime - lastBatchTime) > batchTimeoutMs) {
            processBatch();
        }
    }

    /**
     * 📊 Обработать накопленный батч
     */
    private synchronized void processBatch() {
        if (pendingResults.isEmpty()) {
            return;
        }

        List<PingResultDto> batch = new ArrayList<>();
        PingResultDto result;

        // Извлекаем все накопленные результаты
        while ((result = pendingResults.poll()) != null) {
            batch.add(result);
        }

        if (batch.isEmpty()) {
            return;
        }

        try {
            // TODO: Здесь будет отправка в ClickHouse
            // clickHouseService.saveBatch(batch);

            // Пока просто логируем
            log.info("Processed batch of {} ping results (total batched: {})",
                    batch.size(), totalBatched.addAndGet(batch.size()));

            // Логируем sample результатов для отладки
            if (log.isDebugEnabled() && !batch.isEmpty()) {
                PingResultDto sample = batch.get(0);
                log.debug("Sample ping result: monitor={}, status={}, responseTime={}ms, url={}",
                        sample.getMonitorId(), sample.getStatus(),
                        sample.getResponseTimeMs(), sample.getUrl());
            }

        } catch (Exception e) {
            log.error("Error processing ping results batch: {}", e.getMessage());

            // В случае ошибки возвращаем обратно в очередь
            pendingResults.addAll(batch);
        }

        lastBatchTime = System.currentTimeMillis();
    }

    /**
     * 📈 Получить размер текущего батча
     */
    public int getBatchSize() {
        return pendingResults.size();
    }

    /**
     * 📊 Получить статистику
     */
    public long getTotalBatched() {
        return totalBatched.get();
    }
}

