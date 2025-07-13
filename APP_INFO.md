# CinemaAbyss - Инструкция по запуску и тестированию

## Описание проекта

CinemaAbyss - это платформа для управления информацией о фильмах, пользователях, платежах и подписках. Проект находится в процессе миграции от монолитной архитектуры к микросервисной с использованием паттерна Strangler Fig.

## Архитектура

### Текущие компоненты:
- **Monolith** (Go) - оригинальное приложение с доменами Users, Payments, Subscriptions
- **Movies Service** (Go) - извлеченный микросервис для управления фильмами
- **Events Service** (Java Spring Boot) - сервис для обработки событий через Kafka
- **Proxy Service** (Go) - API Gateway для постепенной миграции трафика
- **PostgreSQL** - база данных
- **Apache Kafka** - брокер сообщений для событийной архитектуры

## Быстрый старт

### Предварительные требования
- Docker и Docker Compose
- Make (опционально, для удобства)
- Git

### Запуск приложения

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd architecture-pro-cinemaabyss-s2
```

2. Создайте файл с переменными окружения:
```bash
cp .env.example .env
```

3. Запустите все сервисы:
```bash
make start
```

4. Проверьте статус сервисов:
```bash
docker-compose ps
# или
make ps
```

5. Проверьте health endpoints:
```bash
make health
# или вручную:
curl http://localhost:8000/health  # Proxy
curl http://localhost:8080/health  # Monolith
curl http://localhost:8081/api/movies/health  # Movies Service
curl http://localhost:8082/api/events/health  # Events Service
```

## Тестирование

### Запуск всех тестов
```bash
make test
```

### Тестирование отдельных компонентов

#### Proxy Service (Strangler Fig Pattern)
```bash
# Проверка маршрутизации (несколько запросов)
make test-proxy

# Изменение процента миграции
make set-migration PERCENT=75
docker-compose restart proxy-service

# Проверка текущего процента
make show-migration
```

#### Events Service (Kafka)
```bash
# Создание событий через API
make test-events

# Просмотр логов обработки событий
make events-logs
# или
docker logs cinemaabyss-events-service --tail 50 | grep "CONSUMED"

# Kafka UI доступен по адресу
http://localhost:8090
```

### Ручное тестирование API

#### Через Proxy (порт 8000)
```bash
# Получить список фильмов
curl http://localhost:8000/api/movies

# Получить список пользователей
curl http://localhost:8000/api/users

# Создать событие
curl -X POST http://localhost:8000/api/events/movie \
  -H "Content-Type: application/json" \
  -d '{"movieId": 1, "title": "The Matrix", "action": "VIEWED", "userId": 123}'
```

#### Напрямую к сервисам
```bash
# Monolith
curl http://localhost:8080/api/users

# Movies Service
curl http://localhost:8081/api/movies

# Events Service
curl http://localhost:8082/api/events/health
```

## Управление через Makefile

Доступные команды:
```bash
make help          # Показать все команды
make build         # Собрать образы
make up            # Запустить сервисы
make down          # Остановить сервисы
make restart       # Перезапустить сервисы
make logs          # Показать логи всех сервисов
make test          # Запустить тесты
make clean         # Остановить и удалить volumes
make ps            # Показать статус контейнеров
make health        # Проверить health endpoints
make proxy-logs    # Логи proxy сервиса
make events-logs   # Логи events сервиса
make kafka-ui      # Открыть Kafka UI
make test-proxy    # Тестировать маршрутизацию
make test-events   # Тестировать создание событий
```

## Конфигурация

### Переменные окружения (.env)
- `MOVIES_MIGRATION_PERCENT` - процент трафика на новый Movies сервис (0-100)
- `GRADUAL_MIGRATION` - включить/выключить постепенную миграцию (true/false)
- Остальные переменные см. в `.env.example`

### Порты
- 8000 - Proxy Service (API Gateway)
- 8080 - Monolith
- 8081 - Movies Service
- 8082 - Events Service
- 5432 - PostgreSQL
- 9092 - Kafka
- 8090 - Kafka UI

## Особенности реализации

### Proxy Service
- Реализует паттерн Strangler Fig для постепенной миграции
- Маршрутизация на основе вероятности (MOVIES_MIGRATION_PERCENT)
- Логирование всех решений о маршрутизации
- Health check endpoint для мониторинга

### Events Service
- Producer и Consumer в одном сервисе (MVP)
- Поддержка трех типов событий: User, Payment, Movie
- Автоматическое создание топиков при старте
- Детальное логирование обработанных событий

## Решение проблем

### Сервисы не запускаются
```bash
# Проверить логи
docker-compose logs <service-name>

# Перезапустить проблемный сервис
docker-compose restart <service-name>
```

### База данных не готова
```bash
# Дождаться готовности и перезапустить зависимые сервисы
docker-compose restart monolith movies-service
```

### Очистка и полный перезапуск
```bash
make clean
make up
```

## Дополнительная информация

- Все сервисы используют Docker networking для внутренней коммуникации
- База данных инициализируется автоматически при первом запуске
- Kafka топики создаются автоматически
- Тесты запускаются в изолированном Docker контейнере 