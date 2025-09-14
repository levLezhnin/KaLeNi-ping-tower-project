package team.kaleni.ping.tower.backend.url_service.dto.request.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(title = "CreateMonitorRequest")
public class CreateMonitorRequest {

    @NotBlank(message = "Monitor name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    @Schema(name = "name", description = "name of the url that will be monitored")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    @Schema(name = "", description = "Any additional info about the monitor")
    private String description;

    @NotBlank(message = "URL is required")
    @Size(max = 2048, message = "URL must be at most 2048 characters")
    @Schema(name = "url", description = "Url that should be tested")
    private String url;

    @NotNull(message = "Interval is required")
    @Min(value = 30, message = "Interval must be at least 30 seconds")
    @Schema(name = "intervalSeconds", description = "Amount of time between checks in seconds")
    private Integer intervalSeconds;

    @Min(value = 1000, message = "Timeout must be at least 1000 milliseconds")
    @Schema(name = "timeoutMs", description = "Time that should be waited before ping abort")
    private Integer timeoutMs;

    @Schema(name = "groupId", description = "In case monitor is in group")
    private Long groupId; // optional group assignment
}

