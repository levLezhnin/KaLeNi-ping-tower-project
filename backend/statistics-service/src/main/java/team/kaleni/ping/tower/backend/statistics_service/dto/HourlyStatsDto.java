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
@Schema(name = "Почасовая статистика", description = "Агрегированная статистика мониторинга за один час")
public class HourlyStatsDto {

    @Schema(description = "Идентификатор монитора", example = "1")
    private Long monitorId;

    @Schema(description = "Время начала часового интервала", example = "2025-09-21 08:00:00", format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime hour;

    @Schema(description = "Общее количество пингов за час", example = "60")
    private Long totalPings;

    @Schema(description = "Количество успешных пингов", example = "58")
    private Long successfulPings;

    @Schema(description = "Количество неудачных пингов", example = "2")
    private Long failedPings;

    @Schema(description = "Процент времени работы (uptime) в процентах", example = "96.67", minimum = "0", maximum = "100")
    private Double uptimePercentage;

    @Schema(description = "Среднее время отклика в миллисекундах", example = "150.5")
    private Double averageResponseTime;

    @Schema(description = "Минимальное время отклика в миллисекундах", example = "85")
    private Integer minResponseTime;

    @Schema(description = "Максимальное время отклика в миллисекундах", example = "300")
    private Integer maxResponseTime;
}
