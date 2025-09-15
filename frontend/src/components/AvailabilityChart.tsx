import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer } from "recharts";

export default function AvailabilityChart({ data }: { data: { time: string; ok: number }[] }) {
  return (
    <div style={{ width: "100%", height: 240 }}>
      <ResponsiveContainer>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="time" minTickGap={30} />
          <YAxis domain={[0,1]} tickFormatter={(v)=> v === 1 ? "OK" : "FAIL"} />
          <Tooltip />
          <Line type="monotone" dataKey="ok" stroke="#60a5fa" strokeWidth={2} dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}