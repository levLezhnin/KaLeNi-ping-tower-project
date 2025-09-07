package team.kaleni.ping.tower.backend.url_service.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.kaleni.ping.tower.backend.url_service.dto.request.CreateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.response.MonitorResponse;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;
import team.kaleni.ping.tower.backend.url_service.entity.MonitorGroup;
import team.kaleni.ping.tower.backend.url_service.entity.TargetUrl;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorGroupRepository;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorRepository;
import team.kaleni.ping.tower.backend.url_service.repository.TargetUrlRepository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final TargetUrlRepository targetUrlRepository;
    private final MonitorGroupRepository monitorGroupRepository;

    @Transactional
    public MonitorResponse createMonitor(Integer ownerId, CreateMonitorRequest req) {
        // 1) Normalize URL and find or create TargetUrl
        String normalized = URLNormalizer.normalize(req.getUrl());
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
        Optional<Monitor> existingByName = monitorRepository.findByOwnerAndName(ownerId, req.getName());
        if (existingByName.isPresent()) {
            throw new IllegalArgumentException("Monitor with the same name already exists for this owner");
        }
        // 2.2) Build Monitor entity (enabled by default)
        Monitor monitor = Monitor.builder()
                .name(req.getName())
                .owner(ownerId)
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
                    .filter(g -> g.getOwner().equals(ownerId))
                    .orElseThrow(() -> new IllegalArgumentException("Group not found or not owned by user"));
        }
        monitor.setGroup(group);
        Monitor saved = monitorRepository.save(monitor);
        // 5) Map to new minimal response
        return MonitorResponse.builder()
                .id(saved.getId())
                .url(target.getUrl())
                .newlyCreatedTarget(newlyCreatedTarget.get())
                .targetId(target.getId())
                .groupId(saved.getGroup() != null ? saved.getGroup().getId() : null)
                .enabled(saved.getEnabled())
                .build();
    }
}

