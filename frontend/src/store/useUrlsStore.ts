import { create } from "zustand";
import type { UrlItem } from "../types/index";
import { urlService } from "../services/urlService";
import { v4 as uuidv4 } from "uuid";

type State = {
    urls: UrlItem[];
    loading: boolean;
    fetchAll: () => Promise<void>;
    addUrl: (payload: Omit<UrlItem, "id">) => Promise<void>;
    updateUrl: (id: string, patch: Partial<UrlItem>) => Promise<void>;
    removeUrl: (id: string) => Promise<void>;
    checkNow: (id: string) => Promise<void>;
};

export const useUrlsStore = create<State>((set, get) => ({
    urls: [],
    loading: false,
    fetchAll: async () => {
        set({ loading: true });
        const urls = await urlService.getAll();
        set({ urls, loading: false });
    },
    addUrl: async (payload) => {
        const item: UrlItem = { ...payload, id: uuidv4() };
        await urlService.add(item);
        await get().fetchAll();
    },
    updateUrl: async (id, patch) => {
        await urlService.update(id, patch);
        await get().fetchAll();
    },
    removeUrl: async (id) => {
        await urlService.remove(id);
        await get().fetchAll();
    },
    checkNow: async (id) => {
        await urlService.checkNow(id);
        await get().fetchAll();
    },
}));