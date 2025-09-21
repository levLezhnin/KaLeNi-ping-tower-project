package team.kaleni.ping.tower.backend.url_service.dto.response.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import team.kaleni.ping.tower.backend.url_service.entity.HttpMethod;

@Data
@Builder
@Schema(name = "Ответ создания монитора", description = "Результат операции создания нового монитора")
public class MonitorResponse {

    @Schema(description = "Результат операции создания", example = "true")
    private boolean result;

    @Schema(description = "Идентификатор созданного монитора", example = "123")
    private Long id;

    @Schema(description = "Название монитора", example = "Проверка API здоровья")
    private String name;

    @Schema(description = "URL монитора", example = "https://api.example.com/health")
    private String url;

    @Schema(description = "HTTP метод запроса", example = "GET", allowableValues = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    private HttpMethod method;

    @Schema(description = "Интервал проверки в секундах", example = "300")
    private Integer intervalSeconds;

    @Schema(description = "Идентификатор группы мониторов", example = "456")
    private Long groupId;

    @Schema(description = "Статус активности монитора", example = "true")
    private Boolean enabled;

    @Schema(description = "Сообщение об ошибке при неудачном создании", example = "Некорректный формат URL")
    private String errorMessage;
}
