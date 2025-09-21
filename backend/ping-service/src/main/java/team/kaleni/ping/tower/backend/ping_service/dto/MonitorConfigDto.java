package team.kaleni.ping.tower.backend.ping_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.kaleni.ping.tower.backend.ping_service.enums.HttpMethod;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorConfigDto {
    private Long monitorId;
    private Long ownerId;
    private String url;
    private String name;
    private HttpMethod method;
    private Map<String, String> headers;
    private String requestBody;
    private String contentType;
    private Integer timeoutMs;
    private Integer intervalSeconds;
}
