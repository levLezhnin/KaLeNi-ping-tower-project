<div align="center">

# Ping Service

**Высокопроизводительный микросервис мониторинга инфраструктуры**

<img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java" alt="Java 21">
<img src="https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=for-the-badge&logo=spring" alt="Spring Boot">
<img src="https://img.shields.io/badge/Redis-Cache-red?style=for-the-badge&logo=redis" alt="Redis">
<img src="https://img.shields.io/badge/ClickHouse-OLAP-blue?style=for-the-badge&logo=clickhouse" alt="ClickHouse">
<img src="https://img.shields.io/badge/WebFlux-Reactive-purple?style=for-the-badge&logo=spring" alt="WebFlux">

</div>

---

## Обзор

**Ping Service** — ключевой компонент экосистемы **MonitorPro**, реализующий enterprise-grade решение для непрерывного мониторинга доступности и производительности веб-сервисов. Сервис построен на принципах реактивного программирования и обеспечивает горизонтальное масштабирование с поддержкой тысяч одновременных проверок.

## Архитектурные решения

### Многослойная архитектура

<table>
<thead>
<tr>
<th>Слой</th>
<th>Компоненты</th>
<th>Архитектурные паттерны</th>
<th>Назначение</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>Scheduling Layer</strong></td>
<td>PingSchedulerService</td>
<td>Producer-Consumer, Event-Driven</td>
<td>Планирование и координация задач мониторинга</td>
</tr>
<tr>
<td><strong>Execution Layer</strong></td>
<td>EnhancedPingService</td>
<td>Circuit Breaker, Retry Pattern, Connection Pooling</td>
<td>Выполнение HTTP-проверок с отказоустойчивостью</td>
</tr>
<tr>
<td><strong>State Management</strong></td>
<td>RedisMonitorService</td>
<td>Redis Sorted Sets, Distributed Caching</td>
<td>Управление состоянием мониторов и очередью задач</td>
</tr>
<tr>
<td><strong>Persistence Layer</strong></td>
<td>ClickHouseService, PingHistoryService</td>
<td>Batch Processing, Write-Behind Pattern</td>
<td>Агрегация и долговременное хранение метрик</td>
</tr>
</tbody>
</table>

### Паттерны высокой производительности

**Event-Driven Architecture**
- Асинхронная обработка через `CompletableFuture` и `ExecutorService` 
- Децентрализованная архитектура с минимальным связыванием компонентов

**Connection Pool Management**
- Reactor Netty с настроенным `ConnectionProvider` (100 соединений, TTL 60с)
- Оптимизация TCP-соединений с `ChannelOption.CONNECT_TIMEOUT_MILLIS`

**Batch Processing Pattern**
- Агрегация результатов в `ConcurrentLinkedQueue` с настраиваемым размером батча 
- Периодическая запись в ClickHouse каждые 10 секунд для снижения нагрузки на БД

**Circuit Breaker Implementation**
- Retry механизм с экспоненциальным backoff через `reactor.util.retry.Retry`
- Intelligent exception filtering для различных типов сетевых ошибок

### Отказоустойчивость и надежность

**Distributed State Management**
- Redis Sorted Sets для временного планирования (`ping:queue`) 
- Processing lock mechanism через Redis Sets с TTL для предотвращения дублирования

**Graceful Degradation**
- Fallback стратегии при недоступности конфигурации мониторов
- Автоматическое переключение интервалов при ошибках (60с вместо штатного интервала)

**Resource Management**
- `@PreDestroy` hooks для корректного завершения executor service 
- Connection pooling с автоматической очисткой idle соединений

## Технологический стек

### Основные фреймворки
- **Spring Boot 3.5.6** с Spring WebFlux для реактивного программирования
- **Project Reactor** для асинхронных операций и backpressure handling  
- **Netty** в качестве неблокирующего HTTP-клиента с event-loop оптимизацией

### Инфраструктурные компоненты  
- **Redis** для distributed caching и state coordination
- **ClickHouse** как OLAP-решение для аналитики временных рядов
- **Connection pooling** через Reactor Netty с настраиваемыми параметрами lifecycle

### Мониторинг и наблюдаемость
- **SLF4J + Logback** с structured logging для корреляции событий
- **Atomic counters** для real-time метрик производительности 
- **Health check endpoints** для integration с orchestration платформами

## Ключевые возможности

### Интеллектуальное планирование
- **Dynamic scheduling** с поддержкой различных интервалов для каждого монитора
- **Queue-based processing** с приоритизацией overdue проверок
- **Load balancing** между worker threads с статистикой обработки

### Продвинутые HTTP-проверки
- Поддержка **GET/POST/HEAD** методов с custom headers и request body 
- **Redirect following** и **response compression** для оптимизации трафика  
- **Configurable timeouts** и **retry policies** с intelligent exception handling

### Масштабируемость данных
- **Write-behind caching** с batch агрегацией для ClickHouse 
- **Optimized INSERT** запросы с bulk operations для высокой пропускной способности 
- **Data partitioning** по времени в ClickHouse для эффективных range queries


## Интеграция с экосистемой MonitorPro

<div align="center">

```
URL Service → Redis Config → Ping Service → ClickHouse ← Statistics Service
↓             ↓             ↓              ↓              ↑
Monitor       State      Execution      Long-term        Analytics
Configuration  Management   & Results      Storage         & Reporting
```

</div>

### Взаимодействие с компонентами

**URL Service интеграция**
- Получение конфигураций мониторов через Redis с fallback механизмами 
- Dynamic configuration updates без перезапуска сервиса

**Statistics Service синхронизация** 
- Общая схема данных `ping_history.ping_results` для совместимости
- Consistent data format с поддержкой nullable полей для статистического анализа

**Redis как Service Bus**
- Centralized state management для distributed deployment
- Event coordination через pub/sub механизмы для real-time updates

## Структура проекта

```
ping-service/
├── service/
│   ├── PingSchedulerService.java      # Координатор задач с async processing  
│   ├── EnhancedPingService.java       # HTTP client с circuit breaker
│   ├── RedisMonitorService.java       # Distributed state management
│   ├── ClickHouseService.java         # OLAP persistence layer
│   └── PingHistoryService.java        # Batch aggregation service
├── dto/
│   ├── MonitorConfigDto.java          # Monitor configuration model
│   ├── PingResultDto.java             # Execution result model  
│   └── MonitorStatusDto.java          # Current state model
├── entity/
│   └── PingResult.java                # Domain entity for persistence
└── enums/
├── PingStatus.java                # Status enumeration (UP/DOWN/ERROR/TIMEOUT)
└── HttpMethod.java                # Supported HTTP methods
```

---

---
