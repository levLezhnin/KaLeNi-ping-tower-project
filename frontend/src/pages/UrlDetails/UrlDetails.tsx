import { useEffect, useMemo, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { urlService } from "../../services/urlService";
import type { UrlItem } from "../../types";
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer } from "recharts";

export default function UrlDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [item, setItem] = useState<UrlItem | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      if (!id) return;
      setLoading(true);
      const u = await urlService.getById(id);
      setItem(u ?? null);
      setLoading(false);
    })();
  }, [id]);

  const chartData = useMemo(() => {
    if (!item) return [];
    return (item.history || []).map(h => ({
      time: new Date(h.ts).toLocaleString(),
      ok: h.ok ? 1 : 0,
    })).reverse();
  }, [item]);

  const handleCheck = async () => {
    if (!id) return;
    await urlService.checkNow(id);
    const u = await urlService.getById(id);
    setItem(u ?? null);
  };

  if (loading) return <div className="max-w-4xl mx-auto p-6 text-[hsl(var(--foreground))]">Загрузка...</div>;
  if (!item) return <div className="max-w-4xl mx-auto p-6 text-[hsl(var(--foreground))]">Не найдено. <button className="text-blue-600" onClick={()=>navigate("/dashboard")}>Вернуться</button></div>;

  return (
    <div className="max-w-4xl mx-auto p-6 text-[hsl(var(--foreground))]">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2 className="text-2xl font-semibold">{item.name}</h2>
          <div className="text-sm text-[hsl(var(--muted-foreground))]">{item.url}</div>
        </div>
        <div className="flex items-center gap-3 text-sm">
          <div>Статус: <span className={item.status === "up" ? "text-green-600" : "text-red-600"}>{item.status}</span></div>
          <button onClick={handleCheck} className="px-3 py-2 border border-[hsl(var(--border))] rounded">Проверить сейчас</button>
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