export type UrlStatus = "up" | "down" | "unknown";

export interface UrlItem {
  id: string;
  name: string;
  url: string;
  status: UrlStatus;
  pinned: boolean;
  interval: number; 
  lastResponse?: string;
  history?: { ts: string; ok: boolean; code?: number }[];
}