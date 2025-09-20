import api from "./apiClient";
import type { CreateMonitorRequest, MonitorCreateResponse, MonitorDetailResponse } from "./monitorTypes";

export const monitorService = {
    async list(): Promise<MonitorDetailResponse[]> {
        const { data } = await api.get<MonitorDetailResponse[]>(`/api/monitors`);
        return data;
    },

    async get(id: number): Promise<MonitorDetailResponse> {
        const { data } = await api.get<MonitorDetailResponse>(`/api/monitors/${id}`);
        console.log(data);
        return data;
    },

    async create(payload: CreateMonitorRequest): Promise<MonitorCreateResponse> {
        try {
            const { data } = await api.post<MonitorCreateResponse>('/api/monitors/register', payload);

            // Проверяем, если result = false, то это ошибка
            if (!data.result && data.errorMessage) {
                throw new Error(data.errorMessage);
            }

            return data;
        } catch (e: any) {
            // Отладочная информация
            console.log("Error in monitorService.create:", e);
            console.log("Response data:", e?.response?.data);

            // Если это уже наша ошибка с errorMessage, просто перебрасываем
            if (e.message && e.message !== "Create failed" && e.message !== "Request failed with status code 400") {
                throw e;
            }

            // Проверяем, есть ли errorMessage в response.data
            if (e?.response?.data?.errorMessage) {
                throw new Error(e.response.data.errorMessage);
            }

            // Иначе обрабатываем как HTTP ошибку
            const msg = e?.response?.data?.message || e?.response?.data?.error || e?.message || "Create failed";
            throw new Error(msg);
        }
    },

    async update(id: number, payload: Partial<CreateMonitorRequest>): Promise<MonitorDetailResponse> {
        try {
            const { data } = await api.put<MonitorDetailResponse>(`/api/monitors/${id}`, payload);
            return data;
        } catch (e: any) {
            const msg = e?.response?.data?.message || e?.response?.data?.error || e?.message || "Update failed";
            throw new Error(msg);
        }
    },

    async remove(id: number): Promise<void> {
        try {
            await api.delete(`/api/monitors/${id}`);
        } catch (e: any) {
            const msg = e?.response?.data?.message || e?.response?.data?.error || e?.message || "Delete failed";
            throw new Error(msg);
        }
    },

    async enable(id: number): Promise<void> {
        try {
            await api.post(`/api/monitors/${id}/enable`);
        } catch (e: any) {
            const msg = e?.response?.data?.message || e?.response?.data?.error || e?.message || "Enable failed";
            throw new Error(msg);
        }
    },

    async disable(id: number): Promise<void> {
        try {
            await api.post(`/api/monitors/${id}/disable`);
        } catch (e: any) {
            const msg = e?.response?.data?.message || e?.response?.data?.error || e?.message || "Disable failed";
            throw new Error(msg);
        }
    },
};

export default monitorService;


