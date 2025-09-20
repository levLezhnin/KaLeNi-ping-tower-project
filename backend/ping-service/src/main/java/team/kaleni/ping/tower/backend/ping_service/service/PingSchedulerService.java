package team.kaleni.ping.tower.backend.ping_service.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.ping_service.dto.MonitorConfigDto;
import team.kaleni.ping.tower.backend.ping_service.dto.PingResultDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PingSchedulerService {

    private final RedisMonitorService redisMonitorService;
    private final EnhancedPingService pingService;
    private final PingHistoryService pingHistoryService;

//    @Qualifier("pingExecutorService")
    private final ExecutorService executorService;

    @Value("${ping.batch.size:50}")
    private int batchSize;

    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicInteger currentlyProcessing = new AtomicInteger(0);

    /**
     * Основной планировщик - запускается каждые 5 секунд
     */
    @Scheduled(fixedRateString = "${ping.scheduler.interval:5000}")
    public void processPingQueue() {
        try {
            List<Long> monitorsToProcess = redisMonitorService.getMonitorsReadyForPing(batchSize);

            if (monitorsToProcess.isEmpty()) {
                log.debug("No monitors ready for ping");
                return;
            }

            log.info("Processing {} monitors from ping queue: [{}]", monitorsToProcess.size(),
                    monitorsToProcess.stream().map(Object::toString).collect(Collectors.joining(",")));

            // Помечаем как обрабатываемые
            redisMonitorService.markAsProcessing(monitorsToProcess);

            // Обрабатываем асинхронно
            processMonitorsBatch(monitorsToProcess);

        } catch (Exception e) {
            log.error("Error in ping scheduler: {}", e.getMessage(), e);
        }
    }

    /**
     * Обработка батча мониторов
     */
    private void processMonitorsBatch(List<Long> monitorIds) {
        currentlyProcessing.addAndGet(monitorIds.size());

        // Создаем задачи для каждого монитора
        List<CompletableFuture<Void>> futures = monitorIds.stream()
                .map(this::processMonitorAsync)
                .toList();

        // Ожидаем завершения всех задач
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((result, throwable) -> {
                    // Снимаем пометку об обработке
                    redisMonitorService.unmarkAsProcessing(monitorIds);
                    currentlyProcessing.addAndGet(-monitorIds.size());

                    if (throwable != null) {
                        log.error("Error processing monitor batch: {}", throwable.getMessage());
                    }
                });
    }

    /**
     * Асинхронная обработка одного монитора
     */
    private CompletableFuture<Void> processMonitorAsync(Long monitorId) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 1. Получаем конфигурацию монитора
                Optional<MonitorConfigDto> configOpt = redisMonitorService.getMonitorConfig(monitorId);

                if (configOpt.isEmpty()) {
                    log.warn("Monitor config not found for monitor {}, skipping", monitorId);
                    // Планируем повторную попытку через 5 минут
                    redisMonitorService.scheduleNextPing(monitorId, 300);
                    return;
                }

                MonitorConfigDto config = configOpt.get();
                // todo: check if monitor is active

                // 2. Выполняем пинг
                var pingResult = pingService.pingMonitor(config);

                // 3. Сохраняем результат в Redis
                redisMonitorService.updateMonitorStatus(
                        monitorId,
                        pingResult.getStatus(),
                        pingResult.getResponseTimeMs(),
                        pingResult.getResponseCode(),
                        pingResult.getErrorMessage()
                );

                // 4. Сохраняем в батч для ClickHouse
                pingHistoryService.addToBatch(pingResult);

                // 5. Планируем следующий пинг
                redisMonitorService.scheduleNextPing(monitorId, config.getIntervalSeconds());

                totalProcessed.incrementAndGet();

                log.debug("Successfully processed monitor {} with status {}",
                        monitorId, pingResult.getStatus());

            } catch (Exception e) {
                log.error("Error processing monitor {}: {}", monitorId, e.getMessage());

                // Планируем повторный пинг через минуту в случае ошибки
                redisMonitorService.scheduleNextPing(monitorId, 60);
            }
        }, executorService);
    }

    public PingResultDto executePing(MonitorConfigDto config) {
        log.debug("Executing ping for monitor {} to URL: {}", config.getMonitorId(), config.getUrl());

        try {
            return pingService.pingMonitor(config);
        } catch (Exception e) {
            log.error("Error executing ping for monitor {}: {}", config.getMonitorId(), e.getMessage());

            return PingResultDto.builder()
                    .monitorId(config.getMonitorId())
                    .status(team.kaleni.ping.tower.backend.ping_service.enums.PingStatus.ERROR)
                    .responseTimeMs(0)
                    .errorMessage("Internal error: " + e.getMessage())
                    .timestamp(java.time.Instant.now())
                    .url(config.getUrl())
                    .build();
        }
    }

    /**
     * Статистика планировщика каждые 30 секунд
     */
    @Scheduled(fixedRate = 30000)
    public void logStatistics() {
        try {
            Map<String, Object> queueStats = redisMonitorService.getQueueStats();
            int batchQueueSize = pingHistoryService.getBatchSize();

            log.info("Ping Scheduler Stats: processed={}, processing={}, queue={}, overdue={}, batch={}",
                    totalProcessed.get(),
                    currentlyProcessing.get(),
                    queueStats.get("totalInQueue"),
                    queueStats.get("overdueCount"),
                    batchQueueSize
            );
        } catch (Exception e) {
            log.error("Error logging statistics: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ping scheduler service...");

        try {
            executorService.shutdown();
            log.info("Ping scheduler service shutdown completed");
        } catch (Exception e) {
            log.error("Error during shutdown: {}", e.getMessage());
        }
    }
}
