package team.kaleni.ping.tower.backend.url_service.dto.response.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(name = "GroupResponse", description = "Group information")
public class GroupResponse {

    @Schema(description = "Group ID", example = "123")
    private Long id;

    @Schema(description = "Group name", example = "Production Services")
    private String name;

    @Schema(description = "Group description", example = "Critical production monitoring")
    private String description;

    @Schema(description = "Owner ID", example = "42")
    private Integer ownerId;

    @Schema(description = "Number of monitors in group", example = "5")
    private Long monitorCount;

    @Schema(description = "Created timestamp")
    private Instant createdAt;

    @Schema(description = "Updated timestamp")
    private Instant updatedAt;
}
