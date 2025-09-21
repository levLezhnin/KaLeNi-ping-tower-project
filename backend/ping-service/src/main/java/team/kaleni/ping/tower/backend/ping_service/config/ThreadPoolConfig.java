package team.kaleni.ping.tower.backend.ping_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class ThreadPoolConfig {

    @Value("${ping.thread-pool.core-size:10}")
    private int corePoolSize;

    @Value("${ping.thread-pool.max-size:20}")
    private int maxPoolSize;

    @Value("${ping.thread-pool.queue-capacity:100}")
    private int queueCapacity;

    @Bean("pingExecutorService")
    public ExecutorService pingExecutorService() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                r -> {
                    Thread thread = new Thread(r, "ping-worker-" + System.currentTimeMillis());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        log.info("Initialized ping executor service with core={}, max={}, queue={}",
                corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }
}

