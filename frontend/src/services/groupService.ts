import api from "./apiClient";
import type { MonitorDetailResponse } from "./monitorService";

export interface GroupResponse {
    id: number;
    name: string;
    description?: string;
    ownerId: number;
    monitorCount: number;
    createdAt: string;
    updatedAt: string;
}

export interface GroupWithListResponse {
    group: GroupResponse;
    monitors: MonitorDetailResponse[];
}

export interface CreateGroupRequest { name: string; description?: string }
export interface UpdateGroupRequest { name: string; description?: string }

export const groupService = {
    async list(): Promise<GroupResponse[]> {
        const { data } = await api.get<GroupResponse[]>(`/api/groups`);
        return data;
    },
    async get(id: number): Promise<GroupWithListResponse> {
        const { data } = await api.get<GroupWithListResponse>(`/api/groups/${id}`);
        return data;
    },
    async create(payload: CreateGroupRequest): Promise<GroupResponse> {
        const { data } = await api.post<GroupResponse>(`/api/groups/register`, payload);
        return data;
    },
    async update(id: number, payload: UpdateGroupRequest): Promise<GroupResponse> {
        const { data } = await api.put<GroupResponse>(`/api/groups/${id}`, payload);
        return data;
    },
    async remove(id: number): Promise<void> {
        await api.delete(`/api/groups/${id}`);
    },
};

export default groupService;


