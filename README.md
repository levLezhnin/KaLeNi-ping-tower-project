# Инструкция по запуску и управлению системой
## Структура проекта

```
root/
├── docker-compose.yml          # Главный файл оркестрации
├── .env                       # Переменные окружения для всей системы
├── backend/
│   ├── url-service/
│   │   └── Dockerfile         # Образ для URL сервиса
│   ├── user-service/
│   │   └── Dockerfile         # Образ для User сервиса
│   ├── ping-service/
│   │   └── Dockerfile         # Образ для Ping сервиса
│   ├── statistics-service/
│   │   └── Dockerfile         # Образ для Statistics сервиса
│   └── notification-service/
│       └── Dockerfile         # Образ для Notification сервиса
└── frontend/
    ├── Dockerfile             # Образ для Frontend
    └── vite.config.ts         # Конфигурация Vite с проксированием
```

## Первоначальный запуск системы

### 1. Подготовка файлов
Убедитесь, что созданы файлы:
- `root/docker-compose.yml` (главный файл оркестрации)
- `root/.env` (переменные окружения)
- Обновлен `frontend/vite.config.ts` с правильным именем контейнера

### 2. Запуск всей системы
```bash
# Перейти в корневую директорию проекта
cd root/

# Запуск всех сервисов в фоновом режиме
docker-compose up -d

# Запуск с отображением логов в реальном времени
docker-compose up

# Запуск с принудительной пересборкой всех образов
docker-compose up --build -d
```

### 3. Проверка статуса
```bash
# Показать статус всех контейнеров
docker-compose ps

# Показать логи всех сервисов
docker-compose logs

# Показать логи конкретного сервиса
docker-compose logs url_service
docker-compose logs user_service
docker-compose logs frontend
```

## Доступ к сервисам

После запуска сервисы доступны по адресам:
- **Frontend**: http://localhost:5173
- **URL Service API**: http://localhost:8080
- **User Service API**: http://localhost:8081
- **Notification Service API**: http://localhost:8083
- **Statistics Service API**: http://localhost:8084

## Работа с отдельными контейнерами

### Пересборка конкретного сервиса
```bash
# Пересобрать только URL Service
docker-compose build url_service
docker-compose up -d url_service

# Пересобрать только User Service  
docker-compose build user_service
docker-compose up -d user_service

# Пересобрать только Frontend
docker-compose build frontend
docker-compose up -d frontend
```

### Перезапуск конкретного сервиса
```bash
# Перезапустить URL Service (без пересборки)
docker-compose restart url_service

# Остановить и запустить заново с зависимостями
docker-compose stop url_service
docker-compose up -d url_service
```

### Обновление после изменений в коде
```bash
# Если изменили код URL Service
docker-compose build url_service
docker-compose up -d url_service

# Если изменили код User Service
docker-compose build user_service  
docker-compose up -d user_service

# Если изменили код Frontend
docker-compose build frontend
docker-compose up -d frontend
```

## Управление базами данных

### Доступ к базам данных для отладки
```bash
# Подключиться к базе URL Service
docker exec -it url_service_db psql -U url_user -d url_service_db

# Подключиться к базе User Service
docker exec -it user_service_db psql -U user_admin -d user_service_db

# Показать логи базы данных
docker-compose logs url_service_db
docker-compose logs user_service_db
```

### Очистка данных базы (ОСТОРОЖНО!)
```bash
# Остановить систему и удалить все данные БД
docker-compose down -v

# Удалить конкретный volume
docker volume rm url_postgres_data
docker volume rm user_postgres_data
```

## Разработка и отладка

### Подключение к работающему контейнеру
```bash
# Зайти в контейнер с URL Service
docker exec -it url_service sh

# Зайти в контейнер с Frontend
docker exec -it frontend sh

# Запустить bash (если доступен)
docker exec -it url_service bash
```

### Проверка конфигурации
```bash
# Проверить синтаксис docker-compose.yml
docker-compose config

# Показать итоговую конфигурацию с подставленными переменными
docker-compose config --services
```

### Изменение переменных окружения
1. Отредактировать файл `root/.env`
2. Перезапустить сервисы:
```bash
docker-compose down
docker-compose up -d
```

## Troubleshooting

### Проблемы с сетью
```bash
# Проверить сетевые соединения между контейнерами
docker exec -it frontend ping url_service
docker exec -it url_service ping url_service_db
```

### Проблемы с портами
```bash
# Проверить, какие порты заняты
netstat -tlnp | grep :8080
netstat -tlnp | grep :5173

# Освободить порт (изменить в .env файле)
```

### Проблемы с образами
```bash
# Принудительная пересборка без кеша
docker-compose build --no-cache url_service

# Удалить образ и пересобрать
docker rmi root_url_service
docker-compose build url_service
```

## Рабочий процесс разработки

1. **Изменили код** → `docker-compose build <service_name>` → `docker-compose up -d <service_name>`
2. **Изменили .env** → `docker-compose down` → `docker-compose up -d`
3. **Изменили docker-compose.yml** → `docker-compose down` → `docker-compose up -d`
4. **Проблемы с БД** → Проверить логи → При необходимости пересоздать volume

Система готова к разработке! 
Все сервисы взаимодействуют через внутреннюю Docker сеть `ping_app_network`.
Обмен сообщений по Kafka происходит через сеть `kafka-network`
