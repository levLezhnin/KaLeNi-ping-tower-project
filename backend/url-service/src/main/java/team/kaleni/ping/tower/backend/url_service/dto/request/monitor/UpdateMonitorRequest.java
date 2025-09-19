package team.kaleni.ping.tower.backend.url_service.dto.request.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import team.kaleni.ping.tower.backend.url_service.entity.HttpMethod;

import java.util.Map;

@Data
@Schema(name = "UpdateMonitorRequest")
public class UpdateMonitorRequest {

    // Basic fields
    @Size(max = 255, message = "Name must be at most 255 characters")
    @Schema(description = "Monitor name", example = "Updated Website Monitor")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    @Schema(description = "Monitor description", example = "Updated description")
    private String description;

    @Size(max = 2048, message = "URL must be at most 2048 characters")
    @Schema(description = "Target URL", example = "https://api.updated.com/health")
    private String url;

    @Schema(description = "HTTP method", example = "POST")
    private HttpMethod method;

    @Schema(description = "Custom HTTP headers")
    private Map<String, String> headers;

    @Size(max = 4000, message = "Request body must be at most 4000 characters")
    @Schema(description = "Request body for POST requests", example = "{\"updated\": \"data\"}")
    private String requestBody;

    @Size(max = 100)
    @Schema(description = "Content-Type header", example = "application/json")
    private String contentType;

    // Monitoring Configuration
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
