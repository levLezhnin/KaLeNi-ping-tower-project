import type { UrlItem } from "../types/index";

const STORAGE_KEY = "monitorpro_urls_v1";

const seed: UrlItem[] = [
  {
    id: "1",
    name: "Google",
    url: "https://google.com",
    status: "up",
    pinned: true,
    interval: 60,
    lastResponse: "200 OK",
    history: [
      { ts: new Date().toISOString(), ok: true, code: 200 },
    ],
  },
  {
    id: "2",
    name: "Example",
    url: "https://example.com",
    status: "down",
    pinned: false,
    interval: 120,
    lastResponse: "500",
    history: [
      { ts: new Date().toISOString(), ok: false, code: 500 },
    ],
  },
];

function load(): UrlItem[] {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(seed));
    return seed;
  }
  try {
    return JSON.parse(raw) as UrlItem[];
  } catch {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(seed));
    return seed;
  }
}

function save(urls: UrlItem[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(urls));
}

export const urlService = {
  getAll: async (): Promise<UrlItem[]> => {
    await new Promise((r) => setTimeout(r, 200));
    return load();
  },

  getById: async (id: string): Promise<UrlItem | undefined> => {
    await new Promise((r) => setTimeout(r, 150));
    return load().find((u) => u.id === id);
  },

  add: async (item: UrlItem): Promise<UrlItem> => {
    const urls = load();
    urls.push(item);
    save(urls);
    return item;
  },

  update: async (id: string, patch: Partial<UrlItem>): Promise<UrlItem | undefined> => {
    const urls = load();
    const idx = urls.findIndex((u) => u.id === id);
    if (idx === -1) return undefined;
    urls[idx] = { ...urls[idx], ...patch };
    save(urls);
    return urls[idx];
  },

  remove: async (id: string): Promise<boolean> => {
    let urls = load();
    urls = urls.filter((u) => u.id !== id);
    save(urls);
    return true;
  },

  // simulate a check (random result)
  checkNow: async (id: string): Promise<UrlItem | undefined> => {
    const urls = load();
    const idx = urls.findIndex((u) => u.id === id);
    if (idx === -1) return undefined;
    // random ping
    const ok = Math.random() > 0.25; // 75% up
    const ts = new Date().toISOString();
    const code = ok ? 200 : 500;
    const record = { ts, ok, code };
    const u = urls[idx];
    u.status = ok ? "up" : "down";
    u.lastResponse = `${code}${ok ? " OK" : ""}`;
    u.history = [record, ...(u.history || [])].slice(0, 200);
    save(urls);
    return u;
  },
};