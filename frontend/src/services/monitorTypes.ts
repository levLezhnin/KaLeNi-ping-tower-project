export type PingStatus = "UP" | "DOWN" | "UNKNOWN" | "PAUSED";

export interface MonitorDetailResponse {
    id: number;
    name: string;
    description?: string;
    url: string;
    method: string;
    headers?: Record<string, string>;
    requestBody?: any;
    contentType?: string;
    intervalSeconds: number;
    timeoutMs?: number;
    enabled?: boolean;
    currentStatus?: PingStatus;
    lastCheckedAt?: string;
    lastResponseTimeMs?: number;
    lastResponseCode?: number;
    lastErrorMessage?: string;
    groupId?: number;
    groupName?: string;
    createdAt?: string;
    updatedAt?: string;
    nextPingAt?: string;
    targetId?: number;
}

export interface CreateMonitorRequest {
    name: string;
    description?: string;
    url: string;
    method: string;
    headers?: Record<string, string>;
    requestBody?: any;
    contentType?: string;
    intervalSeconds: number;
    timeoutMs?: number;
    groupId?: number;
}

export interface MonitorCreateResponse {
    result: boolean;
    id?: number | null;
    name?: string | null;
    url?: string | null;
    method?: string | null;
    intervalSeconds?: number | null;
    groupId?: number | null;
    enabled?: boolean | null;
    newlyCreatedTarget?: boolean;
    targetId?: number;
    errorMessage?: string;
}