package team.kaleni.ping.tower.backend.url_service.dto.request.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Map;

@Data
@Schema(title = "Create monitor request")
public class CreateMonitorRequest {

    @NotBlank(message = "Monitor name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    @Schema(description = "Monitor name", example = "API Health Check")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    @Schema(description = "Monitor description", example = "Checks API endpoint health")
    private String description;

    // HTTP Configuration
    @NotBlank(message = "URL is required")
    @Size(max = 2048, message = "URL must be at most 2048 characters")
    @Schema(description = "Target URL", example = "https://api.example.com/health")
    private String url;

    @Schema(description = "HTTP method", example = "GET")
    private String method = "GET";

    @Schema(description = "Custom HTTP headers")
    private Map<String, String> headers;

    @Size(max = 4000, message = "Request body must be at most 4000 characters")
    @Schema(description = "Request body for POST requests", example = "{\"ping\": \"test\"}")
    private String requestBody;

    @Size(max = 100)
    @Schema(description = "Content-Type header", example = "application/json")
    private String contentType;

    // Monitoring Configuration
    @Min(value = 30, message = "Interval must be at least 30 seconds")
    @Max(value = 86400, message = "Interval must be at most 24 hours")
    @Schema(description = "Check interval in seconds", example = "300")
    private Integer intervalSeconds = 300;

    @Min(value = 1000, message = "Timeout must be at least 1 second")
    @Max(value = 300000, message = "Timeout must be at most 5 minutes")
    @Schema(description = "Request timeout in milliseconds", example = "10000")
    private Integer timeoutMs = 10000;

    @Schema(description = "Group ID", example = "123")
    private Long groupId;
}


