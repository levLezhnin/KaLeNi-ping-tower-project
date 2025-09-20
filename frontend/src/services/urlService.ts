import type { UrlItem } from "../types/index";
import { monitorService, type MonitorDetailResponse } from "./monitorService";

// Store client-only metadata separately: pinned and check history by monitor id
const META_KEY = "monitorpro_meta_v1";

type UrlMeta = {
  pinnedIds: Record<string, boolean>;
  histories: Record<string, { ts: string; ok: boolean; code?: number }[]>;
};

function loadMeta(): UrlMeta {
  const raw = localStorage.getItem(META_KEY);
  if (!raw) return { pinnedIds: {}, histories: {} };
  try {
    const parsed = JSON.parse(raw) as UrlMeta;
    return {
      pinnedIds: parsed.pinnedIds || {},
      histories: parsed.histories || {},
    };
  } catch {
    return { pinnedIds: {}, histories: {} };
  }
}

function saveMeta(meta: UrlMeta) {
  localStorage.setItem(META_KEY, JSON.stringify(meta));
}

function mapStatus(status?: string): UrlItem["status"] {
  if (status === "UP") return "up";
  if (status === "DOWN") return "down";
  return "unknown";
}

function mapMonitorToUrlItem(m: MonitorDetailResponse, meta: UrlMeta): UrlItem {
  const id = String(m.id);
  return {
    id,
    name: m.name,
    url: m.url,
    status: mapStatus(m.currentStatus),
    pinned: Boolean(meta.pinnedIds[id]),
    interval: m.intervalSeconds,
    lastResponse: undefined,
    history: meta.histories[id] || [],
  };
}

export const urlService = {
  getAll: async (): Promise<UrlItem[]> => {
    const meta = loadMeta();
    const list = await monitorService.list();
    return list.map((m) => mapMonitorToUrlItem(m, meta));
  },

  getById: async (id: string): Promise<UrlItem | undefined> => {
    const meta = loadMeta();
    try {
      const m = await monitorService.get(Number(id));
      return mapMonitorToUrlItem(m, meta);
    } catch {
      return undefined;
    }
  },

  add: async (item: UrlItem): Promise<UrlItem> => {
    const payload = {
      name: item.name,
      description: undefined as string | undefined,
      url: item.url,
      intervalSeconds: item.interval,
      timeoutMs: undefined as number | undefined,
      groupId: undefined as number | undefined,
    };
    const created = await monitorService.create(payload);
    if (!created.id) throw new Error("Failed to create monitor");
    const meta = loadMeta();
    // persist pinned choice if set on create
    if (item.pinned) meta.pinnedIds[String(created.id)] = true;
    saveMeta(meta);
    const detail = await monitorService.get(created.id);
    return mapMonitorToUrlItem(detail, meta);
  },

  update: async (id: string, patch: Partial<UrlItem>): Promise<UrlItem | undefined> => {
    const meta = loadMeta();
    const numericId = Number(id);
    // Handle pinned locally without backend call
    if (typeof patch.pinned === "boolean") {
      meta.pinnedIds[id] = patch.pinned;
      saveMeta(meta);
    }
    const toSend: Partial<{ name: string; url: string; intervalSeconds: number; timeoutMs?: number; groupId?: number; }> = {};
    if (typeof patch.name === "string") toSend.name = patch.name;
    if (typeof patch.url === "string") toSend.url = patch.url;
    if (typeof patch.interval === "number") toSend.intervalSeconds = patch.interval;
    if (Object.keys(toSend).length > 0) {
      await monitorService.update(numericId, toSend);
    }
    const detail = await monitorService.get(numericId);
    return mapMonitorToUrlItem(detail, loadMeta());
  },

  remove: async (id: string): Promise<boolean> => {
    const numericId = Number(id);
    await monitorService.remove(numericId);
    const meta = loadMeta();
    delete meta.pinnedIds[id];
    delete meta.histories[id];
    saveMeta(meta);
    return true;
  },

  // There is no explicit "check now" endpoint; emulate by refetching and appending a synthetic record
  checkNow: async (id: string): Promise<UrlItem | undefined> => {
    const numericId = Number(id);
    const meta = loadMeta();
    try {
      const detail = await monitorService.get(numericId);
      const statusOk = detail.currentStatus === "UP";
      const record = { ts: new Date().toISOString(), ok: Boolean(statusOk) };
      const arr = [record, ...(meta.histories[id] || [])].slice(0, 200);
      meta.histories[id] = arr;
      saveMeta(meta);
      return mapMonitorToUrlItem(detail, meta);
    } catch {
      return undefined;
    }
  },
};