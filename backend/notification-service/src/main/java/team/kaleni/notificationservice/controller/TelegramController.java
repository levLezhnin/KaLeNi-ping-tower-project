package team.kaleni.notificationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import team.kaleni.notificationservice.service.QrCodeService;
import team.kaleni.notificationservice.service.TelegramService;

@RestController
@RequestMapping("/api/v1/telegramNotifications")
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramService telegramService;
    private final QrCodeService qrCodeService;

    @Operation(
            summary = "Возвращает ссылку для привязки уведомлений в Telegram",
            description = """
                    Принимает в себя единственное целое число - id пользователя.
                    
                    Возвращает строку в plaintext - ссылку для привязки уведомлений в Telegram.
                    """
    )
    @GetMapping("/getSubscribeLink/{userId}")
    public String getNotificationSubscriptionLink(@PathVariable Long userId) {
        return telegramService.generateSubscriptionLink(userId);
    }

    @Operation(
            summary = "Возвращает QR-код для привязки уведомлений в Telegram",
            description = """
                    Принимает в себя единственное целое число - id пользователя.
                    
                    Возвращает массив байт - данные о QR-коде. Его остаётся только визуально отобразить.
                    """
    )
    @GetMapping("/getSubscribeQrCode/{userId}")
    public byte[] getNotificationSubscriptionQrCode(@PathVariable Long userId) {
        return qrCodeService.generateQrCode(telegramService.generateSubscriptionLink(userId));
    }
}
