import { api } from './apiClient';
import type { HourlyStatistics, ChartDataPoint, StatisticsParams } from '../types';

export class StatisticsService {
    /**
     * Получение почасовой статистики мониторинга за указанный период
     */
    static async getHourlyStatistics(
        monitorId: number,
        params: StatisticsParams
    ): Promise<HourlyStatistics[]> {
        try {
            console.log('Making statistics request with params:', { monitorId, params });

            // Простой запрос с параметрами в query string
            const response = await api.get(`/api/v1/statistics/monitors/${monitorId}/hourly`, {
                params: {
                    startTime: params.startTime,
                    endTime: params.endTime,
                }
            });
            return response.data;
        } catch (error: any) {
            console.error('Statistics API Error:', error);
            console.error('Error response:', error.response?.data);
            console.error('Error status:', error.response?.status);

            // Возвращаем пустой массив вместо ошибки, чтобы UI не ломался
            return [];
        }
    }

    /**
     * Получение почасовой статистики мониторинга за последние 24 часа
     */
    static async getHourlyStatistics24h(monitorId: number): Promise<HourlyStatistics[]> {
        try {
            console.log('Making 24h statistics request for monitor:', monitorId);
            const response = await api.get(`/api/v1/statistics/monitors/${monitorId}/hourly/24h`);
            return response.data;
        } catch (error: any) {
            console.error('24h Statistics API Error:', error);
            console.error('Error response:', error.response?.data);
            console.error('Error status:', error.response?.status);
            return [];
        }
    }

    /**
     * Получение данных для построения графика за указанный период
     */
    static async getChartData(
        monitorId: number,
        params: StatisticsParams
    ): Promise<ChartDataPoint[]> {
        try {
            console.log('Making chart data request with params:', { monitorId, params });

            const response = await api.get(`/api/v1/statistics/monitors/${monitorId}/chart`, {
                params: {
                    startTime: params.startTime,
                    endTime: params.endTime,
                }
            });
            return response.data;
        } catch (error: any) {
            console.error('Chart Data API Error:', error);
            console.error('Error response:', error.response?.data);
            console.error('Error status:', error.response?.status);

            // Возвращаем пустой массив вместо ошибки, чтобы UI не ломался
            return [];
        }
    }

    /**
     * Получение данных для построения графика за последние 24 часа
     */
    static async getChartData24h(monitorId: number): Promise<ChartDataPoint[]> {
        try {
            console.log('Making 24h chart data request for monitor:', monitorId);
            const response = await api.get(`/api/v1/statistics/monitors/${monitorId}/chart/24h`);
            return response.data;
        } catch (error: any) {
            console.error('24h Chart Data API Error:', error);
            console.error('Error response:', error.response?.data);
            console.error('Error status:', error.response?.status);
            return [];
        }
    }

    /**
     * Форматирование даты для API (ISO 8601)
     */
    static formatDateForAPI(date: Date): string {
        // Формат как в документации: 2025-09-20T07:00:00
        // Убираем миллисекунды и Z
        return date.toISOString().replace(/\.\d{3}Z$/, '');
    }

    /**
     * Альтернативный формат даты для API
     */
    static formatDateForAPIAlternative(date: Date): string {
        // Формат: 2025-09-20T07:00:00.000Z
        return date.toISOString();
    }

    /**
     * Получение даты 24 часа назад
     */
    static get24HoursAgo(): Date {
        const now = new Date();
        return new Date(now.getTime() - 24 * 60 * 60 * 1000);
    }

    /**
     * Получение даты 7 дней назад
     */
    static get7DaysAgo(): Date {
        const now = new Date();
        return new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    }

    /**
     * Получение даты 30 дней назад
     */
    static get30DaysAgo(): Date {
        const now = new Date();
        return new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    }
}
