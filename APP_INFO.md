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

## Kubernetes (Задание 3)

### Проверка CI/CD Pipeline

1. **Проверка GitHub Actions:**
   - Откройте вкладку Actions в GitHub репозитории
   - Убедитесь, что последняя сборка прошла успешно (зеленая галочка)
   - Проверьте, что все тесты прошли в CI

2. **Проверка образов в GitHub Container Registry:**
   - Откройте Packages в профиле GitHub
   - Убедитесь, что образы `proxy-service` и `events-service` опубликованы

### Развертывание в Kubernetes

#### Предварительные требования
- Minikube установлен и запущен
- kubectl настроен для работы с кластером
- Доступ к GitHub Container Registry

#### Последовательность команд для развертывания

1. **Создание namespace:**
   ```bash
   kubectl apply -f src/kubernetes/namespace.yaml
   ```

2. **Создание секретов и конфигураций:**
   ```bash
   kubectl apply -f src/kubernetes/configmap.yaml
   kubectl apply -f src/kubernetes/secret.yaml
   kubectl apply -f src/kubernetes/dockerconfigsecret.yaml
   kubectl apply -f src/kubernetes/postgres-init-configmap.yaml
   ```

3. **Развертывание инфраструктуры:**
   ```bash
   # База данных
   kubectl apply -f src/kubernetes/postgres.yaml
   
   # Ожидание готовности PostgreSQL
   kubectl wait --for=condition=ready pod -l app=postgres -n cinemaabyss --timeout=120s
   
   # Kafka и Zookeeper
   kubectl apply -f src/kubernetes/kafka/kafka.yaml
   
   # Ожидание готовности Kafka
   kubectl wait --for=condition=ready pod -l app=kafka -n cinemaabyss --timeout=120s
   ```

4. **Развертывание приложений:**
   ```bash
   # Монолит
   kubectl apply -f src/kubernetes/monolith.yaml
   
   # Микросервисы
   kubectl apply -f src/kubernetes/movies-service.yaml
   kubectl apply -f src/kubernetes/events-service.yaml
   kubectl apply -f src/kubernetes/proxy-service.yaml
   ```

5. **Настройка Ingress:**
   ```bash
   # Включение Ingress аддона
   minikube addons enable ingress
   
   # Применение Ingress конфигурации
   kubectl apply -f src/kubernetes/ingress.yaml
   
   # Добавление записи в hosts
   echo "127.0.0.1 cinemaabyss.example.com" | sudo tee -a /etc/hosts
   ```

6. **Запуск туннеля:**
   ```bash
   # В отдельном терминале
   minikube tunnel
   ```

#### Проверка развертывания

1. **Проверка статуса подов:**
   ```bash
   kubectl -n cinemaabyss get pods
   ```
   Ожидаемый результат: 7 подов в статусе Running

2. **Проверка Ingress:**
   ```bash
   kubectl get ingress -n cinemaabyss
   ```

3. **Проверка доступности API:**
   ```bash
   # Список фильмов
   curl http://cinemaabyss.example.com/api/movies
   
   # Health check Events Service
   curl http://cinemaabyss.example.com/api/events/health
   
   # Health check Proxy Service
   curl http://cinemaabyss.example.com/health
   ```

### Тестирование в Kubernetes

1. **Запуск Postman тестов:**
   ```bash
   cd tests/postman
   mkdir -p reports
   chmod 755 reports
   npm run test:kubernetes
   ```

2. **Ожидаемый результат:**
   - Все 22 теста должны пройти успешно
   - 0 failed assertions
   - События должны быть созданы и обработаны

3. **Проверка обработки событий:**
   ```bash
   # Просмотр логов Events Service (последние 50 строк)
   kubectl logs -n cinemaabyss deployment/events-service --tail 50
   
   # Поиск обработанных событий
   kubectl logs -n cinemaabyss deployment/events-service | grep "CONSUMED"
   ```

4. **Ожидаемые события в логах:**
   - `CONSUMED MOVIE EVENT`
   - `CONSUMED USER EVENT`  
   - `CONSUMED PAYMENT EVENT`

### Проверка Strangler Fig Pattern

1. **Изменение процента миграции:**
   ```bash
   # Установка 50% миграции
   kubectl patch configmap cinemaabyss-config -n cinemaabyss --patch '{"data":{"MOVIES_MIGRATION_PERCENT":"50"}}'
   
   # Перезапуск proxy-service
   kubectl rollout restart deployment proxy-service -n cinemaabyss
   ```

2. **Тестирование маршрутизации:**
   ```bash
   # Несколько запросов для проверки распределения
   for i in {1..10}; do
     echo "Request $i:"
     curl -s http://cinemaabyss.example.com/api/movies | jq -r '.[0].title'
     sleep 1
   done
   ```

3. **Просмотр логов маршрутизации:**
   ```bash
   kubectl logs -n cinemaabyss deployment/proxy-service --tail 20
   ```

### Очистка окружения

```bash
# Удаление всех ресурсов
kubectl delete all --all -n cinemaabyss
kubectl delete namespace cinemaabyss

# Удаление записи из hosts
sudo sed -i '/cinemaabyss.example.com/d' /etc/hosts
```

### Решение типичных проблем

1. **Поды не запускаются:**
   ```bash
   # Проверка событий
   kubectl get events -n cinemaabyss --sort-by='.lastTimestamp'
   
   # Проверка логов конкретного пода
   kubectl logs -n cinemaabyss <pod-name>
   ```

2. **Проблемы с доступом к образам:**
   ```bash
   # Проверка секрета
   kubectl get secret dockerconfigjson -n cinemaabyss -o yaml
   
   # Пересоздание секрета при необходимости
   kubectl delete secret dockerconfigjson -n cinemaabyss
   kubectl apply -f src/kubernetes/dockerconfigsecret.yaml
   ```

3. **Ingress не работает:**
   ```bash
   # Проверка статуса Ingress аддона
   minikube addons list | grep ingress
   
   # Проверка Ingress controller
   kubectl get pods -n ingress-nginx
   ``` 