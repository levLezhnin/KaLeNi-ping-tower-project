import { useEffect, useMemo, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { useMonitorsStore } from "../../store/useMonitorsStore";
import type { MonitorDetailResponse } from "../../services/monitorService";
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer } from "recharts";

export default function UrlDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { getById, refreshOne, enable, disable, remove } = useMonitorsStore();
  const [item, setItem] = useState<MonitorDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      if (!id) return;
      setLoading(true);
      await refreshOne(Number(id));
      setItem(getById(Number(id)) ?? null);
      setLoading(false);
    })();
  }, [id, getById, refreshOne]);

  const chartData = useMemo(() => {
    if (!item) return [];
    return item.lastCheckedAt ? [{
      time: new Date(item.lastCheckedAt).toLocaleString(),
      ok: item.currentStatus === "UP" ? 1 : 0,
    }] : [];
  }, [item]);

  const handleToggle = async () => {
    if (!id || !item) return;
    if (item.enabled) await disable(item.id); else await enable(item.id);
    await refreshOne(item.id);
    setItem(getById(item.id) ?? null);
  };

  if (loading) return <div className="max-w-4xl mx-auto p-6 text-[hsl(var(--foreground))]">Загрузка...</div>;
  if (!item) return <div className="max-w-4xl mx-auto p-6 text-[hsl(var(--foreground))]">Не найдено. <button className="text-blue-600" onClick={() => navigate("/dashboard")}>Вернуться</button></div>;

  return (
    <div className="max-w-4xl mx-auto p-6 text-[hsl(var(--foreground))]">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2 className="text-2xl font-semibold">{item.name}</h2>
          <div className="text-sm text-[hsl(var(--muted-foreground))]">{item.url}</div>
        </div>
        <div className="flex items-center gap-3 text-sm">
          <div>Статус: <span className={item.currentStatus === "UP" ? "text-green-600" : "text-red-600"}>{item.currentStatus}</span></div>
          <button onClick={handleToggle} className="px-3 py-2 border border-[hsl(var(--border))] rounded">{item.enabled ? "Отключить" : "Включить"}</button>
          <button onClick={async () => { await remove(item.id); navigate("/dashboard"); }} className="px-3 py-2 border border-[hsl(var(--border))] rounded text-[hsl(var(--destructive))]">Удалить</button>
          <Link to="/dashboard" className="px-3 py-2 border border-[hsl(var(--border))] rounded">Назад</Link>
        </div>
      </div>

      <div className="bg-[hsl(var(--card))] p-4 rounded shadow mb-6">
        <h4 className="font-medium mb-2">История проверок</h4>
        {chartData.length === 0 ? (
          <div className="text-[hsl(var(--muted-foreground))]">Нет данных</div>
        ) : (
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke={`hsl(var(--border))`} />
              <XAxis dataKey="time" tick={{ fill: `hsl(var(--muted-foreground))`, fontSize: 12 }} />
              <YAxis tick={{ fill: `hsl(var(--muted-foreground))`, fontSize: 12 }} />
              <Tooltip />
              <Line type="monotone" dataKey="ok" stroke={`hsl(var(--chart-1))`} strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}