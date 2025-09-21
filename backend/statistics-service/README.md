
<div align="center">

# Statistics Service

**Микросервис аналитики и статистики мониторинга**

<img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java" alt="Java 21">
<img src="https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=for-the-badge&logo=spring" alt="Spring Boot">
<img src="https://img.shields.io/badge/ClickHouse-JDBC-red?style=for-the-badge&logo=clickhouse" alt="ClickHouse">
<img src="https://img.shields.io/badge/Gradle-Build-blue?style=for-the-badge&logo=gradle" alt="Gradle">

</div>

---

## Описание

**Statistics Service** — это высокопроизводительный микросервис для получения, агрегации и анализа статистики мониторинга серверов в рамках экосистемы *MonitorPro*. Сервис обеспечивает REST API для получения детальной аналитики работы мониторов с поддержкой временных диапазонов и визуализации данных.

## Архитектура

<table>
<thead>
<tr>
<th>Слой</th>
<th>Компоненты</th>
<th>Ответственность</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>Controller Layer</strong></td>
<td>StatisticsController</td>
<td>REST API endpoints, валидация запросов, OpenAPI документация</td>
</tr>
<tr>
<td><strong>Service Layer</strong></td>
<td>StatisticsService</td>
<td>Бизнес-логика агрегации, расчет KPI, обработка временных рядов</td>
</tr>
<tr>
<td><strong>Data Access Layer</strong></td>
<td>ClickHouseStatisticsService</td>
<td>Интеграция с ClickHouse, оптимизированные запросы, обработка NULL</td>
</tr>
</tbody>
</table>

## Технологический стек

### Spring Framework Stack
- **Spring Boot 3.5.6** - основной фреймворк
- **Spring Web** - REST API
- **Spring Validation** - валидация данных

### База данных
- **ClickHouse JDBC 0.6.3** - OLAP база временных рядов
- **TabSeparated** формат для производительности

### API и документация
- **OpenAPI 3 / Swagger** - интерактивная документация
- **Jackson** - JSON сериализация
- **JSR-310** - работа с датами/временем

### 🔧 Инструменты разработки
- **Lombok** - генерация boilerplate кода
- **SLF4J + Logback** - система логирования
- **Gradle** - система сборки

## API Endpoints

Доступен красивый Swagger: http://localhost:8084/swagger-ui/index.html

### Почасовая статистика

```
GET /api/v1/statistics/monitors/{monitorId}/hourly/24h
GET /api/v1/statistics/monitors/{monitorId}/hourly?startTime={start}&endTime={end}
```

<details>
<summary><strong>Response Model: HourlyStatsDto</strong></summary>

- **totalPings** - общее количество пингов
- **uptimePercentage** - процент времени работы (ключевая SLA метрика)
- **averageResponseTime** - среднее время отклика
- **minResponseTime** / **maxResponseTime** - мин/макс время отклика
- **failedPings** - количество неудачных проверок

</details>

### Данные для графиков

```
GET /api/v1/statistics/monitors/{monitorId}/chart/24h  
GET /api/v1/statistics/monitors/{monitorId}/chart?startTime={start}&endTime={end}
```

<details>
<summary><strong>Response Model: ChartDataPointDto</strong></summary>

- **pingTimestamp** - временные метки пингов
- **status** - статусы (UP/DOWN/ERROR)
- **responseTimeMs** - время отклика в миллисекундах
- **responseCode** - HTTP статус коды

</details>

## Интеграция с экосистемой

<div align="center">

```
   ping-service  →    ClickHouse  →    statistics-service
↓                    ↓                ↓
Генерация данных   Хранение врем.    Аналитика и
мониторинга         рядов           отчеты
```

</div>

### Схема взаимодействия

- **Асинхронное взаимодействие** через общую ClickHouse базу
- **Независимое масштабирование** сервисов
- **Единый формат данных** `ping_history.ping_results`

| Поле | Тип | Описание |
|------|-----|----------|
| `monitor_id` | UInt32 | Идентификатор монитора |
| `ping_timestamp` | DateTime64 | Время выполнения пинга |
| `status` | String | Статус проверки (UP/DOWN/ERROR) |
| `response_time_ms` | Nullable(UInt32) | Время отклика |
| `response_code` | Nullable(UInt16) | HTTP код ответа |
| `error_message` | Nullable(String) | Сообщение об ошибке |

## Производительность

> **Ключевые оптимизации:**
>
> - **Stream API** для эффективной обработки больших датасетов
> - **ClickHouse партиционирование** по времени для быстрых запросов
> - **Группировка по ChronoUnit.HOURS** для агрегации

## Отказоустойчивость

- **Exception handling** с детальным логированием
- **Graceful degradation** при отсутствии данных
- **Robust validation** входящих параметров

## Структура проекта

```
statistics-service/
├── src/main/java/
│   ├── controller/
│   │   └── StatisticsController.java     # REST API endpoints
│   ├── service/
│   │   ├── StatisticsService.java        # Бизнес-логика
│   │   └── ClickHouseStatisticsService.java # Data Access Layer
│   ├── dto/
│   │   ├── HourlyStatsDto.java          # Почасовая статистика
│   │   ├── ChartDataPointDto.java       # Данные графиков
│   │   └── PingResultDto.java           # Результат пинга
│   └── config/
│       └── ClickhouseConfig.java        # Конфигурация ClickHouse
├── build.gradle                         # Зависимости и сборка
└── README.md                           # Документация
```

