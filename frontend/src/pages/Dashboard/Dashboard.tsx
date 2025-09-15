import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useUrlsStore } from "../../store/useUrlsStore";
import type { UrlItem } from "../../types/index";

function StatusDot({ status }: { status: UrlItem["status"] }) {
  const cls =
    status === "up"
      ? "bg-green-400"
      : status === "down"
      ? "bg-red-500"
      : "bg-[hsl(var(--muted-foreground))]";
  return <span className={`inline-block w-3 h-3 rounded-full ${cls}`}></span>;
}

export default function Dashboard() {
  const { urls, fetchAll, addUrl, removeUrl, updateUrl, checkNow, loading } = useUrlsStore();
  const [q, setQ] = useState("");
  const [filter, setFilter] = useState<"all" | "up" | "down" | "pinned">("all");
  const [name, setName] = useState("");
  const [url, setUrl] = useState("");
  const [interval, setInterval] = useState<number>(60);

  useEffect(() => {
    fetchAll();
  }, []);

  const visible = useMemo(() => {
    let list = [...urls];
    if (filter === "up") list = list.filter((u) => u.status === "up");
    if (filter === "down") list = list.filter((u) => u.status === "down");
    if (filter === "pinned") list = list.filter((u) => u.pinned);
    if (q.trim())
      list = list.filter(
        (u) =>
          u.name.toLowerCase().includes(q.toLowerCase()) ||
          u.url.toLowerCase().includes(q.toLowerCase())
      );
    list.sort((a, b) => (a.pinned === b.pinned ? 0 : a.pinned ? -1 : 1));
    return list;
  }, [urls, q, filter]);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !url) return;
    await addUrl({
      name,
      url,
      status: "unknown",
      pinned: false,
      interval,
      lastResponse: undefined,
      history: [],
    });
    setName(""); setUrl(""); setInterval(60);
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
            <option value="pinned">Закреплённые</option>
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-[hsl(var(--card))] p-4 rounded shadow">
          <h4 className="font-medium mb-2">Добавить URL</h4>
          <form onSubmit={handleAdd} className="space-y-2">
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
            <button className="bg-[hsl(var(--primary))] text-[hsl(var(--primary-foreground))] px-4 py-2 rounded">Добавить</button>
          </form>
        </div>

        <div className="col-span-2 bg-[hsl(var(--card))] p-4 rounded shadow">
          <h4 className="font-medium mb-3">Сайты</h4>
          {loading ? (
            <div>Загрузка...</div>
          ) : (
            <div className="space-y-2">
              {visible.length === 0 && <div className="text-[hsl(var(--muted-foreground))]">Нет сайтов</div>}
              {visible.map((u) => (
                <div key={u.id} className="flex items-center justify-between p-3 border border-[hsl(var(--border))] rounded">
                  <div className="flex items-center gap-3">
                    <StatusDot status={u.status} />
                    <div>
                      <Link to={`/url/${u.id}`} className="font-medium">{u.name}</Link>
                      <div className="text-sm text-[hsl(var(--muted-foreground))]">{u.url}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 text-sm">
                    <div className="text-[hsl(var(--muted-foreground))]">{u.interval}s</div>
                    <button onClick={()=>updateUrl(u.id, { pinned: !u.pinned })} className="px-2 py-1 border border-[hsl(var(--border))] rounded">
                      {u.pinned ? "Открепить" : "Закрепить"}
                    </button>
                    <button onClick={()=>checkNow(u.id)} className="px-2 py-1 border border-[hsl(var(--border))] rounded">Проверить</button>
                    <button onClick={()=>removeUrl(u.id)} className="px-2 py-1 border border-[hsl(var(--border))] rounded text-[hsl(var(--destructive))]">Удалить</button>
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