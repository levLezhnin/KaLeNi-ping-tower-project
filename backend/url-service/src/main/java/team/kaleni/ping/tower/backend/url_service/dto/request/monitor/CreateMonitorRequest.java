package team.kaleni.ping.tower.backend.url_service.dto.request.monitor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Map;

@Data
@Schema(name = "Запрос создания монитора", description = "Данные для создания нового монитора веб-сервиса")
public class CreateMonitorRequest {

    @NotBlank(message = "Имя монитора обязательно")
    @Size(max = 255, message = "Имя должно содержать не более 255 символов")
    @Schema(description = "Название монитора", example = "Проверка API здоровья")
    private String name;

    @Size(max = 1000, message = "Описание должно содержать не более 1000 символов")
    @Schema(description = "Описание назначения монитора", example = "Проверяет доступность эндпоинта API")
    private String description;

    // HTTP Configuration
    @NotBlank(message = "URL обязателен")
    @Size(max = 2048, message = "URL должен содержать не более 2048 символов")
    @Schema(description = "Целевой URL для мониторинга", example = "https://api.example.com/health")
    private String url;

    @Schema(description = "HTTP метод запроса", example = "GET", allowableValues = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    private String method = "GET";

    @Schema(description = "Дополнительные HTTP заголовки", example = "{\"Authorization\": \"Bearer token123\"}")
    private Map<String, String> headers;

    @Schema(description = "Тело запроса для POST/PUT запросов", example = "{\"ping\": \"test\"}")
    @JsonProperty("requestBody")
    private Object requestBodyRaw;

    @Size(max = 100)
    @Schema(description = "Тип содержимого Content-Type", example = "application/json")
    private String contentType;

    // Monitoring Configuration
    @Min(value = 30, message = "Интервал должен быть не менее 30 секунд")
    @Max(value = 86400, message = "Интервал должен быть не более 24 часов")
    @Schema(description = "Интервал проверки в секундах", example = "300", minimum = "30", maximum = "86400")
    private Integer intervalSeconds = 300;

    @Min(value = 1000, message = "Таймаут должен быть не менее 1 секунды")
    @Max(value = 300000, message = "Таймаут должен быть не более 5 минут")
    @Schema(description = "Таймаут запроса в миллисекундах", example = "10000", minimum = "1000", maximum = "300000")
    private Integer timeoutMs = 10000;

    @Schema(description = "Идентификатор группы мониторов", example = "123")
    private Long groupId;

    @JsonIgnore
    public String getRequestBodyAsString() {
        if (requestBodyRaw == null) {
            return null;
        }

        if (requestBodyRaw instanceof String) {
            return (String) requestBodyRaw;
        }

        // Если это объект, сериализуем в JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(requestBodyRaw);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Некорректный формат requestBody", e);
        }
    }

    public void setRequestBodyFromString(String requestBody) {
        this.requestBodyRaw = requestBody;
    }
}
