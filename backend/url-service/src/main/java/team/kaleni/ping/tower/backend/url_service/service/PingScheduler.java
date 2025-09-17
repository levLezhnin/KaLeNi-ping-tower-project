package team.kaleni.ping.tower.backend.url_service.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorRepository;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class PingScheduler {

    private final MonitorRepository monitorRepository;
    private final PingExecutorService pingExecutorService;

    @Value("${ping.scheduler.thread-pool-size:10}")
    private int threadPoolSize;

    @Value("${ping.scheduler.max-queue-size:1000}")
    private int maxQueueSize;

    @Value("${ping.scheduler.batch-size:100}")
    private int batchSize;

    private ExecutorService executorService;
    private final AtomicInteger activeTasks = new AtomicInteger(0);

    @PostConstruct
    public void initialize() {
        // Create bounded thread pool with custom thread factory
        this.executorService = new ThreadPoolExecutor(
                threadPoolSize,
                threadPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(maxQueueSize),
                r -> {
                    Thread thread = new Thread(r, "ping-executor-" + Thread.currentThread().getId());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // Handle overflow by running in caller thread
        );
        log.info("Ping scheduler initialized with {} threads, max queue size: {}",
                threadPoolSize, maxQueueSize);
    }

    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    public void executePingCycle() {
        log.info("Start new Ping Cycle");
        try {
            Instant now = Instant.now();
            // Process monitors in batches to avoid memory issues
            List<Monitor> monitors = monitorRepository.findMonitorsReadyForPingWithLock(now);
            if (monitors.isEmpty()) {
                log.info("No monitors ready for ping");
                return;
            }
            log.info("Processing {} monitors for ping", monitors.size());
            // Submit all ping tasks concurrently
            List<CompletableFuture<Void>> futures = monitors.stream()
                    .map(this::submitPingTask)
                    .toList();
            // Wait for all tasks to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(30, TimeUnit.SECONDS) // Prevent hanging
                    .join();
            log.info("Completed processing {} monitors in ping cycle. Active tasks: {}",
                    monitors.size(), activeTasks.get());
        } catch (Exception e) {
            log.error("Error in ping cycle execution", e);
        }
    }

    private CompletableFuture<Void> submitPingTask(Monitor monitor) {
        activeTasks.incrementAndGet();
        return CompletableFuture.runAsync(() -> {
            try {
                pingExecutorService.executePingForMonitor(monitor);
            } catch (Exception e) {
                log.error("Error executing ping for monitor {}: {}", monitor.getId(), e.getMessage());
            } finally {
                activeTasks.decrementAndGet();
            }
        }, executorService);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ping scheduler...");
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    log.warn("Executor did not terminate gracefully, forcing shutdown");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("Ping scheduler shutdown complete");
    }

    public int getActiveTasks() {
        return activeTasks.get();
    }
}

