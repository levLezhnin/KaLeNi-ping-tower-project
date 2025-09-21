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

// Статистика API типы
export interface HourlyStatistics {
  monitorId: number;
  hour: string;
  totalPings: number;
  successfulPings: number;
  failedPings: number;
  uptimePercentage: number;
  averageResponseTime: number;
  minResponseTime: number;
  maxResponseTime: number;
}

export interface ChartDataPoint {
  pingTimestamp: string;
  status: "UP" | "DOWN";
  responseTimeMs: number;
  responseCode: number;
}

export interface StatisticsParams {
  startTime: string;
  endTime: string;
}