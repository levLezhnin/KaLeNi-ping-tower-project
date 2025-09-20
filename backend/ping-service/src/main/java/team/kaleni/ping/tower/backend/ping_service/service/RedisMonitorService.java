package team.kaleni.ping.tower.backend.ping_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.ping_service.dto.MonitorConfigDto;
import team.kaleni.ping.tower.backend.ping_service.dto.MonitorStatusDto;
import team.kaleni.ping.tower.backend.ping_service.enums.HttpMethod;
import team.kaleni.ping.tower.backend.ping_service.enums.PingStatus;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisMonitorService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PING_QUEUE_KEY = "ping:queue";
    private static final String STATUS_KEY_PREFIX = "monitor:status:";
    private static final String CONFIG_KEY_PREFIX = "monitor:config:";
    private static final String PROCESSING_SET_KEY = "ping:processing";

    /**
     * Получить мониторы готовые к пингу из sorted set
     */
    public List<Long> getMonitorsReadyForPing(int batchSize) {
        long currentTime = Instant.now().getEpochSecond();

        // Получаем мониторы с score <= текущего времени
        Set<Object> rawMonitorIds = redisTemplate.opsForZSet()
                .rangeByScore(PING_QUEUE_KEY, 0, currentTime, 0, batchSize);

        if (rawMonitorIds == null || rawMonitorIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> monitorIds = rawMonitorIds.stream()
                .map(obj -> Long.valueOf(obj.toString()))
                .collect(Collectors.toList());

        log.debug("Found {} monitors ready for ping", monitorIds.size());
        return monitorIds;
    }

    /**
     * Получить конфигурацию монитора из URL Service через Redis
     * Если конфигурации нет в Redis, возвращаем mock для тестирования
     */
    public Optional<MonitorConfigDto> getMonitorConfig(Long monitorId) {
        String configKey = CONFIG_KEY_PREFIX + monitorId;

        try {
            Object rawConfig = redisTemplate.opsForValue().get(configKey);

            if (rawConfig == null) {
                log.debug("Monitor config not found in Redis for monitor {}, creating mock config", monitorId);
                return createMockConfig(monitorId);
            }

            if (rawConfig instanceof MonitorConfigDto) {
                return Optional.of((MonitorConfigDto) rawConfig);
            } else if (rawConfig instanceof Map) {
                MonitorConfigDto config = mapToMonitorConfig((Map<String, Object>) rawConfig, monitorId);
                return Optional.of(config);
            } else {
                log.warn("Unexpected config format for monitor {}: {}", monitorId, rawConfig.getClass());
                return createMockConfig(monitorId);
            }

        } catch (Exception e) {
            log.error("Error getting config for monitor {}: {}", monitorId, e.getMessage());
            return createMockConfig(monitorId);
        }
    }

    /**
     * Сохранить результат пинга в Redis
     */
    public void updateMonitorStatus(Long monitorId, PingStatus status, Integer responseTimeMs,
                                    Integer responseCode, String errorMessage) {

        String statusKey = STATUS_KEY_PREFIX + monitorId;

        MonitorStatusDto statusDto = MonitorStatusDto.builder()
                .status(status)
                .lastCheckedAt(Instant.now())
                .responseTimeMs(responseTimeMs)
                .responseCode(responseCode)
                .errorMessage(errorMessage)
                .build();

        try {
            redisTemplate.opsForValue().set(statusKey, statusDto, 7, TimeUnit.DAYS);
            log.debug("Updated status for monitor {}: {}", monitorId, status);
        } catch (Exception e) {
            log.error("Error updating status for monitor {}: {}", monitorId, e.getMessage());
        }
    }

    /**
     * Запланировать следующий пинг монитора
     */
    public void scheduleNextPing(Long monitorId, int intervalSeconds) {
        long nextPingTime = Instant.now().plusSeconds(intervalSeconds).getEpochSecond();

        try {
            redisTemplate.opsForZSet().add(PING_QUEUE_KEY, monitorId.toString(), nextPingTime);
            log.debug("Scheduled next ping for monitor {} at epoch {}", monitorId, nextPingTime);
        } catch (Exception e) {
            log.error("Error scheduling next ping for monitor {}: {}", monitorId, e.getMessage());
        }
    }

    /**
     * Удалить монитор из очереди пингов
     */
    public void removeFromPingQueue(Long monitorId) {
        try {
            redisTemplate.opsForZSet().remove(PING_QUEUE_KEY, monitorId.toString());
            log.debug("Removed monitor {} from ping queue", monitorId);
        } catch (Exception e) {
            log.error("Error removing monitor {} from ping queue: {}", monitorId, e.getMessage());
        }
    }

    /**
     * Пометить мониторы как обрабатываемые (для предотвращения дублирования)
     */
    public void markAsProcessing(List<Long> monitorIds) {
        if (monitorIds.isEmpty()) return;

        try {
            String[] ids = monitorIds.stream().map(String::valueOf).toArray(String[]::new);
            redisTemplate.opsForSet().add(PROCESSING_SET_KEY, (Object[]) ids);
            redisTemplate.expire(PROCESSING_SET_KEY, 5, TimeUnit.MINUTES);
            log.debug("Marked {} monitors as processing", monitorIds.size());
        } catch (Exception e) {
            log.error("Error marking monitors as processing: {}", e.getMessage());
        }
    }

    /**
     * Убрать пометку об обработке
     */
    public void unmarkAsProcessing(List<Long> monitorIds) {
        if (monitorIds.isEmpty()) return;

        try {
            String[] ids = monitorIds.stream().map(String::valueOf).toArray(String[]::new);
            redisTemplate.opsForSet().remove(PROCESSING_SET_KEY, (Object[]) ids);
            log.debug("Unmarked {} monitors as processing", monitorIds.size());
        } catch (Exception e) {
            log.error("Error unmarking monitors as processing: {}", e.getMessage());
        }
    }

    /**
     * Проверить статистику очереди пингов
     */
    public Map<String, Object> getQueueStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            Long totalInQueue = redisTemplate.opsForZSet().zCard(PING_QUEUE_KEY);
            Long overdue = redisTemplate.opsForZSet().count(PING_QUEUE_KEY, 0, Instant.now().getEpochSecond());
            Long processing = redisTemplate.opsForSet().size(PROCESSING_SET_KEY);

            stats.put("totalInQueue", totalInQueue);
            stats.put("overdueCount", overdue);
            stats.put("processingCount", processing);
            stats.put("timestamp", Instant.now());

        } catch (Exception e) {
            log.error("Error getting queue stats: {}", e.getMessage());
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    // Helper methods

    private Optional<MonitorConfigDto> createMockConfig(Long monitorId) {
        // Создаем mock конфигурацию для тестирования
        MonitorConfigDto mockConfig = MonitorConfigDto.builder()
                .monitorId(monitorId)
                .url("https://httpbin.org/status/200")
                .method(HttpMethod.GET)
                .timeoutMs(10000)
                .intervalSeconds(300)
                .headers(Map.of("User-Agent", "PingTowerBot/1.0"))
                .build();

        log.debug("Created mock config for monitor {}", monitorId);
        return Optional.of(mockConfig);
    }

    private MonitorConfigDto mapToMonitorConfig(Map<String, Object> map, Long monitorId) {
        return MonitorConfigDto.builder()
                .monitorId(monitorId)
                .url(parseString(map.get("url")))
                .method(parseHttpMethod(map.get("method")))
                .headers(parseHeaders(map.get("headers")))
                .requestBody(parseString(map.get("requestBody")))
                .contentType(parseString(map.get("contentType")))
                .timeoutMs(parseInteger(map.get("timeoutMs"), 10000))
                .intervalSeconds(parseInteger(map.get("intervalSeconds"), 300))
                .build();
    }

    private String parseString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private HttpMethod parseHttpMethod(Object obj) {
        if (obj == null) return HttpMethod.GET;
        try {
            return HttpMethod.valueOf(obj.toString().toUpperCase());
        } catch (Exception e) {
            return HttpMethod.GET;
        }
    }

    private Integer parseInteger(Object obj, int defaultValue) {
        if (obj == null) return defaultValue;
        if (obj instanceof Integer) return (Integer) obj;
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseHeaders(Object obj) {
        if (obj instanceof Map) {
            Map<String, String> headers = new HashMap<>();
            ((Map<String, Object>) obj).forEach((key, value) ->
                    headers.put(key, value != null ? value.toString() : null));
            return headers;
        }
        return null;
    }
}

