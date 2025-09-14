package team.kaleni.ping.tower.backend.url_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.kaleni.ping.tower.backend.url_service.dto.request.monitor.CreateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.request.monitor.UpdateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.DeleteResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorDetailResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorResponse;
import team.kaleni.ping.tower.backend.url_service.service.MonitorService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/monitors")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @Operation(
            summary = "Create a new monitor",
            description = "Creates a monitor for the authorized user. URL is normalized and reused if already exists.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Monitor created",
                            content = @Content(schema = @Schema(implementation = MonitorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation or business error",
                            content = @Content)
            }
    )
    @PostMapping("register")
    public ResponseEntity<MonitorResponse> createMonitor(
            @Parameter(description = "Temporary: owner id header (replace with auth later)", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId,
            @Valid @RequestBody CreateMonitorRequest request
    ) {
        MonitorResponse response = monitorService.createMonitor(ownerId, request);
        if (response.isResult()){
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(
            summary = "Get monitor by ID",
            description = "Retrieves detailed information about a specific monitor",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Monitor found",
                            content = @Content(schema = @Schema(implementation = MonitorDetailResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Monitor not found or not owned by user",
                            content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<MonitorDetailResponse> getMonitorById(
            @Parameter(description = "Monitor ID", example = "123")
            @PathVariable Long id,
            @Parameter(description = "Owner ID", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId
    ) {
        MonitorDetailResponse response = monitorService.getMonitorById(ownerId, id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all monitors",
            description = "Retrieves all monitors for the authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Monitors retrieved successfully")
            }
    )
    @GetMapping
    public ResponseEntity<List<MonitorDetailResponse>> getAllMonitors(
            @Parameter(description = "Owner ID", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId
    ) {
        List<MonitorDetailResponse> monitors = monitorService.getAllMonitors(ownerId);
        return ResponseEntity.ok(monitors);
    }

    @Operation(
            summary = "Update monitor",
            description = "Updates an existing monitor. Can change settings and group assignment.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Monitor updated successfully",
                            content = @Content(schema = @Schema(implementation = MonitorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error or invalid data",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Monitor not found or not owned by user",
                            content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<MonitorDetailResponse> updateMonitor(
            @Parameter(description = "Monitor ID to update", example = "123")
            @PathVariable Long id,
            @Parameter(description = "Owner ID", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId,
            @Valid @RequestBody UpdateMonitorRequest request
    ) {
        MonitorDetailResponse response = monitorService.updateMonitor(ownerId, id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete monitor",
            description = "Deletes a monitor and optionally cleans up orphaned target URLs.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Monitor deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Monitor not found or not owned by user",
                            content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> deleteMonitor(
            @Parameter(description = "Monitor ID to delete", example = "123")
            @PathVariable Long id,
            @Parameter(description = "Owner ID", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId
    ) {
        monitorService.deleteMonitor(ownerId, id);
        DeleteResponse response = DeleteResponse.builder()
                .success(true)
                .deletedId(id)
                .deletedAt(Instant.now())
                .message("Monitor deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Enable monitor", description = "Enables a monitor for ping checks")
    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enableMonitor(
            @PathVariable Long id,
            @RequestHeader("X-Owner-Id") Integer ownerId
    ) {
        monitorService.setMonitorEnabled(ownerId, id, true);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Disable monitor", description = "Disables a monitor from ping checks")
    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disableMonitor(
            @PathVariable Long id,
            @RequestHeader("X-Owner-Id") Integer ownerId
    ) {
        monitorService.setMonitorEnabled(ownerId, id, false);
        return ResponseEntity.ok().build();
    }





}

