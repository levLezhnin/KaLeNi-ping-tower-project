import { useState } from "react";
import { authService } from "../../services/authService";
import api from "../../services/apiClient";
import TelegramBotModal from "../../components/TelegramBotModal";
import { telegramService } from "../../services/telegramService";

export default function Profile() {
    const [username, setUsername] = useState(authService.getUsername() || "");
    const [password, setPassword] = useState("");
    const [password2, setPassword2] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const [telegramModalOpen, setTelegramModalOpen] = useState(false);
    const [telegramMode, setTelegramMode] = useState<'connect' | 'disconnect'>('connect');
    const [telegramLink, setTelegramLink] = useState("");
    const [telegramQr, setTelegramQr] = useState("");
    const isSubscribed = !!localStorage.getItem("telegram_subscribed");

    const userId = authService.getOwnerId();
    const email = authService.getEmail();

    const validatePassword = (pwd: string) => {
        if (pwd.length <= 8) return "Пароль должен быть длиннее 8 символов";
        if (!/[A-Z]/.test(pwd)) return "Пароль должен содержать хотя бы одну заглавную букву";
        if (!/[0-9]/.test(pwd)) return "Пароль должен содержать хотя бы одну цифру";
        if (!/[!@#$%^&*()_+\-=[\]{};':\"\\|,.<>/?]/.test(pwd)) return "Пароль должен содержать хотя бы один специальный символ";
        if (!/^[A-Za-z0-9!@#$%^&*()_+\-=[\]{};':\"\\|,.<>/?]+$/.test(pwd)) return "Пароль должен содержать только латинские буквы, цифры и спецсимволы";
        return null;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        if (!username.trim()) {
            setError("Имя пользователя обязательно");
            return;
        }
        if (password) {
            const pwdErr = validatePassword(password);
            if (pwdErr) {
                setError(pwdErr);
                return;
            }
            if (password !== password2) {
                setError("Пароли не совпадают");
                return;
            }
        }
        setLoading(true);
        try {
            const payload: any = { username };
            if (password) payload.password = password;
            const { data } = await api.put(`/api/v1/users/update/${userId}`, payload);
            if (data && data.id) {
                authService.setUsername(data.username);
                setSuccess("Данные успешно обновлены");
                setPassword("");
                setPassword2("");
            } else {
                setError("Не удалось обновить данные пользователя");
            }
        } catch (e: any) {
            setError(e?.response?.data?.message || e?.response?.data?.error || e.message || "Ошибка обновления");
        } finally {
            setLoading(false);
        }
    };

    const openTelegramModal = async (mode: 'connect' | 'disconnect') => {
        setTelegramMode(mode);
        setTelegramModalOpen(true);
        const userId = authService.getOwnerId();
        const [link, qr] = await Promise.all([
            telegramService.getSubscribeLink(userId),
            telegramService.getSubscribeQrCode(userId),
        ]);
        setTelegramLink(link);
        setTelegramQr(qr);
    };

    return (
        <div className="max-w-2xl mx-auto p-6">
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
                {/* Header */}
                <div className="bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-8">
                    <div className="flex items-center gap-4">
                        <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center">
                            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                            </svg>
                        </div>
                        <div>
                            <h1 className="text-2xl font-bold text-white">Личный кабинет</h1>
                            <p className="text-blue-100 mt-1">Управление профилем и настройками</p>
                        </div>
                    </div>
                </div>

                {/* Content */}
                <div className="p-6">
                    {/* User Info */}
                    <div className="mb-6 p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                        <div className="flex items-center gap-3">
                            <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                            </svg>
                            <div>
                                <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Email</span>
                                <div className="text-gray-900 dark:text-white font-mono">{email}</div>
                            </div>
                        </div>
                        <div className="mt-4">
                            <button
                                className={`px-4 py-2 rounded-lg font-medium transition-all duration-200 ${isSubscribed ? 'bg-red-600 hover:bg-red-700 text-white' : 'bg-blue-600 hover:bg-blue-700 text-white'}`}
                                onClick={() => openTelegramModal(isSubscribed ? 'disconnect' : 'connect')}
                            >
                                {isSubscribed ? 'Отключить уведомления' : 'Подключить уведомления'}
                            </button>
                        </div>
                    </div>

                    {/* Form */}
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {error && (
                            <div className="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                                <div className="flex items-center gap-2">
                                    <svg className="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                    </svg>
                                    <span className="text-red-700 dark:text-red-400 text-sm font-medium">{error}</span>
                                </div>
                            </div>
                        )}

                        {success && (
                            <div className="p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
                                <div className="flex items-center gap-2">
                                    <svg className="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                                    </svg>
                                    <span className="text-green-700 dark:text-green-400 text-sm font-medium">{success}</span>
                                </div>
                            </div>
                        )}

                        {/* Username */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                                Имя пользователя *
                            </label>
                            <input
                                className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                placeholder="Введите имя пользователя"
                                value={username}
                                onChange={e => setUsername(e.target.value)}
                                required
                            />
                        </div>

                        {/* Password Section */}
                        <div className="space-y-4">
                            <div className="flex items-center gap-2">
                                <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                                </svg>
                                <h3 className="text-lg font-medium text-gray-900 dark:text-white">Смена пароля</h3>
                            </div>
                            <p className="text-sm text-gray-500 dark:text-gray-400">Оставьте поля пустыми, если не хотите менять пароль</p>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                                    Новый пароль
                                </label>
                                <input
                                    type="password"
                                    className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                    placeholder="Введите новый пароль"
                                    value={password}
                                    onChange={e => setPassword(e.target.value)}
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                                    Подтвердите новый пароль
                                </label>
                                <input
                                    type="password"
                                    className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                    placeholder="Повторите новый пароль"
                                    value={password2}
                                    onChange={e => setPassword2(e.target.value)}
                                />
                            </div>

                            {/* Password Requirements */}
                            {password && (
                                <div className="p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
                                    <h4 className="text-sm font-medium text-blue-900 dark:text-blue-300 mb-2">Требования к паролю:</h4>
                                    <ul className="text-xs text-blue-800 dark:text-blue-400 space-y-1">
                                        <li className={`flex items-center gap-2 ${password.length > 8 ? 'text-green-600' : ''}`}>
                                            <svg className={`w-3 h-3 ${password.length > 8 ? 'text-green-500' : 'text-gray-400'}`} fill="currentColor" viewBox="0 0 20 20">
                                                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                            </svg>
                                            Длина более 8 символов
                                        </li>
                                        <li className={`flex items-center gap-2 ${/[A-Z]/.test(password) ? 'text-green-600' : ''}`}>
                                            <svg className={`w-3 h-3 ${/[A-Z]/.test(password) ? 'text-green-500' : 'text-gray-400'}`} fill="currentColor" viewBox="0 0 20 20">
                                                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                            </svg>
                                            Заглавная буква
                                        </li>
                                        <li className={`flex items-center gap-2 ${/[0-9]/.test(password) ? 'text-green-600' : ''}`}>
                                            <svg className={`w-3 h-3 ${/[0-9]/.test(password) ? 'text-green-500' : 'text-gray-400'}`} fill="currentColor" viewBox="0 0 20 20">
                                                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                            </svg>
                                            Цифра
                                        </li>
                                        <li className={`flex items-center gap-2 ${/[!@#$%^&*()_+\-=[\]{};':\"\\|,.<>/?]/.test(password) ? 'text-green-600' : ''}`}>
                                            <svg className={`w-3 h-3 ${/[!@#$%^&*()_+\-=[\]{};':\"\\|,.<>/?]/.test(password) ? 'text-green-500' : 'text-gray-400'}`} fill="currentColor" viewBox="0 0 20 20">
                                                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                            </svg>
                                            Специальный символ
                                        </li>
                                    </ul>
                                </div>
                            )}
                        </div>

                        {/* Submit Button */}
                        <div className="pt-4">
                            <button
                                type="submit"
                                className="w-full bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed"
                                disabled={loading}
                            >
                                {loading ? (
                                    <div className="flex items-center justify-center gap-2">
                                        <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                        </svg>
                                        Сохраняем...
                                    </div>
                                ) : (
                                    "Сохранить изменения"
                                )}
                            </button>
                        </div>
                    </form>
                    <TelegramBotModal
                        open={telegramModalOpen}
                        onClose={() => setTelegramModalOpen(false)}
                        mode={telegramMode}
                        link={telegramLink}
                        qrCode={telegramQr}
                        onUsed={telegramMode === 'connect' ? () => localStorage.setItem("telegram_subscribed", "1") : undefined}
                    />
                </div>
            </div>
        </div>
    );
}
