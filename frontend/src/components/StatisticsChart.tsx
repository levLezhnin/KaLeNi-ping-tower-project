import { useMemo, memo } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer, ScatterChart, Scatter } from 'recharts';
import type { ChartDataPoint } from '../types';

interface StatisticsChartProps {
    data: ChartDataPoint[];
    title: string;
    loading?: boolean;
    type?: 'line' | 'scatter';
}

export const StatisticsChart = memo(({
    data,
    title,
    loading = false,
    type = 'line'
}: StatisticsChartProps) => {
    const chartData = useMemo(() => {
        if (!data || data.length === 0) return [];

        return data.map(point => ({
            timestamp: new Date(point.pingTimestamp).getTime(),
            time: new Date(point.pingTimestamp).toLocaleString(),
            status: point.status,
            responseTime: point.responseTimeMs,
            responseCode: point.responseCode,
            isUp: point.status === 'UP' ? 1 : 0,
        })).sort((a, b) => a.timestamp - b.timestamp);
    }, [data]);

    if (loading) {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-sm">
                <div className="p-6">
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">{title}</h3>
                    <div className="flex items-center justify-center py-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                        <span className="ml-2 text-gray-600 dark:text-gray-400">Загрузка...</span>
                    </div>
                </div>
            </div>
        );
    }

    if (!data || data.length === 0) {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-sm">
                <div className="p-6">
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">{title}</h3>
                    <div className="text-center py-8">
                        <div className="text-gray-500 dark:text-gray-400 mb-2">Нет данных для отображения</div>
                        <div className="text-sm text-gray-400 dark:text-gray-500">Данные появятся после первых проверок</div>
                    </div>
                </div>
            </div>
        );
    }

    const CustomTooltip = ({ active, payload }: { active?: boolean; payload?: any[] }) => {
        if (active && payload && payload.length) {
            const data = payload[0].payload;
            return (
                <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-3 shadow-lg">
                    <p className="text-sm font-medium text-gray-900 dark:text-white">
                        {data.time}
                    </p>
                    <p className={`text-sm ${data.status === 'UP' ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'
                        }`}>
                        Статус: {data.status}
                    </p>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                        Время ответа: {data.responseTime} мс
                    </p>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                        HTTP код: {data.responseCode}
                    </p>
                </div>
            );
        }
        return null;
    };

    if (type === 'scatter') {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-sm">
                <div className="p-6">
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">{title}</h3>
                    <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                            <ScatterChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                                <XAxis
                                    dataKey="timestamp"
                                    type="number"
                                    scale="time"
                                    domain={['dataMin', 'dataMax']}
                                    tick={{ fill: "#6b7280", fontSize: 12 }}
                                    axisLine={{ stroke: "#e5e7eb" }}
                                    tickFormatter={(value) => new Date(value).toLocaleTimeString()}
                                />
                                <YAxis
                                    dataKey="responseTime"
                                    tick={{ fill: "#6b7280", fontSize: 12 }}
                                    axisLine={{ stroke: "#e5e7eb" }}
                                    label={{ value: 'Время ответа (мс)', angle: -90, position: 'insideLeft' }}
                                />
                                <Tooltip content={<CustomTooltip />} />
                                <Scatter
                                    dataKey="responseTime"
                                    fill="#3b82f6"
                                />
                            </ScatterChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-sm">
            <div className="p-6">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">{title}</h3>
                <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                        <LineChart data={chartData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                            <XAxis
                                dataKey="timestamp"
                                type="number"
                                scale="time"
                                domain={['dataMin', 'dataMax']}
                                tick={{ fill: "#6b7280", fontSize: 12 }}
                                axisLine={{ stroke: "#e5e7eb" }}
                                tickFormatter={(value) => new Date(value).toLocaleTimeString()}
                            />
                            <YAxis
                                dataKey="responseTime"
                                tick={{ fill: "#6b7280", fontSize: 12 }}
                                axisLine={{ stroke: "#e5e7eb" }}
                                label={{ value: 'Время ответа (мс)', angle: -90, position: 'insideLeft', style: { textAnchor: 'middle' } }}
                            />
                            <Tooltip content={<CustomTooltip />} />
                            <Line
                                type="monotone"
                                dataKey="responseTime"
                                stroke="#3b82f6"
                                strokeWidth={2}
                                dot={(props: { cx?: number; cy?: number; payload?: any }) => {
                                    const { payload } = props;
                                    return (
                                        <circle
                                            cx={props.cx}
                                            cy={props.cy}
                                            r={4}
                                            fill={payload.status === 'UP' ? '#10b981' : '#ef4444'}
                                            stroke={payload.status === 'UP' ? '#10b981' : '#ef4444'}
                                            strokeWidth={2}
                                        />
                                    );
                                }}
                                activeDot={{ r: 6, stroke: '#3b82f6', strokeWidth: 2 }}
                            />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
});
