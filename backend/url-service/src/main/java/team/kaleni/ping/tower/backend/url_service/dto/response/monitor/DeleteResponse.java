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

    @Schema(description = "Deletion timestamp")
    private Instant deletedAt;

    @Schema(description = "Additional cleanup performed", example = "Orphaned target URL removed")
    private String message;
}
