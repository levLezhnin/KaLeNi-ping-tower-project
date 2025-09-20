package team.kaleni.ping.tower.backend.url_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.kaleni.ping.tower.backend.url_service.dto.request.group.CreateGroupRequest;
import team.kaleni.ping.tower.backend.url_service.dto.request.group.UpdateGroupRequest;
import team.kaleni.ping.tower.backend.url_service.dto.response.group.GroupResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.group.GroupWithListResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorDetailResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorResponse;
import team.kaleni.ping.tower.backend.url_service.entity.MonitorGroup;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorGroupRepository;
import team.kaleni.ping.tower.backend.url_service.repository.MonitorRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final MonitorGroupRepository monitorGroupRepository;
    private final MonitorRepository monitorRepository;
    private final MonitorService monitorService;

    @Transactional
    public GroupResponse createGroup(Integer ownerId, CreateGroupRequest request) {
        // Check for duplicate name per owner
        Optional<MonitorGroup> existingGroup = monitorGroupRepository.findByOwnerIdAndName(ownerId, request.getName());
        if (existingGroup.isPresent()) {
            throw new IllegalArgumentException("Group with name '" + request.getName() + "' already exists");
        }
        // Create new group
        MonitorGroup group = new MonitorGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setOwnerId(ownerId);
        MonitorGroup savedGroup = monitorGroupRepository.save(group);
        log.info("Created group {} for owner {}", savedGroup.getId(), ownerId);
        return mapToResponse(savedGroup);
    }

    public GroupWithListResponse getGroupById(Integer ownerId, Long groupId) {
        MonitorGroup group = monitorGroupRepository.findByIdAndOwnerId(groupId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found or not owned by user"));
        GroupResponse groupResponse =  mapToResponse(group);
        List<MonitorDetailResponse> monitors = monitorService.getMonitorsWithinGroup(ownerId, group.getId());
        return GroupWithListResponse
                .builder()
                .group(groupResponse)
                .monitors(monitors)
                .build();
    }

    public List<GroupResponse> getAllGroups(Integer ownerId) {
        List<MonitorGroup> groups = monitorGroupRepository.findByOwnerId(ownerId);
        return groups.stream()
                .map(this::mapToResponse)
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    @Transactional
    public GroupResponse updateGroup(Integer ownerId, Long groupId, UpdateGroupRequest request) {
        // Find existing group and verify ownership
        MonitorGroup existingGroup = monitorGroupRepository.findByIdAndOwnerId(groupId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found or not owned by user"));
        // Check for duplicate name (excluding current group)
        Optional<MonitorGroup> duplicateGroup = monitorGroupRepository.findByOwnerIdAndNameExcludingId(
                ownerId, request.getName(), groupId);
        if (duplicateGroup.isPresent()) {
            throw new IllegalArgumentException("Group with name '" + request.getName() + "' already exists");
        }
        // Update fields
        existingGroup.setName(request.getName());
        existingGroup.setDescription(request.getDescription());
        MonitorGroup savedGroup = monitorGroupRepository.save(existingGroup);
        log.info("Updated group {} for owner {}", groupId, ownerId);
        return mapToResponse(savedGroup);
    }

    @Transactional
    public void deleteGroup(Integer ownerId, Long groupId) {
        // Find group and verify ownership
        MonitorGroup group = monitorGroupRepository.findByIdAndOwnerId(groupId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found or not owned by user"));
        // Remove group association from all monitors in this group
        // Monitors will remain but without group assignment
        monitorRepository.findByGroup(group).forEach(monitor -> {
            monitor.setGroup(null);
            monitorRepository.save(monitor);
        });
        // Delete the group
        monitorGroupRepository.delete(group);
        log.info("Deleted group {} for owner {}", groupId, ownerId);
    }

    private GroupResponse mapToResponse(MonitorGroup group) {
        // Count monitors in this group
        long monitorCount = monitorRepository.countByGroup(group);
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwnerId())
                .monitorCount(monitorCount)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}
