package team.kaleni.ping.tower.backend.url_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "MonitorResponse", description = "Created monitor payload")
public class MonitorResponse {

    @Schema(description = "True or False depends on registration", example = "True")
    private boolean result;

    @Schema(description = "Monitor id", example = "123")
    private Long id;

    @Schema(description = "Normalized URL being monitored", example = "https://example.com")
    private String url;

    @Schema(description = "Flag if it is new url for the system", example = "true")
    private boolean newlyCreatedTarget;

    @Schema(description = "Target URL id", example = "77")
    private Long targetId;

    @Schema(description = "Group id if assigned", example = "10")
    private Long groupId;

    @Schema(description = "Enabled flag", example = "true")
    private Boolean enabled;
}

