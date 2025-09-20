package team.kaleni.notificationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.kaleni.notificationservice.service.QrCodeService;
import team.kaleni.notificationservice.service.TelegramService;

@RestController
@RequestMapping("/api/v1/telegramNotifications")
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramService telegramService;
    private final QrCodeService qrCodeService;

    @Operation(
            summary = "Возвращает ссылку для привязки уведомлений в Телеграмм",
            description = """
                    Принимает в себя единственное целое число - id пользователя (просто число в plaintext, не json).
                    
                    Возвращает строку в plaintext - ссылку для привязки уведомлений в телеграмм.
                    """
    )
    @GetMapping("/getSubscribeLink")
    public String getNotificationSubscriptionLink(@RequestBody Long userId) {
        return telegramService.generateSubscriptionLink(userId);
    }

    @Operation(
            summary = "Возвращает QR-код для привязки уведомлений в Телеграмм",
            description = """
                    Принимает в себя единственное целое число - id пользователя (просто число в plaintext, не json).
                    
                    Возвращает массив байт - данные о QR-коде. Его остаётся только визуально отобразить.
                    """
    )
    @GetMapping("/getSubscribeQrCode")
    public byte[] getNotificationSubscriptionQrCode(@RequestBody Long userId) {
        return qrCodeService.generateQrCode(telegramService.generateSubscriptionLink(userId));
    }
}
