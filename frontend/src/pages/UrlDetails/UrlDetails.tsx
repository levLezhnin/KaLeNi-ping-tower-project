import { useEffect, useState, useCallback, useRef } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { useMonitorsStore } from "../../store/useMonitorsStore";
import type { MonitorDetailResponse } from "../../services/monitorTypes";
import { StatisticsService } from "../../services/statisticsService";
import { StatisticsCard } from "../../components/StatisticsCard";
import { StatisticsChart } from "../../components/StatisticsChart";
import { TimeRangeSelector, type TimeRange } from "../../components/TimeRangeSelector";
import type { HourlyStatistics, ChartDataPoint } from "../../types";

export default function UrlDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { getById, refreshOne, enable, disable, remove } = useMonitorsStore();
  const [item, setItem] = useState<MonitorDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);

  // Состояние для статистики
  const [hourlyStatistics, setHourlyStatistics] = useState<HourlyStatistics[]>([]);
  const [statisticsChartData, setStatisticsChartData] = useState<ChartDataPoint[]>([]);
  const [statisticsLoading, setStatisticsLoading] = useState(false);
  const [selectedTimeRange, setSelectedTimeRange] = useState<TimeRange>('24h');
  const [customStartDate, setCustomStartDate] = useState<string>('');
  const [customEndDate, setCustomEndDate] = useState<string>('');

  // Ref для хранения предыдущих данных, чтобы избежать лишних обновлений
  const prevDataRef = useRef<{
    hourlyStats: HourlyStatistics[];
    chartStats: ChartDataPoint[];
  }>({ hourlyStats: [], chartStats: [] });

  // Функция для загрузки статистики
  const loadStatistics = useCallback(async (monitorId: number, timeRange: TimeRange) => {
    if (!monitorId) return;

    setStatisticsLoading(true);
    try {
      let hourlyStats: HourlyStatistics[] = [];
      let chartStats: ChartDataPoint[] = [];

      switch (timeRange) {
        case '24h':
          hourlyStats = await StatisticsService.getHourlyStatistics24h(monitorId);
          chartStats = await StatisticsService.getChartData24h(monitorId);
          break;
        case '7d': {
          const sevenDaysAgo = StatisticsService.get7DaysAgo();
          const now = new Date();
          hourlyStats = await StatisticsService.getHourlyStatistics(monitorId, {
            startTime: StatisticsService.formatDateForAPI(sevenDaysAgo),
            endTime: StatisticsService.formatDateForAPI(now),
          });
          chartStats = await StatisticsService.getChartData(monitorId, {
            startTime: StatisticsService.formatDateForAPI(sevenDaysAgo),
            endTime: StatisticsService.formatDateForAPI(now),
          });
          break;
        }
        case '30d': {
          const thirtyDaysAgo = StatisticsService.get30DaysAgo();
          const now30 = new Date();
          hourlyStats = await StatisticsService.getHourlyStatistics(monitorId, {
            startTime: StatisticsService.formatDateForAPI(thirtyDaysAgo),
            endTime: StatisticsService.formatDateForAPI(now30),
          });
          chartStats = await StatisticsService.getChartData(monitorId, {
            startTime: StatisticsService.formatDateForAPI(thirtyDaysAgo),
            endTime: StatisticsService.formatDateForAPI(now30),
          });
          break;
        }
        case 'custom':
          // Для произвольных дат используем отдельную функцию
          break;
      }

      setHourlyStatistics(hourlyStats);
      setStatisticsChartData(chartStats);

      // Обновляем ref с новыми данными
      prevDataRef.current = { hourlyStats, chartStats };
    } catch (error) {
      console.error('Ошибка загрузки статистики:', error);
      setHourlyStatistics([]);
      setStatisticsChartData([]);
    } finally {
      setStatisticsLoading(false);
    }
  }, [customStartDate, customEndDate]);

  // Функция для тихого обновления статистики (без индикатора загрузки)
  const loadStatisticsSilently = useCallback(async (monitorId: number, timeRange: TimeRange) => {
    if (!monitorId) return;

    try {
      let hourlyStats: HourlyStatistics[] = [];
      let chartStats: ChartDataPoint[] = [];

      switch (timeRange) {
        case '24h':
          hourlyStats = await StatisticsService.getHourlyStatistics24h(monitorId);
          chartStats = await StatisticsService.getChartData24h(monitorId);
          break;
        case '7d': {
          const sevenDaysAgo = StatisticsService.get7DaysAgo();
          const now = new Date();
          hourlyStats = await StatisticsService.getHourlyStatistics(monitorId, {
            startTime: StatisticsService.formatDateForAPI(sevenDaysAgo),
            endTime: StatisticsService.formatDateForAPI(now),
          });
          chartStats = await StatisticsService.getChartData(monitorId, {
            startTime: StatisticsService.formatDateForAPI(sevenDaysAgo),
            endTime: StatisticsService.formatDateForAPI(now),
          });
          break;
        }
        case '30d': {
          const thirtyDaysAgo = StatisticsService.get30DaysAgo();
          const now30 = new Date();
          hourlyStats = await StatisticsService.getHourlyStatistics(monitorId, {
            startTime: StatisticsService.formatDateForAPI(thirtyDaysAgo),
            endTime: StatisticsService.formatDateForAPI(now30),
          });
          chartStats = await StatisticsService.getChartData(monitorId, {
            startTime: StatisticsService.formatDateForAPI(thirtyDaysAgo),
            endTime: StatisticsService.formatDateForAPI(now30),
          });
          break;
        }
        case 'custom':
          // Для произвольных дат используем отдельную функцию
          break;
      }

      // Обновляем состояние только если данные изменились
      const prevData = prevDataRef.current;
      const hourlyStatsChanged = JSON.stringify(prevData.hourlyStats) !== JSON.stringify(hourlyStats);
      const chartStatsChanged = JSON.stringify(prevData.chartStats) !== JSON.stringify(chartStats);

      if (hourlyStatsChanged) {
        setHourlyStatistics(hourlyStats);
        prevDataRef.current.hourlyStats = hourlyStats;
      }

      if (chartStatsChanged) {
        setStatisticsChartData(chartStats);
        prevDataRef.current.chartStats = chartStats;
      }
    } catch (error) {
      console.error('Ошибка тихого обновления статистики:', error);
    }
  }, []);

  // Обработчики для изменения временного диапазона
  const handleTimeRangeChange = (range: TimeRange) => {
    setSelectedTimeRange(range);
    if (id && range !== 'custom') {
      loadStatistics(Number(id), range);
    }
  };

  const handleCustomDateChange = (startDate: string, endDate: string) => {
    setCustomStartDate(startDate);
    setCustomEndDate(endDate);
    if (id && startDate && endDate) {
      // Вызываем loadStatistics с новыми датами напрямую
      loadStatisticsWithCustomDates(Number(id), startDate, endDate);
    }
  };

  // Отдельная функция для загрузки статистики с произвольными датами
  const loadStatisticsWithCustomDates = useCallback(async (monitorId: number, startDate: string, endDate: string) => {
    if (!monitorId || !startDate || !endDate) return;

    setStatisticsLoading(true);
    try {
      console.log('Loading custom statistics with dates:', { startDate, endDate });

      const hourlyStats = await StatisticsService.getHourlyStatistics(monitorId, {
        startTime: StatisticsService.formatDateForAPI(new Date(startDate)),
        endTime: StatisticsService.formatDateForAPI(new Date(endDate)),
      });
      const chartStats = await StatisticsService.getChartData(monitorId, {
        startTime: StatisticsService.formatDateForAPI(new Date(startDate)),
        endTime: StatisticsService.formatDateForAPI(new Date(endDate)),
      });

      setHourlyStatistics(hourlyStats);
      setStatisticsChartData(chartStats);
    } catch (error) {
      console.error('Ошибка загрузки статистики:', error);
      setHourlyStatistics([]);
      setStatisticsChartData([]);
    } finally {
      setStatisticsLoading(false);
    }
  }, []);

  useEffect(() => {
    const performRefresh = async () => {
      if (!id) return;
      setLoading(true);
      await refreshOne(Number(id));
      setItem(getById(Number(id)) ?? null);
      setLoading(false);
    };

    const performSilentRefresh = async () => {
      if (!id) return;
      // Обновляем данные без изменения состояния загрузки
      await refreshOne(Number(id));
      const newItem = getById(Number(id)) ?? null;

      // Обновляем состояние только если данные действительно изменились
      setItem(prevItem => {
        if (!prevItem || !newItem) return newItem;

        // Проверяем ключевые поля, которые могут измениться
        if (
          prevItem.currentStatus !== newItem.currentStatus ||
          prevItem.lastResponseTimeMs !== newItem.lastResponseTimeMs ||
          prevItem.lastResponseCode !== newItem.lastResponseCode ||
          prevItem.lastCheckedAt !== newItem.lastCheckedAt ||
          prevItem.lastErrorMessage !== newItem.lastErrorMessage
        ) {
          return newItem;
        }

        return prevItem; // Данные не изменились, не обновляем состояние
      });

      // Также обновляем статистику тихо, если она не в режиме загрузки
      if (!statisticsLoading && selectedTimeRange !== 'custom') {
        loadStatisticsSilently(Number(id), selectedTimeRange);
      }
    };

    performRefresh();

    // Устанавливаем интервал для автоматического обновления каждые 30 секунд
    const interval = setInterval(() => {
      performSilentRefresh();
    }, 30000); // 30 секунд

    // Очищаем интервал при размонтировании компонента
    return () => clearInterval(interval);
  }, [id, getById, refreshOne, statisticsLoading, selectedTimeRange, loadStatisticsSilently]);

  // Загружаем статистику при изменении ID или временного диапазона
  useEffect(() => {
    if (id && selectedTimeRange !== 'custom') {
      loadStatistics(Number(id), selectedTimeRange);
    }
  }, [id, selectedTimeRange, loadStatistics]);


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
          <div className="flex items-center gap-2">
            <span className="text-gray-500 dark:text-gray-400">Статус:</span>
            <div className="flex items-center gap-2">
              <div className={`w-2 h-2 rounded-full ${item.currentStatus === "UP" ? "bg-green-400" :
                item.currentStatus === "DOWN" ? "bg-red-500" :
                  item.currentStatus === "PAUSED" ? "bg-yellow-400" : "bg-gray-400"
                }`}></div>
              <span className={`font-medium ${item.currentStatus === "UP" ? "text-green-600 dark:text-green-400" :
                item.currentStatus === "DOWN" ? "text-red-600 dark:text-red-400" :
                  item.currentStatus === "PAUSED" ? "text-yellow-600 dark:text-yellow-400" : "text-gray-600 dark:text-gray-400"
                }`}>
                {item.currentStatus || "UNKNOWN"}
              </span>
            </div>
          </div>
          <button
            onClick={handleToggle}
            className={`px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${item.enabled
              ? "bg-yellow-100 text-yellow-800 hover:bg-yellow-200 dark:bg-yellow-900 dark:text-yellow-300 dark:hover:bg-yellow-800"
              : "bg-green-100 text-green-800 hover:bg-green-200 dark:bg-green-900 dark:text-green-300 dark:hover:bg-green-800"
              }`}
          >
            {item.enabled ? "Отключить" : "Включить"}
          </button>
          <button
            onClick={async () => { await remove(item.id); navigate("/dashboard"); }}
            className="px-3 py-2 rounded-lg text-sm font-medium bg-red-100 text-red-800 hover:bg-red-200 dark:bg-red-900 dark:text-red-300 dark:hover:bg-red-800 transition-all duration-200"
          >
            Удалить
          </button>
          <Link
            to="/dashboard"
            className="px-3 py-2 rounded-lg text-sm font-medium bg-gray-100 text-gray-800 hover:bg-gray-200 dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600 transition-all duration-200"
          >
            Назад
          </Link>
        </div>
      </div>

      {/* Основная информация */}
      <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-sm mb-6">
        <div className="p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Основная информация</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-3">
              <div>
                <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Название</span>
                <div className="text-gray-900 dark:text-white">{item.name}</div>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-500 dark:text-gray-400">URL</span>
                <div className="text-blue-600 dark:text-blue-400 break-all">{item.url}</div>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Описание</span>
                <div className="text-gray-900 dark:text-white">{item.description || <span className="text-gray-500">Нет описания</span>}</div>
              </div>
            </div>
            <div className="space-y-3">
              <div>
                <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Метод</span>
                <div className="inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300">
                  {item.method}
                </div>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Интервал проверки</span>
                <div className="text-gray-900 dark:text-white">{item.intervalSeconds} секунд</div>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Таймаут</span>
                <div className="text-gray-900 dark:text-white">{item.timeoutMs} мс</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Детали запроса */}
      <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-sm mb-6">
        <div className="p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Детали запроса</h3>

          {/* Content-Type */}
          {item.contentType && (
            <div className="mb-4">
              <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Content-Type</span>
              <div className="mt-1 inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300">
                {item.contentType}
              </div>
            </div>
          )}

          {/* Заголовки */}
          {item.headers && Object.keys(item.headers).length > 0 && (
            <div className="mb-4">
              <span className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-2 block">Заголовки</span>
              <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                <div className="space-y-2">
                  {Object.entries(item.headers).map(([key, value]) => (
                    <div key={key} className="flex flex-col sm:flex-row sm:items-center gap-1 sm:gap-4">
                      <span className="font-mono text-xs font-medium text-gray-600 dark:text-gray-400 min-w-0 flex-shrink-0">
                        {key}:
                      </span>
                      <span className="font-mono text-xs text-gray-900 dark:text-white break-all">
                        {value}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Тело запроса */}
          {item.requestBody && item.method !== "GET" && (
            <div className="mb-4">
              <span className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-2 block">Тело запроса</span>
              <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                <pre className="text-xs text-gray-900 dark:text-white whitespace-pre-wrap break-words overflow-x-auto">
                  {typeof item.requestBody === "object"
                    ? JSON.stringify(item.requestBody, null, 2)
                    : String(item.requestBody)
                  }
                </pre>
              </div>
            </div>
          )}

          {/* Если нет дополнительных деталей */}
          {!item.contentType && (!item.headers || Object.keys(item.headers).length === 0) && (!item.requestBody || item.method === "GET") && (
            <div className="text-gray-500 dark:text-gray-400 text-sm">
              Дополнительные параметры запроса не настроены
            </div>
          )}
        </div>
      </div>

      {/* Статус и последняя проверка */}
      <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-sm mb-6">
        <div className="p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Текущий статус</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Статус</span>
              <div className="flex items-center gap-2 mt-1">
                <div className={`w-3 h-3 rounded-full ${item.currentStatus === "UP" ? "bg-green-400" :
                  item.currentStatus === "DOWN" ? "bg-red-500" :
                    item.currentStatus === "PAUSED" ? "bg-yellow-400" : "bg-gray-400"
                  }`}></div>
                <span className={`font-medium ${item.currentStatus === "UP" ? "text-green-600 dark:text-green-400" :
                  item.currentStatus === "DOWN" ? "text-red-600 dark:text-red-400" :
                    item.currentStatus === "PAUSED" ? "text-yellow-600 dark:text-yellow-400" : "text-gray-600 dark:text-gray-400"
                  }`}>
                  {item.currentStatus || "UNKNOWN"}
                </span>
              </div>
            </div>
            <div>
              <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Время ответа</span>
              <div className="text-gray-900 dark:text-white">
                {typeof item.lastResponseTimeMs === "number" ? `${item.lastResponseTimeMs} мс` : "Нет данных"}
              </div>
            </div>
            <div>
              <span className="text-sm font-medium text-gray-500 dark:text-gray-400">HTTP код</span>
              <div className="text-gray-900 dark:text-white">
                {typeof item.lastResponseCode === "number" ? item.lastResponseCode : "Нет данных"}
              </div>
            </div>
          </div>
          {item.lastCheckedAt && (
            <div className="mt-4">
              <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Последняя проверка</span>
              <div className="text-gray-900 dark:text-white">{new Date(item.lastCheckedAt).toLocaleString()}</div>
            </div>
          )}
          {item.lastErrorMessage && (
            <div className="mt-4">
              <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Последняя ошибка</span>
              <div className="mt-1 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded text-sm text-red-700 dark:text-red-400">
                {item.lastErrorMessage}
              </div>
            </div>
          )}
        </div>
      </div>


      {/* Статистика */}
      <div className="mb-6">
        <TimeRangeSelector
          selectedRange={selectedTimeRange}
          onRangeChange={handleTimeRangeChange}
          customStartDate={customStartDate}
          customEndDate={customEndDate}
          onCustomDateChange={handleCustomDateChange}
        />
      </div>

      {/* Почасовая статистика */}
      <div className="mb-6">
        <StatisticsCard
          statistics={hourlyStatistics}
          title="Почасовая статистика"
          loading={statisticsLoading}
        />
      </div>

      {/* График времени ответа */}
      <div className="mb-6">
        <StatisticsChart
          data={statisticsChartData}
          title="График времени ответа"
          loading={statisticsLoading}
          type="line"
        />
      </div>

    </div>
  );
}