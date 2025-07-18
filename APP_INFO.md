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

## Helm (Задание 4)

### Предварительные требования
- Minikube установлен и запущен
- kubectl настроен для работы с кластером  
- Helm установлен (версия 3.x)
- Доступ к GitHub Container Registry

### Установка Helm (если не установлен)

#### Вариант 1: Официальный скрипт установки
```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

#### Вариант 2: Прямое скачивание
```bash
cd ~
curl -L https://get.helm.sh/helm-v3.12.0-linux-amd64.tar.gz -o helm-v3.12.0-linux-amd64.tar.gz
tar -zxvf helm-v3.12.0-linux-amd64.tar.gz
sudo mv linux-amd64/helm /usr/local/bin/helm
helm version
```

### Проверка Helm Charts

1. **Проверка конфигурации values.yaml:**
   ```bash
   # Просмотр текущей конфигурации
   cat src/kubernetes/helm/values.yaml | grep repository
   
   # Убедиться что пути к образам правильные
   grep "ghcr.io/carameil" src/kubernetes/helm/values.yaml
   ```

2. **Проверка шаблонов:**
   ```bash
   # Валидация шаблонов
   helm template cinemaabyss ./src/kubernetes/helm --namespace cinemaabyss > /tmp/helm-output.yaml
   
   # Проверка что шаблоны генерируются корректно
   grep -E "(proxy-service|events-service)" /tmp/helm-output.yaml
   ```

### Развертывание через Helm

1. **Очистка предыдущих развертываний:**
   ```bash
   # Удаление ресурсов если есть
   kubectl delete all --all -n cinemaabyss
   kubectl delete namespace cinemaabyss
   
   # Или удаление через Helm если был установлен
   helm uninstall cinemaabyss -n cinemaabyss
   ```

2. **Установка через Helm:**
   ```bash
   # Перейти в корень проекта
   cd ~/projects/architecture-pro-cinemaabyss-s2
   
   # Установка chart'а
   helm install cinemaabyss ./src/kubernetes/helm --namespace cinemaabyss --create-namespace
   ```

3. **Проверка статуса установки:**
   ```bash
   # Статус Helm release
   helm status cinemaabyss -n cinemaabyss
   
   # Список установленных charts
   helm list -n cinemaabyss
   ```

### Проверка развертывания

1. **Проверка подов:**
   ```bash
   kubectl get pods -n cinemaabyss
   ```
   **Ожидаемый результат:** 7 подов в статусе Running
   - events-service-xxx
   - kafka-0  
   - monolith-xxx
   - movies-service-xxx
   - postgres-0
   - proxy-service-xxx
   - zookeeper-0

2. **Проверка сервисов:**
   ```bash
   kubectl get services -n cinemaabyss
   ```

3. **Проверка Ingress:**
   ```bash
   kubectl get ingress -n cinemaabyss
   ```

### Настройка доступа

1. **Включение Ingress (если не включен):**
   ```bash
   minikube addons enable ingress
   ```

2. **Добавление записи в hosts (если не добавлена):**
   ```bash
   echo "127.0.0.1 cinemaabyss.example.com" | sudo tee -a /etc/hosts
   ```

3. **Запуск туннеля (в отдельном терминале):**
   ```bash
   minikube tunnel
   ```

### Тестирование API

1. **Проверка основной функциональности:**
   ```bash
   # API фильмов
   curl http://cinemaabyss.example.com/api/movies
   
   # Health check Proxy Service  
   curl http://cinemaabyss.example.com/health
   
   # Health check Events Service
   curl http://cinemaabyss.example.com/api/events/health
   ```

2. **Тестирование событий:**
   ```bash
   # Создание тестового события
   curl -X POST http://cinemaabyss.example.com/api/events/movie \
     -H "Content-Type: application/json" \
     -d '{"movieId": 1, "title": "Test Movie", "action": "VIEWED", "userId": 123}'
   
   # Проверка логов Events Service
   kubectl logs -n cinemaabyss deployment/events-service --tail 20 | grep "CONSUMED"
   ```

3. **Запуск полного набора тестов:**
   ```bash
   cd tests/postman
   npm run test:kubernetes
   ```

### Управление конфигурацией через Helm

1. **Изменение конфигурации:**
   ```bash
   # Обновление процента миграции в values.yaml
   sed -i 's/moviesMigrationPercent: "100"/moviesMigrationPercent: "50"/' src/kubernetes/helm/values.yaml
   
   # Применение изменений
   helm upgrade cinemaabyss ./src/kubernetes/helm -n cinemaabyss
   ```

2. **Переопределение значений через командную строку:**
   ```bash
   # Установка с переопределением значений
   helm install cinemaabyss ./src/kubernetes/helm \
     --namespace cinemaabyss --create-namespace \
     --set config.moviesMigrationPercent=75 \
     --set proxyService.replicas=2
   ```

3. **Откат к предыдущей версии:**
   ```bash
   # Просмотр истории
   helm history cinemaabyss -n cinemaabyss
   
   # Откат к предыдущей версии
   helm rollback cinemaabyss 1 -n cinemaabyss
   ```

### Мониторинг и отладка

1. **Просмотр логов сервисов:**
   ```bash
   # Логи Proxy Service
   kubectl logs -n cinemaabyss deployment/proxy-service --tail 50
   
   # Логи Events Service  
   kubectl logs -n cinemaabyss deployment/events-service --tail 50
   
   # Логи всех подов
   kubectl logs -n cinemaabyss -l app=proxy-service --tail 20
   ```

2. **Проверка конфигурации:**
   ```bash
   # Просмотр ConfigMap
   kubectl get configmap cinemaabyss-config -n cinemaabyss -o yaml
   
   # Просмотр текущих значений Helm
   helm get values cinemaabyss -n cinemaabyss
   ```

### Решение проблем

1. **Ошибка доступа к образам:**
   ```bash
   # Проверка секрета
   kubectl get secret dockerconfigjson -n cinemaabyss -o yaml
   
   # Пересоздание через Helm
   helm upgrade cinemaabyss ./src/kubernetes/helm -n cinemaabyss --recreate-pods
   ```

2. **Проблемы с Kafka ClusterID:**
   ```bash
   # Полная очистка с PVC
   helm uninstall cinemaabyss -n cinemaabyss
   kubectl delete pvc --all -n cinemaabyss  
   kubectl delete namespace cinemaabyss
   
   # Повторная установка
   helm install cinemaabyss ./src/kubernetes/helm --namespace cinemaabyss --create-namespace
   ```

3. **Отладка шаблонов Helm:**
   ```bash
   # Проверка генерируемых манифестов
   helm template cinemaabyss ./src/kubernetes/helm --debug
   
   # Сухой запуск для проверки
   helm install cinemaabyss ./src/kubernetes/helm --namespace cinemaabyss --dry-run --debug
   ```

### Очистка

```bash
# Удаление через Helm
helm uninstall cinemaabyss -n cinemaabyss

# Удаление namespace
kubectl delete namespace cinemaabyss

# Удаление записи из hosts
sudo sed -i '/cinemaabyss.example.com/d' /etc/hosts
```

## Istio & Circuit Breaker (Задание 5)

### Предварительные требования
- Minikube установлен и запущен (`minikube start`).
- kubectl настроен для работы с кластером.
- Helm установлен (версия 3.x).

### 1. Установка Istio

**Шаг 1: Добавить Helm репозиторий Istio**
```bash
helm repo add istio https://istio-release.storage.googleapis.com/charts
helm repo update
```

**Шаг 2: Установить компоненты Istio в правильном порядке**
*Важно использовать `--wait`, чтобы каждый компонент был готов перед установкой следующего.*
```bash
# Установка базовых CRD
helm install istio-base istio/base -n istio-system --set defaultRevision=default --create-namespace

# Установка ядра Istio (istiod)
helm install istiod istio/istiod -n istio-system --wait

# Установка Ingress Gateway
helm install istio-ingressgateway istio/gateway -n istio-system --wait
```

**Шаг 3: Проверить установку Istio**
```bash
kubectl get pods -n istio-system
```
*Оба пода (`istiod` и `istio-ingressgateway`) должны быть в статусе `Running`.*

### 2. Развертывание приложения в Service Mesh

**Шаг 1: Развернуть приложение с помощью Helm**
```bash
helm install cinemaabyss ./src/kubernetes/helm --namespace cinemaabyss --create-namespace
```

**Шаг 2: Включить автоматическую инъекцию Istio Sidecar**
```bash
kubectl label namespace cinemaabyss istio-injection=enabled --overwrite
```

**Шаг 3: Перезапустить все сервисы для внедрения прокси**
*Это необходимо, так как инъекция происходит только при создании подов.*
```bash
kubectl rollout restart deployment -n cinemaabyss monolith
kubectl rollout restart deployment -n cinemaabyss movies-service
kubectl rollout restart deployment -n cinemaabyss events-service
kubectl rollout restart deployment -n cinemaabyss proxy-service
```

**Шаг 4: Проверить статус подов**
```bash
kubectl get pods -n cinemaabyss
```
*Убедитесь, что все поды `Running`, а у сервисов (`monolith`, `movies-service` и т.д.) `READY` статус `2/2`.*

### 3. Настройка и проверка Circuit Breaker

**Шаг 1: Применить конфигурацию Circuit Breaker**
```bash
kubectl apply -f src/kubernetes/circuit-breaker-config.yaml -n cinemaabyss
```

**Шаг 2: Развернуть клиент для нагрузочного тестирования (Fortio)**
```bash
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.25/samples/httpbin/sample-client/fortio-deploy.yaml -n cinemaabyss
```

**Шаг 3: Запустить нагрузочный тест**
```bash
# Получаем имя пода Fortio
FORTIO_POD=$(kubectl get pod -n cinemaabyss | grep fortio | awk '{print $1}')

# Запускаем тест
kubectl exec -n cinemaabyss $FORTIO_POD -c fortio -- fortio load -c 50 -qps 0 -n 500 -loglevel Warning http://movies-service:8081/api/movies
```
*Ожидаемый результат: смесь кодов `200` (успех) и `503` (сервис недоступен - сработал Circuit Breaker).*

**Шаг 4: Проверить статистику Istio**
*Эта команда покажет, сколько запросов было заблокировано.*
```bash
kubectl exec -n cinemaabyss $FORTIO_POD -c istio-proxy -- pilot-agent request GET stats | grep movies-service | grep "pending"
```
*Ищите ненулевое значение у `upstream_rq_pending_overflow`.*

### 4. Очистка окружения

```bash
# Удаляем Helm-релиз приложения
helm uninstall cinemaabyss -n cinemaabyss

# Удаляем Fortio
kubectl delete -f https://raw.githubusercontent.com/istio/istio/release-1.25/samples/httpbin/sample-client/fortio-deploy.yaml -n cinemaabyss

# Удаляем namespace приложения
kubectl delete namespace cinemaabyss

# Удаляем Istio
helm uninstall istio-ingressgateway -n istio-system
helm uninstall istiod -n istio-system
helm uninstall istio-base -n istio-system
kubectl delete namespace istio-system
```