import api from "./apiClient";

export type PingStatus = "UP" | "DOWN" | "UNKNOWN" | "PAUSED";

export interface MonitorDetailResponse {
    id: number;
    name: string;
    description?: string;
    url: string;
    targetId: number;
    intervalSeconds: number;
    timeoutMs?: number;
    enabled?: boolean;
    currentStatus?: PingStatus;
    lastCheckedAt?: string;
    groupId?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface CreateMonitorRequest {
    name: string;
    description?: string;
    url: string;
    intervalSeconds: number;
    timeoutMs?: number;
    groupId?: number;
}

export interface MonitorCreateResponse {
    result: boolean;
    id?: number;
    url?: string;
    newlyCreatedTarget?: boolean;
    targetId?: number;
    groupId?: number;
    enabled?: boolean;
}

export const monitorService = {
    async list(): Promise<MonitorDetailResponse[]> {
        const { data } = await api.get<MonitorDetailResponse[]>(`/api/monitors`);
        return data;
    },

    async get(id: number): Promise<MonitorDetailResponse> {
        const { data } = await api.get<MonitorDetailResponse>(`/api/monitors/${id}`);
        return data;
    },

    async create(payload: CreateMonitorRequest): Promise<MonitorCreateResponse> {
        const { data } = await api.post<MonitorCreateResponse>(`/api/monitors/register`, payload);
        return data;
    },

    async update(id: number, payload: Partial<CreateMonitorRequest>): Promise<MonitorDetailResponse> {
        const { data } = await api.put<MonitorDetailResponse>(`/api/monitors/${id}`, payload);
        return data;
    },

    async remove(id: number): Promise<void> {
        await api.delete(`/api/monitors/${id}`);
    },

    async enable(id: number): Promise<void> {
        await api.post(`/api/monitors/${id}/enable`);
    },

    async disable(id: number): Promise<void> {
        await api.post(`/api/monitors/${id}/disable`);
    },
};

export default monitorService;


