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
        const totalInterval = intervalHours * 3600 + intervalMinutes * 60 + intervalSeconds;
        const payload = {
            name,
            description: description.trim() || undefined,
            url,
            method,
            headers: Object.keys(headersObj).length ? headersObj : undefined,
            requestBody: reqBody,
            contentType: contentType.trim() || undefined,
            intervalSeconds: Math.max(1, totalInterval),
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
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
            <div className="bg-white dark:bg-[hsl(var(--card))] rounded-lg shadow-lg p-6 w-full max-w-lg relative">
                <button className="absolute top-2 right-2 text-xl" onClick={onClose}>&times;</button>
                <h3 className="text-xl font-semibold mb-4">Добавить сайт</h3>
                <form className="space-y-3" onSubmit={handleSubmit}>
                    {error && <div className="text-red-600 text-sm mb-2">{error}</div>}
                    <input
                        className="w-full border px-3 py-2 rounded"
                        placeholder="Имя"
                        value={name}
                        onChange={e => setName(e.target.value)}
                        required
                    />
                    <input
                        className="w-full border px-3 py-2 rounded"
                        placeholder="Описание (необязательно)"
                        value={description}
                        onChange={e => setDescription(e.target.value)}
                    />
                    <input
                        className="w-full border px-3 py-2 rounded"
                        placeholder="URL"
                        value={url}
                        onChange={e => setUrl(e.target.value)}
                        required
                    />
                    <div className="flex gap-2">
                        <select
                            className="border px-3 py-2 rounded"
                            value={method}
                            onChange={e => setMethod(e.target.value)}
                        >
                            {HTTP_METHODS.map(m => <option key={m} value={m}>{m}</option>)}
                        </select>
                        <input
                            className="border px-3 py-2 rounded flex-1"
                            placeholder="Content-Type"
                            value={contentType}
                            onChange={e => setContentType(e.target.value)}
                        />
                    </div>
                    {method !== "GET" && (
                        <textarea
                            className="w-full border px-3 py-2 rounded"
                            placeholder="Тело запроса (JSON)"
                            value={requestBody}
                            onChange={e => setRequestBody(e.target.value)}
                            rows={3}
                        />
                    )}
                    <div>
                        <div className="mb-1 font-medium">Заголовки</div>
                        {headers.map((h, i) => (
                            <div key={i} className="flex gap-2 mb-1">
                                <input
                                    className="border px-2 py-1 rounded flex-1"
                                    placeholder="Header"
                                    value={h.key}
                                    onChange={e => {
                                        const arr = [...headers];
                                        arr[i].key = e.target.value;
                                        setHeaders(arr);
                                    }}
                                />
                                <input
                                    className="border px-2 py-1 rounded flex-1"
                                    placeholder="Value"
                                    value={h.value}
                                    onChange={e => {
                                        const arr = [...headers];
                                        arr[i].value = e.target.value;
                                        setHeaders(arr);
                                    }}
                                />
                                <button type="button" onClick={() => setHeaders(headers.filter((_, idx) => idx !== i))} className="text-red-500">×</button>
                            </div>
                        ))}
                        <button type="button" onClick={() => setHeaders([...headers, { key: "", value: "" }])} className="text-blue-600 text-sm">+ Добавить заголовок</button>
                    </div>
                    <div className="flex gap-2 items-end">
                        <div className="flex-1 min-w-0">
                            <label className="block text-sm mb-1">Интервал</label>
                            <div className="flex gap-1">
                                <div className="flex flex-col w-full">
                                    <div className="flex gap-1">
                                        <div className="flex flex-col w-1/3">
                                            <span className="text-xs text-gray-500 mb-1">часы</span>
                                            <input
                                                type="number"
                                                min={0}
                                                className="border px-2 py-2 rounded w-full"
                                                placeholder="часы"
                                                value={intervalHours}
                                                onChange={e => setIntervalHours(Math.max(0, Number(e.target.value)))}
                                            />
                                        </div>
                                        <div className="flex flex-col w-1/3">
                                            <span className="text-xs text-gray-500 mb-1">минуты</span>
                                            <input
                                                type="number"
                                                min={0}
                                                max={59}
                                                className="border px-2 py-2 rounded w-full"
                                                placeholder="мин"
                                                value={intervalMinutes}
                                                onChange={e => setIntervalMinutes(Math.max(0, Math.min(59, Number(e.target.value))))}
                                            />
                                        </div>
                                        <div className="flex flex-col w-1/3">
                                            <span className="text-xs text-gray-500 mb-1">секунды</span>
                                            <input
                                                type="number"
                                                min={1}
                                                max={59}
                                                className="border px-2 py-2 rounded w-full"
                                                placeholder="сек"
                                                value={intervalSeconds}
                                                onChange={e => setIntervalSeconds(Math.max(1, Math.min(59, Number(e.target.value))))}
                                            />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div className="flex-1 min-w-0">
                            <label className="block text-sm mb-1" htmlFor="timeout-input">Таймаут (мс)</label>
                            <input
                                id="timeout-input"
                                type="number"
                                className="border px-3 py-2 rounded w-full"
                                placeholder="Таймаут (мс)"
                                min={3000}
                                value={timeout}
                                onChange={e => setTimeout(Math.max(3000, Number(e.target.value)))}
                            />
                        </div>
                    </div>
                    <button type="submit" className="w-full bg-[hsl(var(--primary))] text-[hsl(var(--primary-foreground))] px-4 py-2 rounded mt-2">Создать</button>
                </form>
            </div>
        </div>
    );
}
