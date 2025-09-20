package team.kaleni.ping.tower.backend.url_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.kaleni.ping.tower.backend.url_service.dto.inner.MonitorStatusDTO;
import team.kaleni.ping.tower.backend.url_service.dto.inner.PingResultDTO;
import team.kaleni.ping.tower.backend.url_service.dto.request.monitor.CreateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.request.monitor.UpdateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.DeleteResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorDetailResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorResponse;
import team.kaleni.ping.tower.backend.url_service.entity.HttpMethod;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;
import team.kaleni.ping.tower.backend.url_service.entity.MonitorGroup;
import team.kaleni.ping.tower.backend.url_service.entity.PingStatus;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorGroupRepository;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final MonitorGroupRepository monitorGroupRepository;
    private final EnhancedPingService pingService;
    private final MonitorStatusService monitorStatusService;

    @Transactional
    public MonitorResponse createMonitor(Integer ownerId, CreateMonitorRequest req) {
        log.info("Creating monitor for owner {} with URL: {}", ownerId, req.getUrl());

        try {
            // 1) Validate unique monitor name per owner
            Optional<Monitor> existingByName = monitorRepository.findByOwnerIdAndName(ownerId, req.getName());
            if (existingByName.isPresent()) {
                return MonitorResponse.builder()
                        .result(false)
                        .errorMessage("Monitor with the same name already exists for this owner")
                        .build();
            }

            HttpMethod method = req.getMethod() != null ? HttpMethod.valueOf(req.getMethod()) : HttpMethod.GET;

            // 2) Build Monitor entity (ТОЛЬКО СТАТИЧНАЯ КОНФИГУРАЦИЯ)
            Monitor monitor = Monitor.builder()
                    .name(req.getName())
                    .description(req.getDescription())
                    .ownerId(ownerId)
                    .url(req.getUrl())
                    .method(method)
                    .headers(req.getHeaders())
                    .requestBody(req.getRequestBody())
                    .contentType(req.getContentType())
                    .intervalSeconds(req.getIntervalSeconds())
                    .timeoutMs(req.getTimeoutMs() != null ? req.getTimeoutMs() : 10000)
                    .enabled(true)
                    .build();

            // 3) Optional group assignment
            if (req.getGroupId() != null) {
                MonitorGroup group = monitorGroupRepository.findByIdAndOwnerId(req.getGroupId(), ownerId)
                        .orElseThrow(() -> new IllegalArgumentException("Group not found or not owned by user"));
                monitor.setGroup(group);
            }

            // 4) Test monitor configuration before saving
            if (!testMonitorConfiguration(monitor)) {
                log.error("Monitor configuration test failed for URL: {}", req.getUrl());
                return MonitorResponse.builder()
                        .result(false)
                        .url(req.getUrl())
                        .errorMessage("Failed to connect to the specified URL with given configuration")
                        .build();
            }

            // 5) Save monitor to PostgreSQL
            Monitor saved = monitorRepository.save(monitor);
            log.info("Monitor {} created successfully in PostgreSQL for owner {}", saved.getId(), ownerId);

            // 6) Initialize in Redis
            monitorStatusService.initializeStatus(saved.getId());

            // 7) Add to ping queue (first ping in 30 seconds)
            Instant firstPing = Instant.now().plusSeconds(30);
            monitorStatusService.addToPingQueue(saved.getId(), firstPing);

            log.info("Monitor {} initialized in Redis with first ping at {}", saved.getId(), firstPing);

            // 8) Return success response
            return MonitorResponse.builder()
                    .result(true)
                    .id(saved.getId())
                    .name(saved.getName())
                    .url(saved.getUrl())
                    .method(saved.getMethod())
                    .intervalSeconds(saved.getIntervalSeconds())
                    .groupId(saved.getGroup() != null ? saved.getGroup().getId() : null)
                    .enabled(saved.getEnabled())
                    .build();

        } catch (Exception e) {
            log.error("Error creating monitor for owner {}: {}", ownerId, e.getMessage());
            return MonitorResponse.builder()
                    .result(false)
                    .errorMessage("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    // 🔥 1. getAllMonitors - читаем из PostgreSQL + Redis
    public List<MonitorDetailResponse> getAllMonitors(Integer ownerId) {
        List<Monitor> monitors = monitorRepository.findByOwnerId(ownerId);

        return monitors.stream()
                .map(monitor -> {
                    // Get status from Redis for each monitor
                    Optional<MonitorStatusDTO> statusOpt = monitorStatusService.getStatus(monitor.getId());
                    return mapToDetailResponse(monitor, statusOpt.orElse(null));
                })
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    public MonitorDetailResponse getMonitorById(Integer ownerId, Long monitorId) {
        // 1) Get static config from PostgreSQL
        Monitor monitor = monitorRepository.findByIdAndOwnerId(monitorId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found or not owned by user"));

        // 2) Get dynamic status from Redis
        Optional<MonitorStatusDTO> statusOpt = monitorStatusService.getStatus(monitorId);

        return mapToDetailResponse(monitor, statusOpt.orElse(null));
    }

    // 🔥 2. updateMonitor - обновляем PostgreSQL + перезапускаем в Redis
    @Transactional
    public MonitorDetailResponse updateMonitor(Integer ownerId, Long monitorId, UpdateMonitorRequest req) {
        // Find existing monitor and verify ownership
        Monitor existingMonitor = monitorRepository.findByIdAndOwnerId(monitorId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found or not owned by user"));

        boolean needsPingTest = false;
        boolean intervalChanged = false;

        // Update basic fields
        if (req.getName() != null && !req.getName().equals(existingMonitor.getName())) {
            // Check name uniqueness
            Optional<Monitor> existingByName = monitorRepository.findByOwnerIdAndName(ownerId, req.getName());
            if (existingByName.isPresent() && !existingByName.get().getId().equals(monitorId)) {
                throw new IllegalArgumentException("Monitor with this name already exists");
            }
            existingMonitor.setName(req.getName());
        }

        if (req.getDescription() != null) {
            existingMonitor.setDescription(req.getDescription());
        }

        // Update HTTP configuration (these require ping test)
        if (req.getUrl() != null && !req.getUrl().equals(existingMonitor.getUrl())) {
            existingMonitor.setUrl(req.getUrl());
            needsPingTest = true;
        }

        if (req.getMethod() != null && !req.getMethod().equals(existingMonitor.getMethod())) {
            existingMonitor.setMethod(req.getMethod());
            needsPingTest = true;
        }

        if (req.getHeaders() != null) {
            existingMonitor.setHeaders(req.getHeaders());
            needsPingTest = true;
        }

        if (req.getRequestBody() != null) {
            existingMonitor.setRequestBody(req.getRequestBody());
            needsPingTest = true;
        }

        if (req.getContentType() != null) {
            existingMonitor.setContentType(req.getContentType());
            needsPingTest = true;
        }

        // Update monitoring configuration
        if (req.getIntervalSeconds() != null && !req.getIntervalSeconds().equals(existingMonitor.getIntervalSeconds())) {
            existingMonitor.setIntervalSeconds(req.getIntervalSeconds());
            intervalChanged = true;
        }

        if (req.getTimeoutMs() != null && !req.getTimeoutMs().equals(existingMonitor.getTimeoutMs())) {
            existingMonitor.setTimeoutMs(req.getTimeoutMs());
            needsPingTest = true; // Timeout change might affect connectivity
        }

        if (req.getEnabled() != null && !req.getEnabled().equals(existingMonitor.getEnabled())) {
            existingMonitor.setEnabled(req.getEnabled());

            // Handle enable/disable in Redis
            if (!req.getEnabled()) {
                // Remove from ping queue if disabled
                monitorStatusService.removeFromPingQueue(monitorId);
                log.info("Monitor {} disabled and removed from ping queue", monitorId);
            } else {
                // Add to ping queue if enabled
                Instant nextPing = Instant.now().plusSeconds(30);
                monitorStatusService.addToPingQueue(monitorId, nextPing);
                log.info("Monitor {} enabled and added to ping queue", monitorId);
            }
        }

        // Update group assignment
        if (req.getGroupId() != null) {
            if (req.getGroupId() == 0) {
                // Remove from group
                existingMonitor.setGroup(null);
            } else {
                MonitorGroup group = monitorGroupRepository.findByIdAndOwnerId(req.getGroupId(), ownerId)
                        .orElseThrow(() -> new IllegalArgumentException("Group not found or not owned by user"));
                existingMonitor.setGroup(group);
            }
        }

        // Test configuration if HTTP settings changed
        if (needsPingTest && existingMonitor.getEnabled()) {
            if (!testMonitorConfiguration(existingMonitor)) {
                log.warn("Monitor configuration test failed after update for monitor {}", monitorId);
                // Don't fail the update, just log warning
            }
        }

        // Save to PostgreSQL
        Monitor savedMonitor = monitorRepository.save(existingMonitor);

        // Update ping schedule if interval changed and monitor is enabled
        if (intervalChanged && savedMonitor.getEnabled()) {
            // Remove old schedule and add new one
            monitorStatusService.removeFromPingQueue(monitorId);
            Instant nextPing = Instant.now().plusSeconds(savedMonitor.getIntervalSeconds());
            monitorStatusService.addToPingQueue(monitorId, nextPing);
            log.info("Monitor {} ping schedule updated with new interval: {} seconds", monitorId, savedMonitor.getIntervalSeconds());
        }

        log.info("Monitor {} updated successfully for owner {}", monitorId, ownerId);

        // Get updated status from Redis
        Optional<MonitorStatusDTO> statusOpt = monitorStatusService.getStatus(monitorId);
        return mapToDetailResponse(savedMonitor, statusOpt.orElse(null));
    }

    @Transactional
    public DeleteResponse deleteMonitor(Integer ownerId, Long monitorId) {
        Monitor monitor = monitorRepository.findByIdAndOwnerId(monitorId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found or not owned by user"));

        String monitorName = monitor.getName();

        // Delete from PostgreSQL
        monitorRepository.delete(monitor);

        // Remove from Redis
        monitorStatusService.removeFromPingQueue(monitorId);
        // Status will expire naturally (7 days TTL)

        log.info("Monitor {} deleted from both PostgreSQL and Redis for owner {}", monitorId, ownerId);

        return DeleteResponse.builder()
                .success(true)
                .deletedId(monitorId)
                .deletedName(monitorName)
                .deletedAt(Instant.now())
                .deletedRecords(0)
                .message("Monitor successfully deleted")
                .build();
    }

    @Transactional
    public void setMonitorEnabled(Integer ownerId, Long monitorId, boolean enabled) {
        Monitor monitor = monitorRepository.findByIdAndOwnerId(monitorId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found or not owned by user"));

        monitor.setEnabled(enabled);
        monitorRepository.save(monitor);

        if (enabled) {
            // Add to ping queue
            Instant nextPing = Instant.now().plusSeconds(30);
            monitorStatusService.addToPingQueue(monitorId, nextPing);
        } else {
            // Remove from ping queue
            monitorStatusService.removeFromPingQueue(monitorId);
        }

        log.info("Monitor {} {} for owner {}", monitorId, enabled ? "enabled" : "disabled", ownerId);
    }

    // 3. testMonitorConfiguration - проверка через PingService
    private boolean testMonitorConfiguration(Monitor monitor) {
        try {
            log.debug("Testing monitor configuration for URL: {}", monitor.getUrl());

            // Use PingService to perform actual test ping
            PingResultDTO result = pingService.pingMonitor(monitor);

            if (result.getStatus() == PingStatus.UP) {
                log.debug("Monitor configuration test successful for URL: {} - Response time: {}ms",
                        monitor.getUrl(), result.getResponseTimeMs());
                return true;
            } else {
                log.warn("Monitor configuration test failed for URL: {} - Status: {}, Error: {}",
                        monitor.getUrl(), result.getStatus(), result.getErrorMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("Error testing monitor configuration for URL {}: {}", monitor.getUrl(), e.getMessage());
            return false;
        }
    }

    // Updated mapping method with Redis status
    private MonitorDetailResponse mapToDetailResponse(Monitor monitor, MonitorStatusDTO status) {
        MonitorGroup group = monitor.getGroup();

        MonitorDetailResponse.MonitorDetailResponseBuilder builder = MonitorDetailResponse.builder()
                .id(monitor.getId())
                .name(monitor.getName())
                .description(monitor.getDescription())
                .url(monitor.getUrl())
                .method(monitor.getMethod())
                .headers(monitor.getHeaders())
                .requestBody(monitor.getRequestBody())
                .contentType(monitor.getContentType())
                .intervalSeconds(monitor.getIntervalSeconds())
                .timeoutMs(monitor.getTimeoutMs())
                .enabled(monitor.getEnabled())
                .groupId(group != null ? group.getId() : null)
                .groupName(group != null ? group.getName() : null)
                .createdAt(monitor.getCreatedAt())
                .updatedAt(monitor.getUpdatedAt());

        // Add Redis status data if available
        if (status != null) {
            builder.currentStatus(status.getStatus())
                    .lastCheckedAt(status.getLastCheckedAt())
                    .lastResponseTimeMs(status.getResponseTimeMs())
                    .lastResponseCode(status.getResponseCode())
                    .lastErrorMessage(status.getErrorMessage());
        } else {
            builder.currentStatus(PingStatus.UNKNOWN);
        }

        return builder.build();
    }

    // Helper method for getting monitors within group (если нужен)
    public List<MonitorDetailResponse> getMonitorsWithinGroup(Integer ownerId, Long groupId) {
        MonitorGroup group = monitorGroupRepository.findByIdAndOwnerId(groupId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found or not owned by user"));

        List<Monitor> monitors = monitorRepository.findByGroup(group);

        return monitors.stream()
                .map(monitor -> {
                    Optional<MonitorStatusDTO> statusOpt = monitorStatusService.getStatus(monitor.getId());
                    return mapToDetailResponse(monitor, statusOpt.orElse(null));
                })
                .toList();
    }
}


