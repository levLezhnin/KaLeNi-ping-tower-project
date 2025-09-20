import { create } from "zustand";
import { type MonitorDetailResponse, type CreateMonitorRequest } from "../services/monitorTypes";
import { monitorService } from "../services/monitorService";

type State = {
    monitors: MonitorDetailResponse[];
    loading: boolean;
    error?: string | null;
    fetchAll: () => Promise<void>;
    getById: (id: number) => MonitorDetailResponse | undefined;
    refreshOne: (id: number) => Promise<void>;
    create: (payload: CreateMonitorRequest) => Promise<MonitorDetailResponse | undefined>;
    update: (id: number, patch: Partial<CreateMonitorRequest>) => Promise<MonitorDetailResponse | undefined>;
    remove: (id: number) => Promise<void>;
    enable: (id: number) => Promise<void>;
    disable: (id: number) => Promise<void>;
};

export const useMonitorsStore = create<State>((set, get) => ({
    monitors: [],
    loading: false,
    error: null,

    fetchAll: async () => {
        set({ loading: true, error: null });
        try {
            const data = await monitorService.list();
            const sanitized = Array.isArray(data)
                ? data.filter((m) =>
                    m && typeof m.id === "number" && ((m.name && m.name.trim().length > 0) || (m.url && m.url.trim().length > 0))
                )
                : [];
            set({ monitors: sanitized, loading: false });
        } catch (e: any) {
            set({ loading: false, error: e?.message || "Не удалось загрузить мониторы" });
        }
    },

    getById: (id) => get().monitors.find(m => m.id === id),

    refreshOne: async (id) => {
        try {
            const detail = await monitorService.get(id);
            const list = get().monitors;
            const idx = list.findIndex(m => m.id === id);
            if (idx === -1) {
                set({ monitors: [detail, ...list] });
            } else {
                const next = [...list];
                next[idx] = detail;
                set({ monitors: next });
            }
        } catch (e) {
            // ignore
        }
    },

    create: async (payload) => {
        try {
            set({ error: null });
            const res = await monitorService.create(payload);

            // Проверяем, что результат успешный и есть ID
            if (res.result && res.id != null) {
                await get().refreshOne(res.id);
                return get().getById(res.id);
            }

            // Если result = false, но нет ошибки в catch, создаем общую ошибку
            if (!res.result) {
                throw new Error(res.errorMessage || "Не удалось создать монитор");
            }

            return undefined;
        } catch (e: any) {
            // Передаем ошибку дальше, чтобы модалка могла её обработать
            throw e;
        }
    },

    update: async (id, patch) => {
        try {
            set({ error: null });
            const detail = await monitorService.update(id, patch);
            const list = get().monitors;
            const idx = list.findIndex(m => m.id === id);
            if (idx === -1) {
                set({ monitors: [detail, ...list] });
            } else {
                const next = [...list];
                next[idx] = detail;
                set({ monitors: next });
            }
            return detail;
        } catch (e: any) {
            set({ error: e?.message || "Не удалось обновить монитор" });
            return undefined;
        }
    },

    remove: async (id) => {
        try {
            set({ error: null });
            await monitorService.remove(id);
            set({ monitors: get().monitors.filter(m => m.id !== id) });
        } catch (e: any) {
            const msg = e?.message || "Не удалось удалить монитор";
            set({ error: msg });
            throw e; // Передаем ошибку дальше для обработки в компонентах
        }
    },

    enable: async (id) => {
        try {
            set({ error: null });
            await monitorService.enable(id);
            await get().refreshOne(id);
        } catch (e: any) {
            const msg = e?.message || "Не удалось включить монитор";
            set({ error: msg });
            throw e; // Передаем ошибку дальше для обработки в компонентах
        }
    },

    disable: async (id) => {
        try {
            set({ error: null });
            await monitorService.disable(id);
            await get().refreshOne(id);
        } catch (e: any) {
            const msg = e?.message || "Не удалось отключить монитор";
            set({ error: msg });
            throw e; // Передаем ошибку дальше для обработки в компонентах
        }
    },
}));

export default useMonitorsStore;
