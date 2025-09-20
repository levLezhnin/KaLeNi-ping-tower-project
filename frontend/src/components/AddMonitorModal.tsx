import { useState } from "react";
import { useMonitorsStore } from "../store/useMonitorsStore";

const HTTP_METHODS = ["GET", "POST", "HEAD", "UPDATE", "PATCH", "DELETE"];

export default function AddMonitorModal({ open, onClose, onCreated }: { open: boolean, onClose: () => void, onCreated: () => void }) {
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [url, setUrl] = useState("");
    const [method, setMethod] = useState("GET");
    const [headers, setHeaders] = useState([{ key: "", value: "" }]);
    const [requestBody, setRequestBody] = useState("");
    const [contentType, setContentType] = useState("application/json");
    const [intervalHours, setIntervalHours] = useState(0);
    const [intervalMinutes, setIntervalMinutes] = useState(0);
    const [intervalSeconds, setIntervalSeconds] = useState(30);
    const [timeout, setTimeout] = useState(3000);

    const create = useMonitorsStore(s => s.create);
    const [error, setError] = useState<string | null>(null);
    const resetForm = () => {
        setName(""); setDescription(""); setUrl(""); setMethod("GET"); setHeaders([{ key: "", value: "" }]); setRequestBody(""); setContentType("application/json"); setIntervalHours(0); setIntervalMinutes(0); setIntervalSeconds(30); setTimeout(3000); setError(null);
    };
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        if (!name.trim() || !url.trim()) {
            setError("Имя и URL обязательны");
            return;
        }

        // Валидация интервала
        const totalInterval = intervalHours * 3600 + intervalMinutes * 60 + intervalSeconds;
        if (totalInterval === 0) {
            setError("Интервал проверки не может быть равен 0");
            return;
        }

        let headersObj: Record<string, string> = {};
        headers.forEach(h => {
            if (h.key && h.value) headersObj[h.key] = h.value;
        });
        let reqBody: any = undefined;
        if (method !== "GET" && requestBody.trim()) {
            try {
                reqBody = JSON.parse(requestBody);
            } catch {
                setError("Тело запроса должно быть валидным JSON");
                return;
            }
        }
        const payload = {
            name,
            description: description.trim() || undefined,
            url,
            method,
            headers: Object.keys(headersObj).length ? headersObj : undefined,
            requestBody: reqBody,
            contentType: contentType.trim() || undefined,
            intervalSeconds: totalInterval,
            timeoutMs: Math.max(3000, timeout),
        };
        const res = await create(payload as any);
        if (!res) {
            setError("Не удалось создать монитор");
        } else {
            resetForm();
            onCreated();
        }
    };

    if (!open) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
                    <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Добавить новый монитор</h3>
                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
                    >
                        <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {/* Form */}
                <form className="p-6 space-y-6" onSubmit={handleSubmit}>
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

                    {/* Основная информация */}
                    <div className="space-y-4">
                        <h4 className="text-lg font-medium text-gray-900 dark:text-white">Основная информация</h4>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                                Название *
                            </label>
                            <input
                                className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                placeholder="Введите название монитора"
                                value={name}
                                onChange={e => setName(e.target.value)}
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                                Описание
                            </label>
                            <input
                                className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                placeholder="Описание монитора (необязательно)"
                                value={description}
                                onChange={e => setDescription(e.target.value)}
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                                URL *
                            </label>
                            <input
                                className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                placeholder="https://example.com"
                                value={url}
                                onChange={e => setUrl(e.target.value)}
                                required
                            />
                        </div>
                    </div>

                    {/* Настройки запроса */}
                    <div className="space-y-4">
                        <h4 className="text-lg font-medium text-gray-900 dark:text-white">Настройки запроса</h4>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                                    HTTP метод
                                </label>
                                <div className="relative">
                                    <select
                                        className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 appearance-none pr-8"
                                        value={method}
                                        onChange={e => setMethod(e.target.value)}
                                    >
                                        {HTTP_METHODS.map(m => <option key={m} value={m}>{m}</option>)}
                                    </select>
                                    <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                                        <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                                        </svg>
                                    </div>
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                                    Content-Type
                                </label>
                                <input
                                    className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                    placeholder="application/json"
                                    value={contentType}
                                    onChange={e => setContentType(e.target.value)}
                                />
                            </div>
                        </div>

                        {method !== "GET" && (
                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                                    Тело запроса (JSON)
                                </label>
                                <textarea
                                    className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 font-mono text-sm"
                                    placeholder='{"key": "value"}'
                                    value={requestBody}
                                    onChange={e => setRequestBody(e.target.value)}
                                    rows={4}
                                />
                            </div>
                        )}
                    </div>

                    {/* Заголовки */}
                    <div className="space-y-4">
                        <div className="flex items-center justify-between">
                            <h4 className="text-lg font-medium text-gray-900 dark:text-white">Заголовки</h4>
                            <button
                                type="button"
                                onClick={() => setHeaders([...headers, { key: "", value: "" }])}
                                className="px-3 py-1 text-sm font-medium text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-lg transition-colors"
                            >
                                + Добавить заголовок
                            </button>
                        </div>

                        <div className="space-y-3">
                            {headers.map((h, i) => (
                                <div key={i} className="flex gap-3 items-center">
                                    <input
                                        className="flex-1 px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 text-sm"
                                        placeholder="Название заголовка"
                                        value={h.key}
                                        onChange={e => {
                                            const arr = [...headers];
                                            arr[i].key = e.target.value;
                                            setHeaders(arr);
                                        }}
                                    />
                                    <input
                                        className="flex-1 px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 text-sm"
                                        placeholder="Значение"
                                        value={h.value}
                                        onChange={e => {
                                            const arr = [...headers];
                                            arr[i].value = e.target.value;
                                            setHeaders(arr);
                                        }}
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setHeaders(headers.filter((_, idx) => idx !== i))}
                                        className="p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                                    >
                                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Настройки мониторинга */}
                    <div className="space-y-4">
                        <h4 className="text-lg font-medium text-gray-900 dark:text-white">Настройки мониторинга</h4>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
                                    Интервал проверки
                                </label>
                                <div className="grid grid-cols-3 gap-3">
                                    <div>
                                        <label className="block text-xs text-gray-500 dark:text-gray-400 mb-1">Часы</label>
                                        <input
                                            type="number"
                                            min={0}
                                            className="w-full px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 text-sm"
                                            value={intervalHours}
                                            onChange={e => setIntervalHours(Math.max(0, Number(e.target.value)))}
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-xs text-gray-500 dark:text-gray-400 mb-1">Минуты</label>
                                        <input
                                            type="number"
                                            min={0}
                                            max={59}
                                            className="w-full px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 text-sm"
                                            value={intervalMinutes}
                                            onChange={e => setIntervalMinutes(Math.max(0, Math.min(59, Number(e.target.value))))}
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-xs text-gray-500 dark:text-gray-400 mb-1">Секунды</label>
                                        <input
                                            type="number"
                                            min={intervalHours === 0 && intervalMinutes === 0 ? 1 : 0}
                                            max={59}
                                            className="w-full px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 text-sm"
                                            value={intervalSeconds}
                                            onChange={e => {
                                                const val = Number(e.target.value);
                                                const minVal = (intervalHours === 0 && intervalMinutes === 0) ? 1 : 0;
                                                setIntervalSeconds(Math.max(minVal, Math.min(59, val)));
                                            }}
                                        />
                                    </div>
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                                    Таймаут (мс)
                                </label>
                                <input
                                    type="number"
                                    min={3000}
                                    className="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                    placeholder="3000"
                                    value={timeout}
                                    onChange={e => setTimeout(Math.max(3000, Number(e.target.value)))}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Кнопки */}
                    <div className="flex gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-4 py-3 text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg font-medium transition-all duration-200"
                        >
                            Отмена
                        </button>
                        <button
                            type="submit"
                            className="flex-1 px-4 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                        >
                            Создать монитор
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
