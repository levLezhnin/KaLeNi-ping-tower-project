package team.kaleni.ping.tower.backend.url_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.kaleni.ping.tower.backend.url_service.dto.inner.PingResultDTO;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;
import team.kaleni.ping.tower.backend.url_service.entity.PingRecord;
import team.kaleni.ping.tower.backend.url_service.entity.PingStatus;
import team.kaleni.ping.tower.backend.url_service.entity.TargetUrl;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorRepository;
import team.kaleni.ping.tower.backend.url_service.repository.PingRecordRepository;
import team.kaleni.ping.tower.backend.url_service.repository.TargetUrlRepository;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PingExecutorService {

    private final EnhancedPingService pingService;
    private final PingRecordRepository pingRecordRepository;
    private final MonitorRepository monitorRepository;
    private final TargetUrlRepository targetUrlRepository;

    @Transactional
    public void executePingForMonitor(Monitor monitor) {
        TargetUrl target = monitor.getTarget();
        Instant now = Instant.now();
        Instant scheduledTime = monitor.getNextPingAt();
        if (scheduledTime == null){
            scheduledTime = now;
        }
        log.debug("Executing ping for monitor {} ({})", monitor.getId(), target.getUrl());
        try {
            // Determine if we should perform actual ping or use cached result
            boolean shouldActuallyPing = shouldPerformActualPing(target, now);
            PingResultDTO result;
            if (shouldActuallyPing) {
                // Perform actual ping
                result = pingService.pingURL(target.getUrl(), monitor.getTimeoutMs());
                // Update target with latest status
                updateTargetStatus(target, result, now);
                log.debug("Actual ping executed for target {} - Status: {}, Response time: {}ms",
                        target.getId(), result.getStatus(), result.getResponseTimeMs());
            } else {
                // Use cached result from target
                result = createCachedResult(target);
                log.debug("Using cached result for target {} - Status: {}",
                        target.getId(), result.getStatus());
            }
            // Save detailed ping record
            savePingRecord(monitor, target, scheduledTime, now, result);
            // Update monitor's next ping time using the last scheduled not the current time
            // Use of current time will make little shifts
            Instant nextPingTime = scheduledTime.plusSeconds(monitor.getIntervalSeconds());
            monitor.setNextPingAt(nextPingTime);
            monitorRepository.save(monitor);
        } catch (Exception e) {
            log.error("Error executing ping for monitor {}: {}", monitor.getId(), e.getMessage(), e);
            // Save error record
            PingResultDTO errorResult = PingResultDTO.builder()
                    .status(PingStatus.ERROR)
                    .errorMessage("Internal ping service error: " + e.getMessage())
                    .responseTimeMs(0)
                    .build();
            savePingRecord(monitor, target, scheduledTime, now, errorResult);
            // Still update next ping time to avoid getting stuck
            monitor.setNextPingAt(now.plusSeconds(monitor.getIntervalSeconds()));
            monitorRepository.save(monitor);
        }
    }

    private boolean shouldPerformActualPing(TargetUrl target, Instant now) {
        if (target.getLastCheckedAt() == null) {
            return true; // Never pinged before
        }
        Duration timeSinceLastPing = Duration.between(target.getLastCheckedAt(), now);
        return timeSinceLastPing.getSeconds() >= 30; // 30-second caching rule
    }

    private PingResultDTO createCachedResult(TargetUrl target) {
        return PingResultDTO.builder()
                .status(target.getLastStatus() != null ? target.getLastStatus() : PingStatus.UNKNOWN)
                .responseTimeMs(target.getLastResponseTimeMs())
                .fromCache(true)
                .build();
    }

    private void updateTargetStatus(TargetUrl target, PingResultDTO result, Instant now) {
        PingStatus oldStatus = target.getLastStatus();
        PingStatus newStatus = result.getStatus();
        target.setLastStatus(newStatus);
        target.setLastCheckedAt(now);
        target.setLastResponseTimeMs(result.getResponseTimeMs());
        targetUrlRepository.save(target);
        // Log status changes for monitoring
        if (oldStatus != null && !oldStatus.equals(newStatus)) {
            log.info("Status change detected for target {} ({}): {} -> {}",
                    target.getId(), target.getUrl(), oldStatus, newStatus);
        }
    }

    private void savePingRecord(Monitor monitor, TargetUrl target, Instant scheduledTime,
                                Instant actualPingTime, PingResultDTO result) {
        PingRecord record = PingRecord.builder()
                .monitorId(monitor.getId())
                .targetId(target.getId())
                .scheduledAt(scheduledTime)
                .actualPingAt(actualPingTime)
                .status(result.getStatus())
                .responseCode(result.getResponseCode())
                .responseTimeMs(result.getResponseTimeMs())
                .errorMessage(result.getErrorMessage())
                .usedCachedResult(result.isFromCache())
                .metadata(result.getMetadata())
                .build();
        pingRecordRepository.save(record);
        log.debug("Ping record saved: Monitor {}, Status {}, Cached: {}",
                monitor.getId(), result.getStatus(), result.isFromCache());
    }
}
