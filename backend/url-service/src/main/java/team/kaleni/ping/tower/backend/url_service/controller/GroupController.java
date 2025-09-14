package team.kaleni.ping.tower.backend.url_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.kaleni.ping.tower.backend.url_service.dto.request.group.CreateGroupRequest;
import team.kaleni.ping.tower.backend.url_service.dto.request.group.UpdateGroupRequest;
import team.kaleni.ping.tower.backend.url_service.dto.response.group.GroupResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.group.GroupWithListResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.DeleteResponse;
import team.kaleni.ping.tower.backend.url_service.service.GroupService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Group Management", description = "Operations for managing monitor groups")
public class GroupController {

    private final GroupService groupService;

    @Operation(
            summary = "Create a new group",
            description = "Creates a new monitor group for organizing monitors",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Group created successfully",
                            content = @Content(schema = @Schema(implementation = GroupResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error or duplicate name",
                            content = @Content)
            }
    )
    @PostMapping(path = "register")
    public ResponseEntity<GroupResponse> createGroup(
            @Parameter(description = "Owner ID", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        GroupResponse response = groupService.createGroup(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get group by ID",
            description = "Retrieves detailed information about a specific group",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Group found",
                            content = @Content(schema = @Schema(implementation = GroupResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Group not found or not owned by user",
                            content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<GroupWithListResponse> getGroupById(
            @Parameter(description = "Group ID", example = "123")
            @PathVariable Long id,
            @Parameter(description = "Owner ID", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId
    ) {
        GroupWithListResponse response = groupService.getGroupById(ownerId, id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all groups",
            description = "Retrieves all groups for the authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
            }
    )
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups(
            @Parameter(description = "Owner ID", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId
    ) {
        List<GroupResponse> groups = groupService.getAllGroups(ownerId);
        return ResponseEntity.ok(groups);
    }

    @Operation(
            summary = "Update group",
            description = "Updates an existing group's name and description",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Group updated successfully",
                            content = @Content(schema = @Schema(implementation = GroupResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error or duplicate name",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Group not found or not owned by user",
                            content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<GroupResponse> updateGroup(
            @Parameter(description = "Group ID to update", example = "123")
            @PathVariable Long id,
            @Parameter(description = "Owner ID", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        GroupResponse response = groupService.updateGroup(ownerId, id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete group",
            description = "Deletes a group and removes group assignment from all monitors in it, monitors stay as were",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Group deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Group not found or not owned by user",
                            content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> deleteGroup(
            @Parameter(description = "Group ID to delete", example = "123")
            @PathVariable Long id,
            @Parameter(description = "Owner ID", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId
    ) {
        groupService.deleteGroup(ownerId, id);
        DeleteResponse response = DeleteResponse.builder()
                .success(true)
                .deletedId(id)
                .deletedAt(Instant.now())
                .message("Group deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
