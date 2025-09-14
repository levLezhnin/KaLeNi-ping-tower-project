package team.kaleni.ping.tower.backend.url_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.kaleni.ping.tower.backend.url_service.dto.request.monitor.CreateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.request.monitor.UpdateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorDetailResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorResponse;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;
import team.kaleni.ping.tower.backend.url_service.entity.MonitorGroup;
import team.kaleni.ping.tower.backend.url_service.entity.TargetUrl;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorGroupRepository;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorRepository;
import team.kaleni.ping.tower.backend.url_service.repository.TargetUrlRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final TargetUrlRepository targetUrlRepository;
    private final MonitorGroupRepository monitorGroupRepository;
    private final PingService pingService;
    private final URLNormalizer urlNormalizer;

    @Transactional
    public MonitorResponse createMonitor(Integer ownerId, CreateMonitorRequest req) {
        // 0) Normalize URL and test for correctness:
        String normalized = urlNormalizer.normalize(req.getUrl());
        if (!testUrl(normalized)) {
            log.error("The url {} is not valid!", normalized);
            return MonitorResponse.builder().result(false).url(normalized).build();
        }
        // 1) Find or create TargetUrl
        AtomicBoolean newlyCreatedTarget = new AtomicBoolean(false);
        TargetUrl target = targetUrlRepository.findByUrl(normalized)
                .orElseGet(() -> {
                    TargetUrl t = new TargetUrl();
                    t.setUrl(normalized);
                    TargetUrl saved = targetUrlRepository.save(t);
                    newlyCreatedTarget.set(true);
                    return saved;
                });
        // 2) Unique monitor name per owner enforced by constraint (owner_id, name)
        Optional<Monitor> existingByName = monitorRepository.findByOwnerIdAndName(ownerId, req.getName());
        if (existingByName.isPresent()) {
            throw new IllegalArgumentException("Monitor with the same name already exists for this owner");
        }
        // 2.2) Build Monitor entity (enabled by default)
        Monitor monitor = Monitor.builder()
                .name(req.getName())
                .ownerId(ownerId)
                .target(target)
                .description(req.getDescription())
                .intervalSeconds(req.getIntervalSeconds())
                .enabled(true)
                .build();
        if (req.getTimeoutMs() != null) {
            monitor.setTimeoutMs(req.getTimeoutMs());
        }
        // 3) Optional group (must belong to owner)
        MonitorGroup group = null;
        if (req.getGroupId() != null) {
            group = monitorGroupRepository.findById(req.getGroupId())
                    .filter(g -> Objects.equals(g.getOwnerId(), ownerId))
                    .orElseThrow(() -> new IllegalArgumentException("Group not found or not owned by user"));
        }
        monitor.setGroup(group);
        Monitor saved = monitorRepository.save(monitor);
        // 4) Map to new minimal response
        return MonitorResponse.builder()
                .result(true)
                .id(saved.getId())
                .url(target.getUrl())
                .newlyCreatedTarget(newlyCreatedTarget.get())
                .targetId(target.getId())
                .groupId(saved.getGroup() != null ? saved.getGroup().getId() : null)
                .enabled(saved.getEnabled())
                .build();
    }

    private boolean testUrl(String url) {
        return pingService.pingURL(url);
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

    @Transactional
    public MonitorDetailResponse updateMonitor(Integer ownerId, Long monitorId, UpdateMonitorRequest req) {
        // Find existing monitor and verify ownership
        Monitor existingMonitor = monitorRepository.findByIdAndOwnerId(monitorId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found or not owned by user"));
        if (req.getGroupId() != null) {
            MonitorGroup group = monitorGroupRepository.findByIdAndOwnerId(req.getGroupId(), ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Group not found or not owned by user"));
            existingMonitor.setGroup(group);
        }
        if (req.getName() != null) {
            existingMonitor.setName(req.getName());
        }
        if (req.getDescription() != null) {
            existingMonitor.setDescription(req.getDescription());
        }
        existingMonitor.setIntervalSeconds(req.getIntervalSeconds());
        if (req.getTimeoutMs() != null) {
            existingMonitor.setTimeoutMs(req.getTimeoutMs());
        }
        if (req.getEnabled() != null) {
            existingMonitor.setEnabled(req.getEnabled());
        }
        Monitor savedMonitor = monitorRepository.save(existingMonitor);
        return mapToDetailResponse(savedMonitor);
    }

    @Transactional
    public void deleteMonitor(Integer ownerId, Long monitorId) {
        //Find monitor and verify ownership
        Monitor monitor = monitorRepository.findByIdAndOwnerId(monitorId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found or not owned by user"));
        monitorRepository.delete(monitor);
        TargetUrl target = monitor.getTarget();
        long remainingMonitors = monitorRepository.countByTarget(target);
        if (remainingMonitors == 0) {
            log.info("Cleaning up orphaned TargetUrl: {}", target.getUrl());
            targetUrlRepository.delete(target);
        }
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
        TargetUrl target = monitor.getTarget();
        MonitorGroup group = monitor.getGroup();
        return MonitorDetailResponse.builder()
                .id(monitor.getId())
                .name(monitor.getName())
                .description(monitor.getDescription())
                .url(target.getUrl())
                .targetId(target.getId())
                .intervalSeconds(monitor.getIntervalSeconds())
                .timeoutMs(monitor.getTimeoutMs())
                .enabled(monitor.getEnabled())
                .currentStatus(target.getLastStatus())
                .lastCheckedAt(target.getLastCheckedAt())
                .groupId(group != null ? group.getId() : null)
                .createdAt(monitor.getCreatedAt())
                .updatedAt(monitor.getUpdatedAt())
                .build();
    }
}

