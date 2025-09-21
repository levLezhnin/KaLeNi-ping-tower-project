package team.kaleni.notificationservice.config;

public class TelegramBotResponses {
    private static final String TUTORIAL_SUBSCRIBE =
            """
            📋 Для того, чтобы привязать уведомления к Telegram Вам необходимо:
            • Перейти на сайт сервиса Ping Tower перейти в раздел ... и воспользоваться там...
            
            Как только Вы нажмёте на ссылку мы снова встретимся, и я смогу помочь Вам отслеживать интересующие ресурсы. 😉
            """;

    private static final String TUTORIAL_UNSUBSCRIBE =
                    """
                    ⚡️ Управляйте уведомлениями одним кликом - просто нажмите на эту команду: /unsubscribe
                    
                    Настройки уведомлений всегда под Вашим контролем - их в любой момент можно изменить!
                    """;

    private static final String BOT_ABILITIES = """
            📋 Что я умею:
            • Оповещать о недоступности сервисов
            • Сообщать о восстановлении работы сервисов
            • Оповещать об увеличении времени отклика сервиса
            """;

    public static final String MSG_BAD_REQUEST = """
            Привет! 👋
            К сожалению я не смог распознать Ваш запрос. 🤔
            
            """ + BOT_ABILITIES + """
            
            """ + TUTORIAL_SUBSCRIBE + """
            
            """ + TUTORIAL_UNSUBSCRIBE;

    public static final String MSG_SUBSCRIBE_OK = """
            Привет! 👋
            Рад, что вы подписались на рассылку уведомлений через Telegram!
            
            Я - Ваш персональный помощник, который будет своевременно сообщать об изменениях в поведении волнующих Вас сервисов.
            
            """ + BOT_ABILITIES + """
            
            """ + TUTORIAL_UNSUBSCRIBE;

    public static final String MSG_SUBSCRIBE_NOT_OK = """
            Привет! 👋
            Вижу, что Вы хотите подписаться на рассылку уведомлений в Telegram. 👀
            
            """ + TUTORIAL_SUBSCRIBE;

    public static final String MSG_SUBSCRIBE_FAILED = """
            Привет! 👋
            Вижу, что Вы хотите подписаться на рассылку уведомлений в Telegram. 👀
            
            К сожалению, произошла ошибка при обработке Вашего запроса. 🥺
            Рекомендую попробовать сделать привязку ещё раз.
            
            """ + TUTORIAL_SUBSCRIBE;

    public static final String MSG_UNSUBSCRIBE_OK = """
            Привет! 👋
            Вы успешно отписались от рассылки уведомлений в Telegram.
            Надеюсь, что Вы ещё вернётесь. 🥺
            """;

    public static final String MSG_UNSUBSCRIBE_NOT_OK = """
            Привет! 👋
            Вижу, что Вы хотите отписаться от рассылки уведомлений. 👀
            
            К сожалению, произошла ошибка при обработке Вашего запроса. 🥺
            Рекомендую попробовать повторить запрос ещё раз.
            
            """ + TUTORIAL_UNSUBSCRIBE;

    public static final String MSG_NOTIFICATION_PATTERN = """
            🔔 Вам пришло уведомление:
            %s
            """;
}
