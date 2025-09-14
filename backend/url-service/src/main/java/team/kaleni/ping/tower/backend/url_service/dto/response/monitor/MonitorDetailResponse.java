package team.kaleni.ping.tower.backend.url_service.dto.response.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import team.kaleni.ping.tower.backend.url_service.entity.PingStatus;

import java.time.Instant;

@Data
@Builder
@Schema(name = "MonitorDetailResponse", description = "Detailed monitor information")
public class MonitorDetailResponse {

    @Schema(description = "Monitor ID", example = "123")
    private Long id;

    @Schema(description = "Monitor name", example = "My Website")
    private String name;

    @Schema(description = "Monitor description", example = "Main company website")
    private String description;

    @Schema(description = "Target URL", example = "https://example.com")
    private String url;

    @Schema(description = "Target ID", example = "77")
    private Long targetId;

    @Schema(description = "Check interval in seconds", example = "300")
    private Integer intervalSeconds;

    @Schema(description = "Timeout in milliseconds", example = "10000")
    private Integer timeoutMs;

    @Schema(description = "Is monitor enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Current status", example = "UP")
    private PingStatus currentStatus;

    @Schema(description = "Last checked time")
    private Instant lastCheckedAt;

    @Schema(description = "Group ID if assigned", example = "10")
    private Long groupId;

    @Schema(description = "Created timestamp")
    private Instant createdAt;

    @Schema(description = "Updated timestamp")
    private Instant updatedAt;
}
