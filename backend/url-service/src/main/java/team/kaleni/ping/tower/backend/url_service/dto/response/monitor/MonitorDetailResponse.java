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
@Schema(name = "Детальная информация о мониторе", description = "Полная информация о мониторе включая конфигурацию и текущий статус")
public class MonitorDetailResponse {

    @Schema(description = "Идентификатор монитора", example = "123")
    private Long id;

    @Schema(description = "Название монитора", example = "Проверка API здоровья")
    private String name;

    @Schema(description = "Описание назначения монитора", example = "Проверяет доступность основного эндпоинта API")
    private String description;

    // === HTTP Configuration ===
    @Schema(description = "Целевой URL для мониторинга", example = "https://api.example.com/health")
    private String url;

    @Schema(description = "HTTP метод запроса", example = "POST", allowableValues = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    private HttpMethod method;

    @Schema(description = "Дополнительные HTTP заголовки", example = "{\"Authorization\": \"Bearer token123\"}")
    private Map<String, String> headers;

    @Schema(description = "Тело запроса для POST/PUT запросов", example = "{\"ping\": \"test\"}")
    private String requestBody;

    @Schema(description = "Тип содержимого Content-Type", example = "application/json")
    private String contentType;

    // === Monitoring Configuration ===
    @Schema(description = "Интервал проверки в секундах", example = "300")
    private Integer intervalSeconds;

    @Schema(description = "Таймаут запроса в миллисекундах", example = "10000")
    private Integer timeoutMs;

    @Schema(description = "Статус активности монитора", example = "true")
    private Boolean enabled;

    // === Current Status ===
    @Schema(description = "Текущий статус проверки", example = "UP", allowableValues = {"UP", "DOWN", "ERROR"})
    private PingStatus currentStatus;

    @Schema(description = "Время последней проверки", example = "2025-09-21T08:15:30.123Z", format = "date-time")
    private Instant lastCheckedAt;

    @Schema(description = "Время отклика последней проверки в миллисекундах", example = "250")
    private Integer lastResponseTimeMs;

    @Schema(description = "HTTP код ответа последней проверки", example = "200")
    private Integer lastResponseCode;

    @Schema(description = "Сообщение об ошибке при неудачной проверке", example = "Connection timeout")
    private String lastErrorMessage;

    // === Relationships ===
    @Schema(description = "Идентификатор группы мониторов", example = "10")
    private Long groupId;

    @Schema(description = "Название группы мониторов", example = "Production APIs")
    private String groupName;

    // === Metadata ===
    @Schema(description = "Время создания монитора", example = "2025-09-21T07:00:00.000Z", format = "date-time")
    private Instant createdAt;

    @Schema(description = "Время последнего обновления конфигурации", example = "2025-09-21T08:00:00.000Z", format = "date-time")
    private Instant updatedAt;

    @Schema(description = "Время следующей запланированной проверки", example = "2025-09-21T08:20:30.000Z", format = "date-time")
    private Instant nextPingAt;
}
