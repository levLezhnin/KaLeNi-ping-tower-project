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
        : "bg-[hsl(var(--muted-foreground))]";
  return <span className={`inline-block w-3 h-3 rounded-full ${cls}`}></span>;
}

export default function Dashboard() {
  const { monitors, fetchAll, remove, enable, disable, loading } = useMonitorsStore();
  const [q, setQ] = useState("");
  const [filter, setFilter] = useState<"all" | "up" | "down">("all");
  const [addModalOpen, setAddModalOpen] = useState(false);

  useEffect(() => {
    fetchAll();
  }, []);

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
        <h2 className="text-2xl font-semibold">Мониторинг — URL-адреса</h2>
        <div className="flex items-center gap-3">
          <input
            placeholder="Поиск..."
            value={q}
            onChange={(e) => setQ(e.target.value)}
            className="px-3 py-2 border border-[hsl(var(--border))] rounded bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
          />
          <select
            value={filter}
            onChange={(e) => setFilter(e.target.value as any)}
            className="px-3 py-2 border border-[hsl(var(--border))] rounded bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
          >
            <option value="all">Все</option>
            <option value="up">Только доступные</option>
            <option value="down">Только недоступные</option>
          </select>
          <button
            className="ml-4 bg-[hsl(var(--primary))] text-[hsl(var(--primary-foreground))] px-4 py-2 rounded shadow hover:bg-[hsl(var(--primary-dark))]"
            onClick={() => setAddModalOpen(true)}
          >
            Добавить сайт
          </button>
        </div>
      </div>
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
            {visible.map((m) => (
              <div key={m.id} className="flex flex-col md:flex-row md:items-center justify-between p-3 border border-[hsl(var(--border))] rounded gap-2">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-3">
                    <StatusDot status={m.currentStatus} />
                    <div className="truncate">
                      <div className="flex items-center gap-2">
                        <Link to={`/url/${m.id}`} className="font-medium truncate">{m.name}</Link>
                        {m.method && <span className="text-xs px-2 py-0.5 rounded bg-[hsl(var(--muted))] text-[hsl(var(--muted-foreground))]">{m.method}</span>}
                        {m.contentType && <span className="text-xs px-2 py-0.5 rounded bg-[hsl(var(--muted))] text-[hsl(var(--muted-foreground))]">{m.contentType}</span>}
                      </div>
                      {m.description && <div className="text-xs text-[hsl(var(--muted-foreground))] truncate">{m.description}</div>}
                      <div className="text-sm text-[hsl(var(--muted-foreground))] truncate">{m.url}</div>
                      <div className="text-xs text-[hsl(var(--muted-foreground))] flex gap-2 flex-wrap">
                        <span>Интервал: {m.intervalSeconds}s</span>
                        <span>Таймаут: {m.timeoutMs}мс</span>
                        {m.lastCheckedAt && <span>Проверен: {new Date(m.lastCheckedAt).toLocaleString()}</span>}
                      </div>
                    </div>
                  </div>
                  <div className="flex flex-wrap gap-2 mt-1 text-xs text-[hsl(var(--muted-foreground))]">
                    {m.headers && Object.keys(m.headers).length > 0 && (
                      <span>Headers: {Object.entries(m.headers).map(([k, v]) => `${k}: ${v}`).join(", ")}</span>
                    )}
                    {m.requestBody && m.method !== "GET" && (
                      <span>Body: {typeof m.requestBody === "object" ? JSON.stringify(m.requestBody) : String(m.requestBody)}</span>
                    )}
                  </div>
                  <div className="flex flex-wrap gap-4 mt-1 text-xs text-[hsl(var(--muted-foreground))]">
                    {typeof m.lastResponseTimeMs === "number" && <span>Время ответа: {m.lastResponseTimeMs}мс</span>}
                    {typeof m.lastResponseCode === "number" && <span>Код: {m.lastResponseCode}</span>}
                    {m.lastErrorMessage && <span className="text-red-500">Ошибка: {m.lastErrorMessage}</span>}
                  </div>
                </div>
                <div className="flex items-center gap-3 text-sm mt-2 md:mt-0">
                  {m.enabled ? (
                    <button onClick={() => disable(m.id)} className="px-2 py-1 border border-[hsl(var(--border))] rounded">Отключить</button>
                  ) : (
                    <button onClick={() => enable(m.id)} className="px-2 py-1 border border-[hsl(var(--border))] rounded">Включить</button>
                  )}
                  <button onClick={() => remove(m.id)} className="px-2 py-1 border border-[hsl(var(--border))] rounded text-[hsl(var(--destructive))]">Удалить</button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}