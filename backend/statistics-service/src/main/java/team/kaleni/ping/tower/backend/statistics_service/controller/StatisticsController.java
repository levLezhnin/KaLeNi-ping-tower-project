package team.kaleni.ping.tower.backend.statistics_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.kaleni.ping.tower.backend.statistics_service.dto.ChartDataPointDto;
import team.kaleni.ping.tower.backend.statistics_service.dto.HourlyStatsDto;
import team.kaleni.ping.tower.backend.statistics_service.service.StatisticsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Статистика - Мониторинг", description = "API для получения статистики мониторинга серверов")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(
            summary = "Получение почасовой статистики мониторинга за последние 24 часа",
            description = "Возвращает агрегированную статистику по часам за последние 24 часа для указанного монитора"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Статистика успешно получена",
                    content = @Content(schema = @Schema(implementation = HourlyStatsDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Монитор не найден"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping("/monitors/{monitorId}/hourly/24h")
    public ResponseEntity<List<HourlyStatsDto>> getHourlyStatistics24h(
            @Parameter(description = "Идентификатор монитора", example = "1")
            @PathVariable Long monitorId) {

        log.info("Getting hourly statistics for monitor {} for last 24h", monitorId);

        try {
            List<HourlyStatsDto> stats = statisticsService.getHourlyStatistics24h(monitorId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting hourly statistics for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "Получение почасовой статистики мониторинга за указанный период",
            description = "Возвращает агрегированную статистику по часам за произвольный временной период"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Статистика успешно получена",
                    content = @Content(schema = @Schema(implementation = HourlyStatsDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Монитор не найден"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping("/monitors/{monitorId}/hourly")
    public ResponseEntity<List<HourlyStatsDto>> getHourlyStatistics(
            @Parameter(description = "Идентификатор монитора", example = "1")
            @PathVariable Long monitorId,
            @Parameter(description = "Начало периода", example = "2025-09-20T07:00:00")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "Конец периода", example = "2025-09-21T07:00:00")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime) {

        log.info("Getting hourly statistics for monitor {} from {} to {}", monitorId, startTime, endTime);

        try {
            List<HourlyStatsDto> stats = statisticsService.getHourlyStatistics(monitorId, startTime, endTime);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting hourly statistics for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "Получение данных для построения графика за последние 24 часа",
            description = "Возвращает детальные данные всех пингов за последние 24 часа для построения графиков"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные графика успешно получены",
                    content = @Content(schema = @Schema(implementation = ChartDataPointDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Монитор не найден"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping("/monitors/{monitorId}/chart/24h")
    public ResponseEntity<List<ChartDataPointDto>> getChartData24h(
            @Parameter(description = "Идентификатор монитора", example = "1")
            @PathVariable Long monitorId) {

        log.info("Getting chart data for monitor {} for last 24h", monitorId);

        try {
            List<ChartDataPointDto> data = statisticsService.getChartData24h(monitorId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error getting chart data for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "Получение данных для построения графика за указанный период",
            description = "Возвращает детальные данные всех пингов за произвольный временной период для построения графиков"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные графика успешно получены",
                    content = @Content(schema = @Schema(implementation = ChartDataPointDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Монитор не найден"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping("/monitors/{monitorId}/chart")
    public ResponseEntity<List<ChartDataPointDto>> getChartData(
            @Parameter(description = "Идентификатор монитора", example = "1")
            @PathVariable Long monitorId,
            @Parameter(description = "Начало периода", example = "2025-09-20T07:00:00")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "Конец периода", example = "2025-09-21T07:00:00")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime) {

        log.info("Getting chart data for monitor {} from {} to {}", monitorId, startTime, endTime);

        try {
            List<ChartDataPointDto> data = statisticsService.getChartData(monitorId, startTime, endTime);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error getting chart data for monitor {}: {}", monitorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
