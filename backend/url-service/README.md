<div align="center">

# URL Service

**Центральный микросервис управления мониторинговыми конфигурациями**

<img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java" alt="Java 21">
<img src="https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=for-the-badge&logo=spring" alt="Spring Boot">
<img src="https://img.shields.io/badge/PostgreSQL-Database-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL">
<img src="https://img.shields.io/badge/Spring%20Data%20JPA-ORM-green?style=for-the-badge&logo=spring" alt="Spring Data JPA">
<img src="https://img.shields.io/badge/OpenAPI-3.0-yellow?style=for-the-badge&logo=swagger" alt="OpenAPI">

</div>

---

## Обзор

**URL Service** — основной компонент конфигурационного слоя экосистемы **MonitorPro**, реализующий Domain-Driven Design подход для управления мониторинговыми ресурсами. Сервис обеспечивает CRUD операции над мониторами с поддержкой группировки, многопользовательской изоляции и интеграции с системой планирования проверок.

## Архитектурные решения

### Domain-Driven Design Architecture

<table>
<thead>
<tr>
<th>Слой</th>
<th>Компоненты</th>
<th>DDD Паттерны</th>
<th>Назначение</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>Presentation Layer</strong></td>
<td>MonitorController</td>
<td>Application Services, DTOs</td>
<td>REST API с OpenAPI документацией и валидацией</td>
</tr>
<tr>
<td><strong>Application Layer</strong></td>
<td>MonitorService</td>
<td>Use Cases, Command/Query Separation</td>
<td>Бизнес-логика и координация операций</td>
</tr>
<tr>
<td><strong>Domain Layer</strong></td>
<td>Monitor Entity, MonitorGroup</td>
<td>Entities, Value Objects, Aggregates</td>
<td>Основная бизнес-модель и инварианты</td>
</tr>
<tr>
<td><strong>Infrastructure Layer</strong></td>
<td>MonitorRepository, JPA</td>
<td>Repository Pattern, Data Mapper</td>
<td>Персистентность и внешние интеграции</td>
</tr>
</tbody>
</table>

### Enterprise Persistence Patterns

**Repository Pattern Implementation**
- Spring Data JPA с **custom query methods** для сложных бизнес-запросов 
- **Security-first queries** с обязательной фильтрацией по `ownerId` для multi-tenancy 
- **Batch operations** поддержка через `findByIdIn()` для массовых операций

**Domain Entity Design** 
- **Rich Domain Model** с бизнес-логикой инкапсулированной в entities
- **PostgreSQL JSONB** интеграция для flexible headers storage через `@JdbcTypeCode`
- **Auditing support** с автоматическими `@CreationTimestamp` и `@UpdateTimestamp`


## Технологический стек

### Основные фреймворки
- **Spring Boot 3.5.6** с Spring Web MVC для REST API
- **Spring Data JPA** для Repository Pattern и ORM абстракции
- **Hibernate 6.x** с PostgreSQL-specific оптимизациями

### Persistence & Data Access
- **PostgreSQL** с JSONB support для flexible configuration storage
- **Connection pooling** через HikariCP для production performance
- **Database migrations** support для schema evolution

### API & Documentation
- **OpenAPI 3.0** с comprehensive endpoint documentation
- **Bean Validation (JSR-380)** для request/response validation
- **Custom exception handling** для consistent error responses

### Enterprise Integration
- **Multi-format serialization** support через Jackson
- **Header-based authentication** с migration path к OAuth2/JWT

## Основные возможности

### Monitor Lifecycle Management
- **Full CRUD operations** с intelligent URL normalization и reuse
- **State management** через enable/disable endpoints для operational control
- **Group assignment** с hierarchical organization support 

### Advanced HTTP Configuration
- **Method support**: GET, POST, HEAD, UPDATE, PATCH, DELETE
- **Custom headers** через PostgreSQL JSONB с type-safe mapping
- **Request body** configuration для POST/PATCH operations 
- **Timeout & interval** granular настройки per-monitor basis

### Security & Isolation
- **Owner-based isolation** на уровне repository queries
- **Unique naming** constraints в рамках tenant scope
- **Resource counting** для quota enforcement и billing integration

## API Endpoints

Полная документация API доступна по адресу:
**[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Core Operations
- **POST** `/api/monitors/register` - Create monitor с intelligent URL reuse
- **GET** `/api/monitors/{id}` - Retrieve detailed monitor information  
- **GET** `/api/monitors` - List all monitors для authenticated user
- **PUT** `/api/monitors/{id}` - Update monitor configuration
- **DELETE** `/api/monitors/{id}` - Remove monitor with cleanup

### Operational Control
- **POST** `/api/monitors/{id}/enable` - Enable monitor for ping checks
- **POST** `/api/monitors/{id}/disable` - Disable monitor temporarily

## Интеграция с экосистемой MonitorPro

<div align="center">

```
Frontend → URL Service → Redis Config Cache → Ping Service
↓           ↓              ↓                   ↓
Monitor    Domain         State            Execution
CRUD      Model      Distribution        & Results
```

</div>

### Service Collaboration Patterns

**Configuration Distribution**
- Monitor configurations синхронизируются в Redis для Ping Service access
- **Event-driven updates** при изменении конфигураций мониторов
- **Cache invalidation** стратегии для consistency между сервисами

**Data Model Alignment**
- Shared enums (`HttpMethod`, `PingStatus`) для type consistency
- **Unified validation** rules across service boundaries
- **Consistent entity mapping** для seamless data flow

**Security Boundary**
- **Centralized authorization** через header-based owner validation 
- **Tenant isolation** enforcement на persistence layer level
- **Migration path** к production-ready authentication services

## Структура проекта

```
url-service/
├── controller/
│   └── MonitorController.java         # REST API с OpenAPI documentation
├── service/
│   └── MonitorService.java            # Application services & use cases
├── repository/
│   └── MonitorRepository.java         # Data access с security queries  
├── entity/
│   ├── Monitor.java                   # Core domain entity
│   ├── MonitorGroup.java              # Grouping aggregate
│   ├── HttpMethod.java                # HTTP method enumeration
│   └── PingStatus.java                # Status enumeration  
└── dto/
├── request/                       # Input DTOs с validation
└── response/                      # Output DTOs с documentation
```

---

<div align="center">

*Разработано для хакатона в Нижнем Новгороде от T1, кейс **Ping Tower**

</div>
