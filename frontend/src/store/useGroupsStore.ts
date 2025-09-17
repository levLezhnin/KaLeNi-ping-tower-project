import { create } from "zustand";
import groupService, { type GroupResponse, type GroupWithListResponse, type CreateGroupRequest, type UpdateGroupRequest } from "../services/groupService";
import monitorService from "../services/monitorService";

type State = {
    groups: GroupResponse[];
    loading: boolean;
    error?: string | null;
    fetchAll: () => Promise<void>;
    create: (payload: CreateGroupRequest) => Promise<GroupResponse | undefined>;
    update: (id: number, payload: UpdateGroupRequest) => Promise<GroupResponse | undefined>;
    remove: (id: number) => Promise<void>;
    getDetails: (id: number) => Promise<GroupWithListResponse | undefined>;
    setIntervalForGroup: (id: number, intervalSeconds: number) => Promise<void>;
};

export const useGroupsStore = create<State>((set, get) => ({
    groups: [],
    loading: false,
    error: null,

    fetchAll: async () => {
        set({ loading: true, error: null });
        try {
            const data = await groupService.list();
            set({ groups: data, loading: false });
        } catch (e: any) {
            set({ loading: false, error: e?.message || "Не удалось загрузить группы" });
        }
    },

    create: async (payload) => {
        try {
            set({ error: null });
            const g = await groupService.create(payload);
            await get().fetchAll();
            return g;
        } catch (e: any) {
            set({ error: e?.message || "Не удалось создать группу" });
            return undefined;
        }
    },

    update: async (id, payload) => {
        try {
            set({ error: null });
            const g = await groupService.update(id, payload);
            await get().fetchAll();
            return g;
        } catch (e: any) {
            set({ error: e?.message || "Не удалось обновить группу" });
            return undefined;
        }
    },

    remove: async (id) => {
        try {
            set({ error: null });
            await groupService.remove(id);
            await get().fetchAll();
        } catch (e: any) {
            set({ error: e?.message || "Не удалось удалить группу" });
        }
    },

    getDetails: async (id) => {
        try {
            set({ error: null });
            return await groupService.get(id);
        } catch (e: any) {
            set({ error: e?.message || "Не удалось загрузить группу" });
            return undefined;
        }
    },

    // Backend группового интервала нет; реализуем изменением intervalSeconds у всех мониторов группы
    setIntervalForGroup: async (id, intervalSeconds) => {
        try {
            set({ error: null });
            const details = await groupService.get(id);
            if (!details) return;
            const fixed = Math.max(30, intervalSeconds);
            await Promise.all(details.monitors.map(m => monitorService.update(m.id, { intervalSeconds: fixed })));
        } catch (e: any) {
            set({ error: e?.message || "Не удалось обновить интервал группы" });
        }
    },
}));

export default useGroupsStore;


