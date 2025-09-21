import { memo } from 'react';
import type { HourlyStatistics } from '../types';

interface StatisticsCardProps {
    statistics: HourlyStatistics[];
    title: string;
    loading?: boolean;
}

export const StatisticsCard = memo(({
    statistics,
    title,
    loading = false
}: StatisticsCardProps) => {
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

    if (!statistics || statistics.length === 0) {
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

    // Вычисляем общую статистику
    const totalStats = statistics.reduce((acc, stat) => ({
        totalPings: acc.totalPings + stat.totalPings,
        successfulPings: acc.successfulPings + stat.successfulPings,
        failedPings: acc.failedPings + stat.failedPings,
        totalResponseTime: acc.totalResponseTime + (stat.averageResponseTime * stat.totalPings),
        minResponseTime: Math.min(acc.minResponseTime, stat.minResponseTime),
        maxResponseTime: Math.max(acc.maxResponseTime, stat.maxResponseTime),
    }), {
        totalPings: 0,
        successfulPings: 0,
        failedPings: 0,
        totalResponseTime: 0,
        minResponseTime: Infinity,
        maxResponseTime: 0,
    });

    const overallUptime = totalStats.totalPings > 0
        ? (totalStats.successfulPings / totalStats.totalPings) * 100
        : 0;

    const averageResponseTime = totalStats.totalPings > 0
        ? totalStats.totalResponseTime / totalStats.totalPings
        : 0;

    return (
        <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-sm">
            <div className="p-6">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">{title}</h3>

                {/* Общая статистика */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                    <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                        <div className="text-sm font-medium text-gray-500 dark:text-gray-400">Общий аптайм</div>
                        <div className="text-2xl font-bold text-green-600 dark:text-green-400">
                            {overallUptime.toFixed(1)}%
                        </div>
                    </div>
                    <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                        <div className="text-sm font-medium text-gray-500 dark:text-gray-400">Всего пингов</div>
                        <div className="text-2xl font-bold text-gray-900 dark:text-white">
                            {totalStats.totalPings}
                        </div>
                    </div>
                    <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                        <div className="text-sm font-medium text-gray-500 dark:text-gray-400">Успешных</div>
                        <div className="text-2xl font-bold text-green-600 dark:text-green-400">
                            {totalStats.successfulPings}
                        </div>
                    </div>
                    <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                        <div className="text-sm font-medium text-gray-500 dark:text-gray-400">Неудачных</div>
                        <div className="text-2xl font-bold text-red-600 dark:text-red-400">
                            {totalStats.failedPings}
                        </div>
                    </div>
                </div>

                {/* Статистика времени ответа */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                    <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                        <div className="text-sm font-medium text-gray-500 dark:text-gray-400">Среднее время ответа</div>
                        <div className="text-xl font-bold text-blue-600 dark:text-blue-400">
                            {averageResponseTime.toFixed(1)} мс
                        </div>
                    </div>
                    <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                        <div className="text-sm font-medium text-gray-500 dark:text-gray-400">Минимальное время</div>
                        <div className="text-xl font-bold text-green-600 dark:text-green-400">
                            {totalStats.minResponseTime === Infinity ? 'N/A' : `${totalStats.minResponseTime} мс`}
                        </div>
                    </div>
                    <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                        <div className="text-sm font-medium text-gray-500 dark:text-gray-400">Максимальное время</div>
                        <div className="text-xl font-bold text-orange-600 dark:text-orange-400">
                            {totalStats.maxResponseTime} мс
                        </div>
                    </div>
                </div>

                {/* Детальная таблица по часам */}
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                        <thead className="bg-gray-50 dark:bg-gray-700">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                    Время
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                    Аптайм
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                    Пинги
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                    Время ответа
                                </th>
                            </tr>
                        </thead>
                        <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                            {statistics.slice(-10).reverse().map((stat, index) => (
                                <tr key={index}>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                                        {new Date(stat.hour).toLocaleString()}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                                        <span className={`font-medium ${stat.uptimePercentage >= 99 ? 'text-green-600 dark:text-green-400' :
                                            stat.uptimePercentage >= 95 ? 'text-yellow-600 dark:text-yellow-400' :
                                                'text-red-600 dark:text-red-400'
                                            }`}>
                                            {stat.uptimePercentage.toFixed(1)}%
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                                        <div className="flex items-center space-x-2">
                                            <span className="text-green-600 dark:text-green-400">{stat.successfulPings}</span>
                                            <span className="text-gray-400">/</span>
                                            <span className="text-red-600 dark:text-red-400">{stat.failedPings}</span>
                                            <span className="text-gray-400">/</span>
                                            <span className="text-gray-600 dark:text-gray-400">{stat.totalPings}</span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                                        {stat.averageResponseTime.toFixed(1)} мс
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
});
