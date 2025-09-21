package team.kaleni.notificationservice.service;

import net.glxn.qrgen.javase.QRCode;
import org.springframework.stereotype.Service;

@Service
public class QrCodeService {

    public byte[] generateQrCode(String url) {
        return QRCode.from(url).withSize(250, 250).stream().toByteArray();
    }

}
