package team.kaleni.ping.tower.backend.url_service.dto.request.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(title = "Update group request")
public class UpdateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    @Schema(description = "Group name", example = "Updated Production Services")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    @Schema(description = "Group description", example = "Updated description")
    private String description;
}
