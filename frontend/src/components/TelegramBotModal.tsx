import React from "react";

interface TelegramBotModalProps {
    open: boolean;
    onClose: () => void;
    mode: "connect" | "disconnect";
    link: string;
    qrCode: string; // base64 string
    onUsed?: () => void;
}

const connectText = (
    <>
        <h3 className="text-xl font-semibold mb-2">Получайте уведомления в Telegram</h3>
        <p className="mb-4 text-gray-700 dark:text-gray-300">
            Подключите Telegram-бота, чтобы получать мгновенные уведомления, если ваш сайт недоступен. Просто отсканируйте QR-код или перейдите по ссылке ниже.
        </p>
    </>
);

const disconnectText = (
    <>
        <h3 className="text-xl font-semibold mb-2">Отключение уведомлений</h3>
        <p className="mb-4 text-gray-700 dark:text-gray-300">
            Вы уже подключили Telegram-бота. Для отключения уведомлений отпишитесь в самом боте. Ссылка и QR-код для перехода:
        </p>
    </>
);

const TelegramBotModal: React.FC<TelegramBotModalProps> = ({ open, onClose, mode, link, qrCode, onUsed }) => {
    if (!open) return null;
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-md mx-4 max-h-[90vh] overflow-y-auto p-8 relative">
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
                >
                    <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
                {mode === "connect" ? connectText : disconnectText}
                <div className="flex flex-col items-center gap-4">
                    <img
                        src={`data:image/png;base64,${qrCode}`}
                        alt="QR для Telegram"
                        className="w-40 h-40 object-contain border border-gray-200 dark:border-gray-700 rounded-lg bg-white"
                    />
                    <a
                        href={link}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-blue-600 dark:text-blue-400 underline break-all text-center font-semibold text-lg"
                    >
                        Ссылка на бота
                    </a>
                </div>
                <div className="mt-8 flex gap-3">
                    {mode === "connect" ? (
                        <button
                            className="flex-1 px-4 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-all duration-200"
                            onClick={() => { onUsed && onUsed(); onClose(); }}
                        >
                            Я подключил
                        </button>
                    ) : (
                        <button
                            className="flex-1 px-4 py-3 bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg font-medium transition-all duration-200"
                            onClick={onClose}
                        >
                            Ок
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default TelegramBotModal;
