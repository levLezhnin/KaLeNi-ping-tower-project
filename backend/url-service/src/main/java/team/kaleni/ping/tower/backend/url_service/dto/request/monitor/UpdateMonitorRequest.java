package team.kaleni.ping.tower.backend.url_service.dto.request.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import team.kaleni.ping.tower.backend.url_service.entity.HttpMethod;

import java.util.Map;

@Data
@Schema(name = "Запрос обновления монитора", description = "Данные для обновления существующего монитора")
public class UpdateMonitorRequest {

    // Basic fields
    @Size(max = 255, message = "Имя должно содержать не более 255 символов")
    @Schema(description = "Название монитора", example = "Обновленный монитор веб-сайта")
    private String name;

    @Size(max = 1000, message = "Описание должно содержать не более 1000 символов")
    @Schema(description = "Описание назначения монитора", example = "Обновленное описание")
    private String description;

    @Size(max = 2048, message = "URL должен содержать не более 2048 символов")
    @Schema(description = "Целевой URL для мониторинга", example = "https://api.updated.com/health")
    private String url;

    @Schema(description = "HTTP метод запроса", example = "POST", allowableValues = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    private HttpMethod method;

    @Schema(description = "Дополнительные HTTP заголовки", example = "{\"Authorization\": \"Bearer newtoken\"}")
    private Map<String, String> headers;

    @Size(max = 4000, message = "Тело запроса должно содержать не более 4000 символов")
    @Schema(description = "Тело запроса для POST/PUT запросов", example = "{\"updated\": \"data\"}")
    private String requestBody;

    @Size(max = 100)
    @Schema(description = "Тип содержимого Content-Type", example = "application/json")
    private String contentType;

    // Monitoring Configuration
    @Min(value = 30, message = "Интервал должен быть не менее 30 секунд")
    @Schema(description = "Интервал проверки в секундах", example = "180", minimum = "30")
    private Integer intervalSeconds;

    @Min(value = 1000, message = "Таймаут должен быть не менее 1000 миллисекунд")
    @Schema(description = "Таймаут запроса в миллисекундах", example = "8000", minimum = "1000")
    private Integer timeoutMs;

    @Schema(description = "Идентификатор группы мониторов", example = "5")
    private Long groupId;

    @Schema(description = "Включить/выключить монитор", example = "true")
    private Boolean enabled;
}
