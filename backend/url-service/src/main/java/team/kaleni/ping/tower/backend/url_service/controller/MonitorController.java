package team.kaleni.ping.tower.backend.url_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.kaleni.ping.tower.backend.url_service.dto.request.monitor.CreateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.request.monitor.UpdateMonitorRequest;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.DeleteResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorDetailResponse;
import team.kaleni.ping.tower.backend.url_service.dto.response.monitor.MonitorResponse;
import team.kaleni.ping.tower.backend.url_service.service.MonitorService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/monitors")
@RequiredArgsConstructor
@Tag(name = "Мониторинг - Управление", description = "API для управления мониторами серверов и веб-сервисов")
public class MonitorController {

    private final MonitorService monitorService;

    @Operation(
            summary = "Создание нового монитора",
            description = "Создает новый монитор для авторизованного пользователя. URL нормализуется и переиспользуется при совпадении"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Монитор успешно создан",
                    content = @Content(schema = @Schema(implementation = MonitorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации или бизнес-логики",
                    content = @Content
            )
    })
    @PostMapping("register")
    public ResponseEntity<MonitorResponse> createMonitor(
            @Parameter(description = "Идентификатор владельца (временно, будет заменено на аутентификацию)", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId,
            @Valid @RequestBody CreateMonitorRequest request) {

        MonitorResponse response = monitorService.createMonitor(ownerId, request);
        if (response.isResult()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(
            summary = "Получение монитора по идентификатору",
            description = "Возвращает детальную информацию о конкретном мониторе"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Монитор найден",
                    content = @Content(schema = @Schema(implementation = MonitorDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Монитор не найден или не принадлежит пользователю",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<MonitorDetailResponse> getMonitorById(
            @Parameter(description = "Идентификатор монитора", example = "123")
            @PathVariable Long id,
            @Parameter(description = "Идентификатор владельца", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId) {

        MonitorDetailResponse response = monitorService.getMonitorById(ownerId, id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Получение всех мониторов пользователя",
            description = "Возвращает список всех мониторов для авторизованного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Мониторы успешно получены"
            )
    })
    @GetMapping
    public ResponseEntity<List<MonitorDetailResponse>> getAllMonitors(
            @Parameter(description = "Идентификатор владельца", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId) {

        List<MonitorDetailResponse> monitors = monitorService.getAllMonitors(ownerId);
        return ResponseEntity.ok(monitors);
    }

    @Operation(
            summary = "Обновление существующего монитора",
            description = "Обновляет конфигурацию существующего монитора, включая настройки и принадлежность к группе"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Монитор успешно обновлен",
                    content = @Content(schema = @Schema(implementation = MonitorDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации или некорректные данные",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Монитор не найден или не принадлежит пользователю",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<MonitorDetailResponse> updateMonitor(
            @Parameter(description = "Идентификатор монитора для обновления", example = "123")
            @PathVariable Long id,
            @Parameter(description = "Идентификатор владельца", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId,
            @Valid @RequestBody UpdateMonitorRequest request) {

        MonitorDetailResponse response = monitorService.updateMonitor(ownerId, id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Удаление монитора",
            description = "Удаляет монитор и при необходимости очищает неиспользуемые целевые URL"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Монитор успешно удален"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Монитор не найден или не принадлежит пользователю",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> deleteMonitor(
            @Parameter(description = "Идентификатор монитора для удаления", example = "123")
            @PathVariable Long id,
            @Parameter(description = "Идентификатор владельца", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId) {

        monitorService.deleteMonitor(ownerId, id);

        DeleteResponse response = DeleteResponse.builder()
                .success(true)
                .deletedId(id)
                .deletedAt(Instant.now())
                .message("Монитор успешно удален")
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Включение монитора",
            description = "Активирует монитор для выполнения проверок доступности"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Монитор успешно включен"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Монитор не найден",
                    content = @Content
            )
    })
    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enableMonitor(
            @Parameter(description = "Идентификатор монитора", example = "123")
            @PathVariable Long id,
            @Parameter(description = "Идентификатор владельца", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId) {

        monitorService.setMonitorEnabled(ownerId, id, true);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Отключение монитора",
            description = "Деактивирует монитор, прекращая выполнение проверок доступности"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Монитор успешно отключен"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Монитор не найден",
                    content = @Content
            )
    })
    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disableMonitor(
            @Parameter(description = "Идентификатор монитора", example = "123")
            @PathVariable Long id,
            @Parameter(description = "Идентификатор владельца", example = "42")
            @RequestHeader("X-Owner-Id") Integer ownerId) {

        monitorService.setMonitorEnabled(ownerId, id, false);
        return ResponseEntity.ok().build();
    }
}
