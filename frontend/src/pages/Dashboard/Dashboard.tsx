import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useMonitorsStore } from "../../store/useMonitorsStore";
import type { MonitorDetailResponse } from "../../services/monitorTypes";
import AddMonitorModal from "../../components/AddMonitorModal";

function StatusDot({ status }: { status: MonitorDetailResponse["currentStatus"] }) {
  const cls =
    status === "UP"
      ? "bg-green-400"
      : status === "DOWN"
        ? "bg-red-500"
        : status === "PAUSED"
          ? "bg-yellow-400"
          : "bg-gray-400"; // UNKNOWN or undefined
  return <span className={`inline-block w-3 h-3 rounded-full ${cls}`}></span>;
}

export default function Dashboard() {
  const { monitors, fetchAll, remove, enable, disable, loading } = useMonitorsStore();
  const [q, setQ] = useState("");
  const [filter, setFilter] = useState<"all" | "up" | "down">("all");
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [lastUpdate, setLastUpdate] = useState<Date | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const performFetch = async () => {
      await fetchAll();
      setLastUpdate(new Date());
    };

    performFetch();

    // Устанавливаем интервал для автоматического обновления каждые 30 секунд
    const interval = setInterval(() => {
      performFetch();
    }, 30000); // 30 секунд

    // Очищаем интервал при размонтировании компонента
    return () => clearInterval(interval);
  }, [fetchAll]);

  const visible = useMemo(() => {
    let list = [...monitors];
    if (filter === "up") list = list.filter((m) => m.currentStatus === "UP");
    if (filter === "down") list = list.filter((m) => m.currentStatus === "DOWN");
    if (q.trim())
      list = list.filter(
        (m) =>
          m.name.toLowerCase().includes(q.toLowerCase()) ||
          m.url.toLowerCase().includes(q.toLowerCase())
      );
    return list;
  }, [monitors, q, filter]);

  return (
    <div className="max-w-7xl mx-auto p-6 text-[hsl(var(--foreground))]">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold">Мониторинг — URL-адреса</h2>
          {lastUpdate && (
            <div className="flex items-center gap-2 mt-1">
              <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
              <span className="text-sm text-gray-500 dark:text-gray-400">
                Обновлено: {lastUpdate.toLocaleTimeString()}
              </span>
            </div>
          )}
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={async () => {
              await fetchAll();
              setLastUpdate(new Date());
            }}
            className="p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-all duration-200"
            title="Обновить данные"
          >
            <svg className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </button>
          <div className="relative">
            <input
              placeholder="Поиск по названию или URL..."
              value={q}
              onChange={(e) => setQ(e.target.value)}
              className="px-4 py-2 pl-10 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 hover:border-gray-300 dark:hover:border-gray-500"
            />
            <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
              <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
          </div>
          <div className="relative">
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value as any)}
              className="appearance-none px-4 py-2 pr-8 border border-gray-200 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 hover:border-gray-300 dark:hover:border-gray-500 cursor-pointer"
            >
              <option value="all">Все мониторы</option>
              <option value="up">Только доступные</option>
              <option value="down">Только недоступные</option>
            </select>
            <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
              <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>
          <button
            className="ml-4 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg shadow-sm hover:shadow-md transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 font-medium"
            onClick={() => setAddModalOpen(true)}
          >
            <div className="flex items-center gap-2">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              Добавить сайт
            </div>
          </button>
        </div>
      </div>
      {error && (
        <div className="mb-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
          <div className="flex items-start gap-3">
            <svg className="w-5 h-5 text-red-500 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <div className="flex-1">
              <h4 className="text-red-800 dark:text-red-300 text-sm font-medium mb-1">Ошибка операции</h4>
              <p className="text-red-700 dark:text-red-400 text-sm">{error}</p>
            </div>
            <button
              onClick={() => setError(null)}
              className="text-red-500 hover:text-red-700 dark:text-red-400 dark:hover:text-red-300"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      )}

      <AddMonitorModal
        open={addModalOpen}
        onClose={() => setAddModalOpen(false)}
        onCreated={() => {
          setAddModalOpen(false);
          fetchAll();
        }}
      />
      <div className="w-full bg-[hsl(var(--card))] p-4 rounded shadow mb-8">
        <h4 className="font-medium mb-3">Мониторы</h4>
        {loading ? (
          <div>Загрузка...</div>
        ) : (
          <div className="space-y-2">
            {visible.length === 0 && <div className="text-[hsl(var(--muted-foreground))]">Нет мониторов</div>}
            <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
              {visible.map((m) => (
                <div key={m.id} className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-sm hover:shadow-md transition-shadow duration-200 overflow-hidden flex flex-col h-full">
                  {/* Header with status and title */}
                  <div className="p-4 border-b border-gray-100 dark:border-gray-700">
                    <div className="flex items-start justify-between">
                      <div className="flex items-center gap-3 flex-1 min-w-0">
                        <div className="flex-shrink-0">
                          <StatusDot status={m.currentStatus} />
                        </div>
                        <div className="min-w-0 flex-1">
                          <Link
                            to={`/url/${m.id}`}
                            className="font-semibold text-lg text-gray-900 dark:text-white hover:text-blue-600 dark:hover:text-blue-400 transition-colors truncate block"
                          >
                            {m.name}
                          </Link>
                          {m.description && (
                            <p className="text-sm text-gray-600 dark:text-gray-400 mt-1 line-clamp-2">
                              {m.description}
                            </p>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center gap-2 ml-3">
                        <Link
                          to={`/url/${m.id}`}
                          className="px-3 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-700 hover:bg-blue-200 dark:bg-blue-900 dark:text-blue-300 dark:hover:bg-blue-800 transition-colors"
                        >
                          Статистика
                        </Link>
                      </div>
                    </div>
                  </div>

                  {/* Main content */}
                  <div className="p-4 space-y-3 flex-1">
                    {/* URL */}
                    <div className="flex items-center gap-2">
                      <div className="w-2 h-2 rounded-full bg-gray-400 flex-shrink-0"></div>
                      <a
                        href={m.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-sm text-blue-600 dark:text-blue-400 hover:underline truncate flex-1"
                      >
                        {m.url}
                      </a>
                    </div>

                    {/* Method and Content Type */}
                    <div className="flex items-center gap-2 flex-wrap">
                      {m.method && (
                        <span className="inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300">
                          {m.method}
                        </span>
                      )}
                      {m.contentType && (
                        <span className="inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300">
                          {m.contentType}
                        </span>
                      )}
                    </div>

                    {/* Status info */}
                    <div className="grid grid-cols-2 gap-3 text-xs">
                      <div className="space-y-1">
                        <div className="text-gray-500 dark:text-gray-400">Интервал</div>
                        <div className="font-medium text-gray-900 dark:text-gray-100">{m.intervalSeconds}s</div>
                      </div>
                      <div className="space-y-1">
                        <div className="text-gray-500 dark:text-gray-400">Таймаут</div>
                        <div className="font-medium text-gray-900 dark:text-gray-100">{m.timeoutMs}мс</div>
                      </div>
                    </div>

                    {/* Last check info */}
                    {m.lastCheckedAt && (
                      <div className="text-xs text-gray-500 dark:text-gray-400">
                        Последняя проверка: {new Date(m.lastCheckedAt).toLocaleString()}
                      </div>
                    )}

                    {/* Response info */}
                    <div className="flex items-center justify-between text-xs">
                      {typeof m.lastResponseTimeMs === "number" && (
                        <div className="flex items-center gap-1">
                          <div className="w-1.5 h-1.5 rounded-full bg-green-400"></div>
                          <span className="text-gray-600 dark:text-gray-400">{m.lastResponseTimeMs}мс</span>
                        </div>
                      )}
                      {typeof m.lastResponseCode === "number" && (
                        <div className={`px-2 py-1 rounded text-xs font-medium ${m.lastResponseCode >= 200 && m.lastResponseCode < 300
                          ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300'
                          : m.lastResponseCode >= 400
                            ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300'
                            : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300'
                          }`}>
                          {m.lastResponseCode}
                        </div>
                      )}
                    </div>

                    {/* Headers info */}
                    {m.headers && Object.keys(m.headers).length > 0 && (
                      <div className="text-xs text-gray-500 dark:text-gray-400">
                        <div className="font-medium mb-1">Заголовки:</div>
                        <div className="space-y-1">
                          {Object.entries(m.headers).slice(0, 2).map(([k, v]) => (
                            <div key={k} className="truncate">
                              <span className="font-medium">{k}:</span> {v}
                            </div>
                          ))}
                          {Object.keys(m.headers).length > 2 && (
                            <div className="text-gray-400">+{Object.keys(m.headers).length - 2} еще</div>
                          )}
                        </div>
                      </div>
                    )}

                    {/* Request body info */}
                    {m.requestBody && m.method !== "GET" && (
                      <div className="text-xs text-gray-500 dark:text-gray-400">
                        <div className="font-medium mb-1">Тело запроса:</div>
                        <div className="bg-gray-50 dark:bg-gray-700 p-2 rounded text-xs font-mono truncate">
                          {typeof m.requestBody === "object" ? JSON.stringify(m.requestBody) : String(m.requestBody)}
                        </div>
                      </div>
                    )}

                    {/* Error message */}
                    {m.lastErrorMessage && (
                      <div className="p-2 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded text-xs text-red-700 dark:text-red-400">
                        <div className="font-medium mb-1">Ошибка:</div>
                        {m.lastErrorMessage}
                      </div>
                    )}
                  </div>

                  {/* Footer with controls */}
                  <div className="px-4 py-3 bg-gray-50 dark:bg-gray-700/50 border-t border-gray-100 dark:border-gray-700 mt-auto">
                    <div className="flex items-center justify-between">
                      <label className="flex items-center cursor-pointer select-none gap-2">
                        <div className="relative">
                          <input
                            type="checkbox"
                            checked={!!m.enabled}
                            onChange={async () => {
                              try {
                                if (m.enabled) {
                                  await disable(m.id);
                                } else {
                                  await enable(m.id);
                                }
                                setError(null);
                              } catch (e: any) {
                                setError(e?.message || "Не удалось изменить статус монитора");
                              }
                            }}
                            className="peer sr-only"
                          />
                          <div className="w-12 h-6 rounded-full transition bg-gray-300 peer-checked:bg-green-500 flex items-center px-1 box-border">
                            <span className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full shadow transition-all duration-200 ${m.enabled ? 'translate-x-6' : 'translate-x-0'}`}></span>
                          </div>
                        </div>
                        <span className="text-xs text-gray-600 dark:text-gray-400">
                          {m.enabled ? 'Включен' : 'Выключен'}
                        </span>
                      </label>
                      <button
                        onClick={async () => {
                          try {
                            await remove(m.id);
                            setError(null);
                          } catch (e: any) {
                            setError(e?.message || "Не удалось удалить монитор");
                          }
                        }}
                        className="px-3 py-1 text-xs font-medium text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded transition-colors"
                      >
                        Удалить
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}