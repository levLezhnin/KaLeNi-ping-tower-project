-- clickhouse/init/01-init-ping-history.sql

CREATE DATABASE IF NOT EXISTS ping_history;

USE ping_history;

-- 🔥 Основная таблица для пинг результатов
CREATE TABLE IF NOT EXISTS ping_results (
    monitor_id UInt32,
    ping_timestamp DateTime64(3, 'UTC'),
    status String,
    response_time_ms Nullable(UInt32),
    response_code Nullable(UInt16),
    error_message Nullable(String),
    url String,
    created_at DateTime64(3, 'UTC') DEFAULT now64(3, 'UTC')
) ENGINE = MergeTree()
ORDER BY (monitor_id, ping_timestamp)
PARTITION BY toYYYYMM(ping_timestamp)
TTL toDateTime(ping_timestamp) + INTERVAL 1 YEAR;


CREATE TABLE IF NOT EXISTS ping_stats_hourly (
    monitor_id UInt32,
    hour_timestamp DateTime,
    successful_pings UInt32,
    failed_pings UInt32,
    total_pings UInt32,
    avg_response_time Nullable(Float64),
    min_response_time Nullable(UInt32),
    max_response_time Nullable(UInt32)
) ENGINE = SummingMergeTree()
ORDER BY (monitor_id, hour_timestamp)
PARTITION BY toYYYYMM(hour_timestamp);

-- 🔥 Материализованное представление для статистики
CREATE MATERIALIZED VIEW IF NOT EXISTS ping_stats_hourly_mv
TO ping_stats_hourly
AS SELECT
    monitor_id,
    toStartOfHour(ping_timestamp) as hour_timestamp,
    countIf(status = 'UP') as successful_pings,
    countIf(status = 'DOWN') as failed_pings,
    count() as total_pings,
    avgIf(response_time_ms, status = 'UP' AND response_time_ms IS NOT NULL) as avg_response_time,
    minIf(response_time_ms, status = 'UP' AND response_time_ms IS NOT NULL) as min_response_time,
    maxIf(response_time_ms, status = 'UP' AND response_time_ms IS NOT NULL) as max_response_time
FROM ping_results
GROUP BY monitor_id, hour_timestamp;
