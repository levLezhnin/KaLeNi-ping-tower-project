package team.kaleni.notificationservice.sender;

public interface NotificationSender {
    void send(String recipient, String message);
}
