package team.kaleni.ping.tower.backend.url_service.dto.response.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import team.kaleni.ping.tower.backend.url_service.entity.HttpMethod;
import team.kaleni.ping.tower.backend.url_service.entity.PingStatus;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@Schema(name = "MonitorDetailResponse", description = "Detailed monitor information")
public class MonitorDetailResponse {

    @Schema(description = "Monitor ID", example = "123")
    private Long id;

    @Schema(description = "Monitor name", example = "API Health Check")
    private String name;

    @Schema(description = "Monitor description", example = "Checks main API endpoint health")
    private String description;

    // === HTTP Configuration ===
    @Schema(description = "Target URL", example = "https://api.example.com/health")
    private String url;

    @Schema(description = "HTTP method", example = "POST")
    private HttpMethod method;

    @Schema(description = "Custom HTTP headers")
    private Map<String, String> headers;

    @Schema(description = "Request body for POST/PUT requests", example = "{\"ping\": \"test\"}")
    private String requestBody;

    @Schema(description = "Content-Type header", example = "application/json")
    private String contentType;

    // === Monitoring Configuration ===
    @Schema(description = "Check interval in seconds", example = "300")
    private Integer intervalSeconds;

    @Schema(description = "Timeout in milliseconds", example = "10000")
    private Integer timeoutMs;

    @Schema(description = "Is monitor enabled", example = "true")
    private Boolean enabled;

    // === Current Status ===
    @Schema(description = "Current status", example = "UP")
    private PingStatus currentStatus;

    @Schema(description = "Last checked time")
    private Instant lastCheckedAt;

    @Schema(description = "Last response time in milliseconds", example = "250")
    private Integer lastResponseTimeMs;

    @Schema(description = "Last HTTP response code", example = "200")
    private Integer lastResponseCode;

    @Schema(description = "Last error message if failed", example = "Connection timeout")
    private String lastErrorMessage;

    // === Relationships ===
    @Schema(description = "Group ID if assigned", example = "10")
    private Long groupId;

    @Schema(description = "Group name if assigned", example = "Production APIs")
    private String groupName;

    // === Metadata ===
    @Schema(description = "Created timestamp")
    private Instant createdAt;

    @Schema(description = "Updated timestamp")
    private Instant updatedAt;

    @Schema(description = "Next scheduled ping time")
    private Instant nextPingAt;
}
