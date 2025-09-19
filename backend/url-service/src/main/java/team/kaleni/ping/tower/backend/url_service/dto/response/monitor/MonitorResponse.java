package team.kaleni.ping.tower.backend.url_service.dto.response.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import team.kaleni.ping.tower.backend.url_service.entity.HttpMethod;

@Data
@Builder
@Schema(name = "MonitorResponse", description = "Monitor creation response")
public class MonitorResponse {

    @Schema(description = "Operation result", example = "true")
    private boolean result;

    @Schema(description = "Monitor ID", example = "123")
    private Long id;

    @Schema(description = "Monitor name", example = "API Health Check")
    private String name;

    @Schema(description = "Monitor URL", example = "https://api.example.com/health")
    private String url;

    @Schema(description = "HTTP method", example = "GET")
    private HttpMethod method;

    @Schema(description = "Check interval in seconds", example = "300")
    private Integer intervalSeconds;

    @Schema(description = "Group ID", example = "456")
    private Long groupId;

    @Schema(description = "Monitor enabled status", example = "true")
    private Boolean enabled;

    @Schema(description = "Error message if creation failed", example = "Invalid URL format")
    private String errorMessage;
}
