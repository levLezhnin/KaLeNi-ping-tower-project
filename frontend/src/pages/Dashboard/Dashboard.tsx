import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useMonitorsStore } from "../../store/useMonitorsStore";
import { useGroupsStore } from "../../store/useGroupsStore";
import type { CreateMonitorRequest, MonitorDetailResponse } from "../../services/monitorTypes";

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
  const { monitors, fetchAll, create, remove, update, enable, disable, loading, error } = useMonitorsStore();
  const { groups, fetchAll: fetchGroups } = useGroupsStore();
  const [q, setQ] = useState("");
  const [filter, setFilter] = useState<"all" | "up" | "down">("all");
  const [name, setName] = useState("");
  const [url, setUrl] = useState("");
  const [interval, setInterval] = useState<number>(60);
  const [submitting, setSubmitting] = useState(false);
  const [addError, setAddError] = useState<string | null>(null);

  useEffect(() => {
    fetchAll();
    fetchGroups();
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

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !url) return;
    setSubmitting(true);

    // Собираем payload
    const payload: CreateMonitorRequest = {
      name,
      description: "",
      url,
      intervalSeconds: Math.max(30, interval),
      timeoutMs: 1000,
    };

    // Добавляем groupId только если он задан и не 0
    // if (groupId && groupId > 0) {
    //   payload.groupId = groupId;
    // }

    const res = await create(payload);

    if (!res) {
      setAddError(error || "Не удалось создать монитор");
    } else {
      setAddError(null);
      setName(""); setUrl(""); setInterval(60);
    }

    setSubmitting(false);
  };

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
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        {/* Groups Panel hidden for now */}
        {false && (
          <div className="bg-[hsl(var(--card))] p-4 rounded shadow"></div>
        )}
        <div className="bg-[hsl(var(--card))] p-4 rounded shadow">
          <h4 className="font-medium mb-2">Добавить URL</h4>
          <form onSubmit={handleAdd} className="space-y-2">
            {addError && <div className="text-sm text-red-600">{addError}</div>}
            <input
              placeholder="Название"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full border border-[hsl(var(--border))] px-3 py-2 rounded bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
            />
            <input
              placeholder="https://..."
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              className="w-full border border-[hsl(var(--border))] px-3 py-2 rounded bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
            />
            <div className="flex items-center gap-2">
              <input
                type="number"
                value={interval}
                onChange={(e) => setInterval(Number(e.target.value))}
                className="border border-[hsl(var(--border))] px-3 py-2 rounded w-28 bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
              />
              <span className="text-sm text-[hsl(var(--muted-foreground))]">секунд</span>
            </div>
            <button disabled={submitting} className="bg-[hsl(var(--primary))] text-[hsl(var(--primary-foreground))] px-4 py-2 rounded disabled:opacity-60">
              {submitting ? "Добавляю..." : "Добавить"}
            </button>
          </form>
        </div>

        <div className="col-span-2 bg-[hsl(var(--card))] p-4 rounded shadow">
          <h4 className="font-medium mb-3">Мониторы</h4>
          {loading ? (
            <div>Загрузка...</div>
          ) : (
            <div className="space-y-2">
              {visible.length === 0 && <div className="text-[hsl(var(--muted-foreground))]">Нет мониторов</div>}
              {visible.map((m) => (
                <div key={m.id} className="flex items-center justify-between p-3 border border-[hsl(var(--border))] rounded">
                  <div className="flex items-center gap-3">
                    <StatusDot status={m.currentStatus} />
                    <div>
                      <Link to={`/url/${m.id}`} className="font-medium">{m.name}</Link>
                      <div className="text-sm text-[hsl(var(--muted-foreground))]">{m.url}</div>
                      <div className="text-xs text-[hsl(var(--muted-foreground))]">Группа: {m.groupId ? (groups.find(g => g.id === m.groupId)?.name || `#${m.groupId}`) : "без группы"}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 text-sm">
                    <div className="text-[hsl(var(--muted-foreground))]">{m.intervalSeconds}s</div>
                    <select
                      value={m.groupId ?? ''}
                      onChange={async (e) => {
                        const val = e.target.value;
                        await update(m.id, { groupId: val ? Number(val) : undefined });
                      }}
                      className="px-2 py-1 border border-[hsl(var(--border))] rounded bg-[hsl(var(--card))] text-[hsl(var(--card-foreground))]"
                    >
                      <option value=''>Без группы</option>
                      {/* {Array.isArray(groups) && groups.map(g => (
                        <option key={g.id} value={g.id}>{g.name}</option>
                      ))} */}
                    </select>
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
    </div>
  );
}