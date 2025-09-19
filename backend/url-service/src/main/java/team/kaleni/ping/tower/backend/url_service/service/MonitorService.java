package team.kaleni.ping.tower.backend.url_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.kaleni.ping.tower.backend.url_service.dto.request.monitor.CreateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.request.monitor.UpdateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorDetailResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorResponse;
import team.kaleni.ping.tower.backend.url_service.entity.HttpMethod;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;
import team.kaleni.ping.tower.backend.url_service.entity.MonitorGroup;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorGroupRepository;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final MonitorGroupRepository monitorGroupRepository;
    private final PingService pingService;

    @Transactional
    public MonitorResponse createMonitor(Integer ownerId, CreateMonitorRequest req) {
        log.info("Creating monitor for owner {} with URL: {}", ownerId, req.getUrl());

        // 1) Validate unique monitor name per owner
        Optional<Monitor> existingByName = monitorRepository.findByOwnerIdAndName(ownerId, req.getName());
        if (existingByName.isPresent()) {
            throw new IllegalArgumentException("Monitor with the same name already exists for this owner");
        }

        HttpMethod method = HttpMethod.valueOf(req.getMethod());
        // 2) Build Monitor entity with all new fields
        Monitor monitor = Monitor.builder()
                .name(req.getName())
                .description(req.getDescription())
                .ownerId(ownerId)

                // HTTP Configuration
                .url(req.getUrl())
                .method(req.getMethod() != null ? method : HttpMethod.GET)
                .headers(req.getHeaders())
                .requestBody(req.getRequestBody())
                .contentType(req.getContentType())

                // Monitoring Configuration
                .intervalSeconds(req.getIntervalSeconds())
                .timeoutMs(req.getTimeoutMs() != null ? req.getTimeoutMs() : 10000)
                .enabled(true)

                // Initialize for immediate ping
                .nextPingAt(null) // Will be picked up immediately by scheduler
                .build();

        // 3) Optional group assignment (must belong to owner)
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
                    .build();
        }

        // 5) Save monitor
        Monitor saved = monitorRepository.save(monitor);
        log.info("Monitor {} created successfully for owner {}", saved.getId(), ownerId);

        // 6) Return success response
        return MonitorResponse.builder()
                .result(true)
                .id(saved.getId())
                .url(saved.getUrl())
                .method(saved.getMethod())
                .groupId(saved.getGroup() != null ? saved.getGroup().getId() : null)
                .enabled(saved.getEnabled())
                .build();
    }

    /**
     * Test monitor configuration during creation
     */
    private boolean testMonitorConfiguration(Monitor monitor) {
        try {
            return pingService.pingMonitor(monitor);
        } catch (Exception e) {
            log.error("Error testing monitor configuration: {}", e.getMessage());
            return false;
        }
    }

    public MonitorDetailResponse getMonitorById(Integer ownerId, Long monitorId) {
        Monitor monitor = monitorRepository.findByIdAndOwnerId(monitorId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found or not owned by user"));
        return mapToDetailResponse(monitor);
    }

    public List<MonitorDetailResponse> getAllMonitors(Integer ownerId) {
        List<Monitor> monitors = monitorRepository.findByOwnerId(ownerId);
        return monitors.stream()
                .map(this::mapToDetailResponse)
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    public List<MonitorDetailResponse> getMonitorsWithinGroup(MonitorGroup group) {
        List<Monitor> monitors = monitorRepository.findByGroup(group);
        return monitors.stream()
                .map(this::mapToDetailResponse)
                .toList();
    }

    @Transactional
    public MonitorDetailResponse updateMonitor(Integer ownerId, Long monitorId, UpdateMonitorRequest req) {
        // Find existing monitor and verify ownership
        Monitor existingMonitor = monitorRepository.findByIdAndOwnerId(monitorId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found or not owned by user"));

        boolean needsPingTest = false;
        // Update basic fields
        if (req.getName() != null) {
            existingMonitor.setName(req.getName());
        }
        if (req.getDescription() != null) {
            existingMonitor.setDescription(req.getDescription());
        }
        // Update HTTP configuration (these require ping test)
        if (req.getUrl() != null) {
            existingMonitor.setUrl(req.getUrl());
            needsPingTest = true;
        }
        if (req.getMethod() != null) {
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
        if (req.getIntervalSeconds() != null) {
            existingMonitor.setIntervalSeconds(req.getIntervalSeconds());
        }
        if (req.getTimeoutMs() != null) {
            existingMonitor.setTimeoutMs(req.getTimeoutMs());
            needsPingTest = true; // Timeout change might affect connectivity
        }
        if (req.getEnabled() != null) {
            existingMonitor.setEnabled(req.getEnabled());
        }
        // Update group assignment
        if (req.getGroupId() != null) {
            MonitorGroup group = monitorGroupRepository.findByIdAndOwnerId(req.getGroupId(), ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Group not found or not owned by user"));
            existingMonitor.setGroup(group);
        }
        // Test configuration if HTTP settings changed
        if (needsPingTest && existingMonitor.getEnabled()) {
            if (!testMonitorConfiguration(existingMonitor)) {
                log.warn("Monitor configuration test failed after update for monitor {}", monitorId);
                // Don't fail the update, just log warning
            }
        }
        Monitor savedMonitor = monitorRepository.save(existingMonitor);
        log.info("Monitor {} updated successfully for owner {}", monitorId, ownerId);
        return mapToDetailResponse(savedMonitor);
    }


    @Transactional
    public void deleteMonitor(Integer ownerId, Long monitorId) {
        // Find monitor and verify ownership
        Monitor monitor = monitorRepository.findByIdAndOwnerId(monitorId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found or not owned by user"));

        monitorRepository.delete(monitor);
        log.info("Monitor {} deleted successfully for owner {}", monitorId, ownerId);
    }

    @Transactional
    public void setMonitorEnabled(Integer ownerId, Long monitorId, boolean enabled) {
        Monitor monitor = monitorRepository.findByIdAndOwnerId(monitorId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found or not owned by user"));

        monitor.setEnabled(enabled);
        monitorRepository.save(monitor);
        log.info("Monitor {} {} for owner {}", monitorId, enabled ? "enabled" : "disabled", ownerId);
    }

    private MonitorDetailResponse mapToDetailResponse(Monitor monitor) {
        MonitorGroup group = monitor.getGroup();

        return MonitorDetailResponse.builder()
                .id(monitor.getId())
                .name(monitor.getName())
                .description(monitor.getDescription())

                // HTTP Configuration
                .url(monitor.getUrl())
                .method(monitor.getMethod())
                .headers(monitor.getHeaders())
                .requestBody(monitor.getRequestBody())
                .contentType(monitor.getContentType())

                // Monitoring Configuration
                .intervalSeconds(monitor.getIntervalSeconds())
                .timeoutMs(monitor.getTimeoutMs())
                .enabled(monitor.getEnabled())

                // Current Status
                .currentStatus(monitor.getLastStatus())
                .lastCheckedAt(monitor.getLastCheckedAt())
                .lastResponseTimeMs(monitor.getLastResponseTimeMs())
                .lastResponseCode(monitor.getLastResponseCode())
                .lastErrorMessage(monitor.getLastErrorMessage())

                // Relationships
                .groupId(group != null ? group.getId() : null)

                // Metadata
                .createdAt(monitor.getCreatedAt())
                .updatedAt(monitor.getUpdatedAt())
                .build();
    }
}
