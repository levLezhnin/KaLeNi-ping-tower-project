package team.kaleni.ping.tower.backend.url_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public MonitorResponse createMonitor(Integer ownerId, CreateMonitorRequest req) {
        // 0) Normalize URL and test for correctness:
        String normalized = URLNormalizer.normalize(req.getUrl());
        if (!testUrl(normalized)){
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
        // 5) Map to new minimal response
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

    private boolean testUrl(String url){
        return pingService.pingURL(url);
    }
}

