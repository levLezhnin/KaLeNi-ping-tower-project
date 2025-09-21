package team.kaleni.ping.tower.backend.statistics_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Точка данных графика", description = "Отдельная точка данных для построения графиков мониторинга")
public class ChartDataPointDto {

    @Schema(description = "Время выполнения пинга", example = "2025-09-21 08:15:30", format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pingTimestamp;

    @Schema(description = "Статус проверки (UP/DOWN/ERROR)", example = "UP", allowableValues = {"UP", "DOWN", "ERROR"})
    private String status;

    @Schema(description = "Время отклика в миллисекундах", example = "150", nullable = true)
    private Integer responseTimeMs;

    @Schema(description = "HTTP код ответа", example = "200", nullable = true)
    private Integer responseCode;
}
