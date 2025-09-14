package team.kaleni.ping.tower.backend.url_service.dto.request.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "UpdateMonitorRequest")
public class UpdateMonitorRequest {

    @Size(max = 255, message = "Name must be at most 255 characters")
    @Schema(description = "Monitor name", example = "Updated Website Monitor")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    @Schema(description = "Monitor description", example = "Updated description")
    private String description;

    @NotNull(message = "Interval is required")
    @Min(value = 30, message = "Interval must be at least 30 seconds")
    @Schema(description = "Check interval in seconds", example = "180")
    private Integer intervalSeconds;

    @Min(value = 1000, message = "Timeout must be at least 1000 milliseconds")
    @Schema(description = "Timeout in milliseconds", example = "8000")
    private Integer timeoutMs;

    @Schema(description = "Group ID", example = "5")
    private Long groupId;

    @Schema(description = "Enable/disable monitor", example = "true")
    private Boolean enabled;
}
