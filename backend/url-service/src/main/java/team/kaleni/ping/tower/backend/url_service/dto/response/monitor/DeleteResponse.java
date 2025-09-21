package team.kaleni.ping.tower.backend.url_service.dto.response.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(name = "Ответ удаления монитора", description = "Результат операции удаления монитора")
public class DeleteResponse {

    @Schema(description = "Флаг успешности операции", example = "true")
    private boolean success;

    @Schema(description = "Идентификатор удаленного монитора", example = "123")
    private Long deletedId;

    @Schema(description = "Название удаленного монитора", example = "Проверка API здоровья")
    private String deletedName;

    @Schema(description = "Время удаления монитора", example = "2025-09-21T08:30:00.000Z", format = "date-time")
    private Instant deletedAt;

    @Schema(description = "Количество удаленных записей пингов", example = "1247")
    private Integer deletedRecords;

    @Schema(description = "Дополнительная информация об очистке", example = "Монитор успешно удален из всех групп")
    private String message;
}
