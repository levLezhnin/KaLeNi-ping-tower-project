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