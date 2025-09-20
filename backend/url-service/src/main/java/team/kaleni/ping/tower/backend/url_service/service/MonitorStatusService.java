package team.kaleni.ping.tower.backend.url_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import team.kaleni.ping.tower.backend.url_service.dto.inner.MonitorStatusDTO;
import team.kaleni.ping.tower.backend.url_service.entity.PingStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorStatusService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STATUS_KEY_PREFIX = "monitor:status:";
    private static final String QUEUE_KEY = "ping:queue";

    public void updateStatus(Long monitorId, PingStatus status, Integer responseTimeMs,
                             Integer responseCode, String errorMessage) {

        String key = STATUS_KEY_PREFIX + monitorId;

        MonitorStatusDTO statusDto = MonitorStatusDTO.builder()
                .status(status)
                .lastCheckedAt(Instant.now())
                .responseTimeMs(responseTimeMs)
                .responseCode(responseCode)
                .errorMessage(errorMessage)
                .build();

        redisTemplate.opsForValue().set(key, statusDto, 7, TimeUnit.DAYS);
        log.debug("Updated status for monitor {}: {}", monitorId, status);
    }

    public Optional<MonitorStatusDTO> getStatus(Long monitorId) {
        String key = STATUS_KEY_PREFIX + monitorId;

        try {
            Object rawValue = redisTemplate.opsForValue().get(key);

            if (rawValue == null) {
                return Optional.empty();
            }

            // 🔥 Теперь всегда ожидаем Map
            if (rawValue instanceof java.util.Map) {
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) rawValue;

                MonitorStatusDTO statusDto = MonitorStatusDTO.builder()
                        .status(parseStatus(map.get("status")))
                        .lastCheckedAt(parseInstant(map.get("lastCheckedAt")))
                        .responseTimeMs(parseInteger(map.get("responseTimeMs")))
                        .responseCode(parseInteger(map.get("responseCode")))
                        .errorMessage(parseString(map.get("errorMessage")))
                        .build();

                return Optional.of(statusDto);
            } else {
                log.warn("Unexpected data format for monitor {} status: {}", monitorId, rawValue.getClass());
                return Optional.empty();
            }

        } catch (Exception e) {
            log.warn("Error reading status for monitor {}, returning empty: {}", monitorId, e.getMessage());
            return Optional.empty();
        }
    }


    public void addToPingQueue(Long monitorId, Instant nextPingTime) {
        redisTemplate.opsForZSet().add(QUEUE_KEY, monitorId.toString(), nextPingTime.getEpochSecond());
        log.debug("Added monitor {} to ping queue with next ping at {}", monitorId, nextPingTime);
    }

    public void removeFromPingQueue(Long monitorId) {
        redisTemplate.opsForZSet().remove(QUEUE_KEY, monitorId.toString());
        log.debug("Removed monitor {} from ping queue", monitorId);
    }

    public void initializeStatus(Long monitorId) {
        MonitorStatusDTO initialStatus = MonitorStatusDTO.builder()
                .status(PingStatus.UNKNOWN)
                .lastCheckedAt(null)
                .responseTimeMs(null)
                .responseCode(null)
                .errorMessage(null)
                .build();

        String key = STATUS_KEY_PREFIX + monitorId;
        redisTemplate.opsForValue().set(key, initialStatus, 7, TimeUnit.DAYS);
        log.info("Initialized status for monitor {}", monitorId);
    }

    // Helper methods
    private PingStatus parseStatus(Object obj) {
        if (obj == null) return PingStatus.UNKNOWN;

        String statusStr = obj.toString();
        try {
            return PingStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown PingStatus: {}", statusStr);
            return PingStatus.UNKNOWN;
        }
    }

    private Instant parseInstant(Object obj) {
        if (obj == null) return null;

        try {
            return Instant.parse(obj.toString());
        } catch (Exception e) {
            log.warn("Failed to parse Instant: {}", obj);
            return null;
        }
    }

    private Integer parseInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String parseString(Object obj) {
        return obj != null ? obj.toString() : null;
    }
}
