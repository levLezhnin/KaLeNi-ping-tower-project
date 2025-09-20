package team.kaleni.ping.tower.backend.url_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.kaleni.ping.tower.backend.url_service.dto.inner.PingResultDTO;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;
import team.kaleni.ping.tower.backend.url_service.entity.PingRecord;
import team.kaleni.ping.tower.backend.url_service.entity.PingStatus;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorRepository;
import team.kaleni.ping.tower.backend.url_service.repository.PingRecordRepository;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PingExecutorService {

    private final EnhancedPingService pingService;
    private final PingRecordRepository pingRecordRepository;
    private final MonitorRepository monitorRepository;

//    @Transactional
//    public void executePingForMonitor(Monitor monitor) {
//        Instant now = Instant.now();
////        Instant scheduledTime = monitor.getNextPingAt();
//        Instant scheduledTime = Instant.now();
//
//        if (scheduledTime == null) {
//            scheduledTime = now;
//        }
//
//        log.debug("Executing ping for monitor {} ({})", monitor.getId(), monitor.getUrl());
//
//        try {
//            // Determine if we should perform actual ping or use cached result from monitor
//            boolean shouldActuallyPing = shouldPerformActualPing(monitor, now);
//            PingResultDTO result;
//
//            if (shouldActuallyPing) {
//                // Perform actual ping using monitor's full configuration
//                result = pingService.pingMonitor(monitor);
//
//                // Update monitor with latest status
//                updateMonitorStatus(monitor, result, now);
//
//                log.debug("Actual ping executed for monitor {} - Status: {}, Response time: {}ms",
//                        monitor.getId(), result.getStatus(), result.getResponseTimeMs());
//            } else {
//                // Use cached result from monitor's last status
//                result = createCachedResult(monitor);
//
//                log.debug("Using cached result for monitor {} - Status: {}",
//                        monitor.getId(), result.getStatus());
//            }
//
//            // Save detailed ping record
//            savePingRecord(monitor, scheduledTime, now, result);
//
//            // Update monitor's next ping time using the last scheduled time to prevent drift
//            Instant nextPingTime = scheduledTime.plusSeconds(monitor.getIntervalSeconds());
//            monitor.setNextPingAt(nextPingTime);
//            monitorRepository.save(monitor);
//
//        } catch (Exception e) {
//            log.error("Error executing ping for monitor {}: {}", monitor.getId(), e.getMessage(), e);
//
//            // Save error record
//            PingResultDTO errorResult = PingResultDTO.builder()
//                    .status(PingStatus.ERROR)
//                    .errorMessage("Internal ping service error: " + e.getMessage())
//                    .responseTimeMs(0)
//                    .build();
//
//            savePingRecord(monitor, scheduledTime, now, errorResult);
//
//            // Still update next ping time to avoid getting stuck
//            monitor.setNextPingAt(now.plusSeconds(monitor.getIntervalSeconds()));
//            monitorRepository.save(monitor);
//        }
//    }
//
//    /**
//     * 30-second caching rule: check if monitor was pinged recently
//     * Now based on monitor's lastCheckedAt instead of separate target table
//     */
//    private boolean shouldPerformActualPing(Monitor monitor, Instant now) {
//        if (monitor.getLastCheckedAt() == null) {
//            return true; // Never pinged before
//        }
//
//        Duration timeSinceLastPing = Duration.between(monitor.getLastCheckedAt(), now);
//        return timeSinceLastPing.getSeconds() >= 30; // 30-second caching rule
//    }
//
//    /**
//     * Create cached result from monitor's last known status
//     */
//    private PingResultDTO createCachedResult(Monitor monitor) {
//        return PingResultDTO.builder()
//                .status(monitor.getLastStatus() != null ? monitor.getLastStatus() : PingStatus.UNKNOWN)
//                .responseTimeMs(monitor.getLastResponseTimeMs())
//                .responseCode(monitor.getLastResponseCode())
//                .fromCache(true)
//                .build();
//    }
//
//    /**
//     * Update monitor's status fields with ping results
//     */
//    private void updateMonitorStatus(Monitor monitor, PingResultDTO result, Instant now) {
//        PingStatus oldStatus = monitor.getLastStatus();
//        PingStatus newStatus = result.getStatus();
//
//        monitor.setLastStatus(newStatus);
//        monitor.setLastCheckedAt(now);
//        monitor.setLastResponseTimeMs(result.getResponseTimeMs());
//        monitor.setLastResponseCode(result.getResponseCode());
//        monitor.setLastErrorMessage(result.getErrorMessage());
//
//        // We'll save the monitor in the calling method, so don't save here
//
//        // Log status changes for monitoring
//        if (oldStatus != null && !oldStatus.equals(newStatus)) {
//            log.info("Status change detected for monitor {} ({}): {} -> {}",
//                    monitor.getId(), monitor.getUrl(), oldStatus, newStatus);
//        }
//    }
//
//    /**
//     * Save ping record with monitor information
//     */
//    private void savePingRecord(Monitor monitor, Instant scheduledTime,
//                                Instant actualPingTime, PingResultDTO result) {
//        PingRecord record = PingRecord.builder()
//                .monitorId(monitor.getId())
//                .scheduledAt(scheduledTime)
//                .actualPingAt(actualPingTime)
//                .status(result.getStatus())
//                .responseCode(result.getResponseCode())
//                .responseTimeMs(result.getResponseTimeMs())
//                .errorMessage(result.getErrorMessage())
//                .usedCachedResult(result.isFromCache())
//                .metadata(result.getMetadata())
//                // Store request details for debugging
//                .requestMethod(monitor.getMethod())
//                .requestUrl(monitor.getUrl())
//                .build();
//
//        pingRecordRepository.save(record);
//
//        log.debug("Ping record saved: Monitor {}, Status {}, Cached: {}",
//                monitor.getId(), result.getStatus(), result.isFromCache());
//    }
}
