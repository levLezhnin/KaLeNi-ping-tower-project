package team.kaleni.ping.tower.backend.url_service.dto.response.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorDetailResponse;

import java.util.List;

@Data
@Builder
@Schema(name = "GroupResponse", description = "Group information")
public class GroupWithListResponse {

    @Schema(description = "Group data")
    private GroupResponse group;

    @Schema(description = "List of monitors assigned to the group")
    private List<MonitorDetailResponse> monitors;
}
