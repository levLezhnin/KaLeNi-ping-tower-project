package team.kaleni.ping.tower.backend.url_service.dto.response.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(name = "DeleteResponse", description = "Delete operation result")
public class DeleteResponse {

    @Schema(description = "Success flag", example = "true")
    private boolean success;

    @Schema(description = "Deleted monitor ID", example = "123")
    private Long deletedId;

    @Schema(description = "Deleted monitor name", example = "API Health Check")
    private String deletedName;

    @Schema(description = "Deletion timestamp")
    private Instant deletedAt;

    @Schema(description = "Number of ping records deleted", example = "1247")
    private Integer deletedRecords;

    @Schema(description = "Additional cleanup information", example = "Monitor successfully removed from all groups")
    private String message;
}
