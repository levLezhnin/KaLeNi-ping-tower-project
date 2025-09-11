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
import team.kaleni.ping.tower.backend.url_service.dto.request.CreateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.response.MonitorResponse;
import team.kaleni.ping.tower.backend.url_service.service.MonitorService;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

