package team.kaleni.ping.tower.backend.url_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorConfigService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CONFIG_KEY_PREFIX = "monitor:config:";

    /**
     * Сохранить конфигурацию монитора в Redis для Ping Service
     */
    public void saveMonitorConfig(Monitor monitor) {
        String configKey = CONFIG_KEY_PREFIX + monitor.getId();

        Map<String, Object> config = new HashMap<>();
        config.put("monitorId", monitor.getId());
        config.put("name", monitor.getName());
        config.put("url", monitor.getUrl());
        config.put("method", monitor.getMethod().name());
        config.put("headers", monitor.getHeaders());
        config.put("requestBody", monitor.getRequestBody());
        config.put("contentType", monitor.getContentType());
        config.put("timeoutMs", monitor.getTimeoutMs());
        config.put("intervalSeconds", monitor.getIntervalSeconds());
        config.put("enabled", monitor.getEnabled());

        try {
            redisTemplate.opsForValue().set(configKey, config, 30, TimeUnit.DAYS);
            log.debug("Saved config for monitor {} to Redis", monitor.getId());
        } catch (Exception e) {
            log.error("Error saving config for monitor {}: {}", monitor.getId(), e.getMessage());
        }
    }

    /**
     * Удалить конфигурацию монитора из Redis
     */
    public void deleteMonitorConfig(Long monitorId) {
        String configKey = CONFIG_KEY_PREFIX + monitorId;

        try {
            redisTemplate.delete(configKey);
            log.debug("Deleted config for monitor {} from Redis", monitorId);
        } catch (Exception e) {
            log.error("Error deleting config for monitor {}: {}", monitorId, e.getMessage());
        }
    }

    /**
     * Обновить конфигурацию монитора в Redis
     */
    public void updateMonitorConfig(Monitor monitor) {
        // Просто перезаписываем конфигурацию
        saveMonitorConfig(monitor);
    }
}

