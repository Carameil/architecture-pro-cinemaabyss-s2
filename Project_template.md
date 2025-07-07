## Изучите [README.md](README.md) файл и структуру проекта.

## Задание 1

### 1. Спроектированная To-Be архитектура CinemaAbyss

На основе анализа текущей монолитной архитектуры и бизнес-требований платформы CinemaAbyss, спроектирована микросервисная архитектура, разделенная на следующие домены и ограниченные контексты:

#### Выделенные домены и поддомены:

**1. User Domain (Core Domain)**
- **User Management** - управление профилями пользователей
- **Authentication & Authorization** - аутентификация и управление доступом
- **User Preferences** - персональные настройки и предпочтения

**2. Content Domain (Core Domain)**
- **Movie Catalog** - каталог фильмов и метаданные
- **Genres Management** - управление жанрами
- **Ratings & Reviews** - рейтинги и отзывы пользователей

**3. Billing Domain (Core Domain)**
- **Payment Processing** - обработка платежных транзакций
- **Subscription Management** - управление подписками и тарифными планами
- **Billing History** - история платежей и счетов

**4. Analytics Domain (Supporting Domain)**
- **Viewing Analytics** - сбор данных о просмотрах пользователей
- **Recommendations Integration** - интеграция с внешними рекомендательными системами
- **User Behavior Analytics** - анализ поведения пользователей

**5. Notification Domain (Supporting Domain)**
- **Email Notifications** - email-уведомления
- **Push Notifications** - push-уведомления
- **In-App Messages** - внутренние сообщения

#### Ограниченные контексты (Bounded Contexts):

**1. User Context**
- Управление пользователями, профилями, аутентификацией
- Владеет данными: users, user_profiles, user_sessions, user_roles

**2. Content Context**
- Управление каталогом фильмов, жанрами, метаданными
- Владеет данными: movies, genres, movie_genres, movie_metadata

**3. Payment Context**
- Обработка платежей, интеграция с платежными системами
- Владеет данными: payments, payment_methods, transactions

**4. Subscription Context**
- Управление подписками, тарифными планами
- Владеет данными: subscriptions, subscription_plans, subscription_history

**5. Analytics Context**
- Сбор и анализ данных о поведении пользователей
- Владеет данными: viewing_history, user_analytics, recommendations

**6. Notification Context**
- Отправка уведомлений по различным каналам
- Владеет данными: notification_templates, notification_history

#### Выделенные микросервисы:

**Core Services:**
1. **User Service (Go)** - управление пользователями, аутентификация, профили
2. **Movie Service (Go)** - каталог фильмов, метаданные, жанры, рейтинги
3. **Payment Service (Java Spring)** - обработка платежей, интеграция с платежными системами
4. **Subscription Service (Java Spring)** - управление подписками и тарифными планами

**Supporting Services:**
5. **Analytics Service (Go)** - сбор аналитики, интеграция с внешними рекомендательными системами
6. **Notification Service (Go)** - отправка уведомлений по email и push
7. **Event Service (Java Spring)** - обработка событий, интеграция с Kafka

**Infrastructure Services:**
8. **API Gateway (Kong/Nginx)** - единая точка входа, маршрутизация, аутентификация

#### Интеграционное взаимодействие:

**1. Синхронное взаимодействие (REST API):**
- Через API Gateway для всех внешних запросов
- Прямые service-to-service вызовы для критических операций

**2. Асинхронное взаимодействие (Event-Driven):**
- Apache Kafka для событийной архитектуры
- События: UserCreated, PaymentProcessed, SubscriptionActivated, MovieViewed

**3. Единая точка вызова:**
- API Gateway (Proxy Service) как единая точка входа
- Маршрутизация, аутентификация, rate limiting
- Паттерн Strangler Fig для постепенной миграции

### Диаграммы C4

#### Context диаграмма
[Context диаграмма CinemaAbyss To-Be](https://www.planttext.com?text=hLPDR-Cs4BthLymQFJY15H-wlInGO1t7cqqH9q7ixAAdW1R7CW6AL4dANc_H_zuPYdByaGGzD1T5v7ozDsz6_ko3SA6fTFP1cK8t4c4LGkq_3OTER2vA5LRDel7e2ci2cd1Hs6fOQs7O9_T1QhKOZZ_c3tpqDBqTLi87T7JqF6QF7-6YvkZP1ubBPH2k9jzknlTti_-G9ZIraExNbtIARlCbMUI-TYgWxx99NFETtsNvSdyt7tsVpEwda_yrKJmJ6_Ismlot5yuwL4E7eaYGJozhwqFfqF-bZopsoXroSVwT-wNIrJlNXKEfmdCXd7pFIjictMUtep_ld-RFts_cNvzl9zUJkwl-MPO55JJ2-2EaDiAL4gKJ5UGm9eOhCLfilOUvpIyHHgvO4Kr5Q1ncsGC1iARFG0RGUw97ZvwaaB2qhW8-XytAmiBPZKSFbLqhz0EeX14blVjcuGkdwg2efI2CX8q8nOfkKjCet2zjOtxlHRfwPrrI8IjbADk8dye79uADwSUjQqhI5Y2aFCqU0yQibJMAlAcWBJM_WXKA7LQl9SzcqUBvqZP62hVj5v4YmHHnxZt2EXfwP_1t1lJNklRhTFwMEPrFLU4iPIIjeyyNmgDigmV2YO80bz25WGqflQMw1LOH-45781GY26tBmG7Ni51QSn06xv0cWvoaY6tOHyqA9D3_x61FVeH-BRQSube4t8WjzxizW7J20CWEdH4QaZ7KpX8KppaJn_rPe6fm0EJ-8FG0C1GNP_lPzmHEHUnLG9mfFMlfuMJy2kdz87iq18bQhT5j8J4AH1iO6zHI5IrcAeRmNpib1p0bbx8nXvNm41tMSCZQwE66yZuUd04T7bGplSrhkq52TSGve2bvbt5TTLItqEhdIy-RakHwqfokgEmHTHh1FMbTlZ5oeYX8kYIGqqdhVPtF7sR3nybi7ci_Yv7JdEs5dgPGoNDmF3u3M96kT9emAh-khNixmKlaKsMrgZdQ59sTtuKyYNIa4EwkFsevIrzeTE-5UQt9rnZyQrqw7QDTq8moau4gLm7iSaSbBONpyxbm9OQ3lGfVKF3JVpe_9viSNbi7OdyHin5LrqrW559Z22mR5anXD9ciYRLSEfe60rmjsRHqnEiR8x4c03oltQZuGl2YYFTNj4euEQFV6TGYiBoZmRKJiY6XLug-7x5JIsShbeVLjAGoYirDm4I8ncNaWrKIkU0F2FnX6sTmss5lTPmuJiiiNioGAOXjf3KLUFr5gSVBRmetoKTY82sq_UQgJDcQF19NoQNLWfGGB2p5siOV0YtpKUg7wcFZJe4jREhVWI2Ef72CUs5385e8-Oc-8Wr7Q-I6fYpnsP4oybjJh1pnslYzrTeI5kL1VeHim_jsHHz7wxRnCZASM6Y1E5pIHZ950ZosMek5PPCr9c8vyWk4p_Gl_Pg0Vm40)

#### Контейнерная диаграмма  
[Контейнерная диаграмма CinemaAbyss To-Be](https://www.planttext.com?text=bLVDKkGs4BxxAPIvx505p6LFEUtm4o0BnC53g9mesHOpAcp9aUI1gQqyJDueZvIMBDkIfO4C1yPgTtzgTN_gRd-J2WlLrbNo4sL5rPO4RPHgvC-BXS1lfskgDctUIY8ApXHXwhJWzQAfCDCs9rAL5SqNvrzFKYrwlBjTr5Wg8a3qV0uMc38YJXkDdzmk__pjSVNyTBEwVhwzlBgylvWV9OcYgYBe_2iQrD45nMk1QtI2pc5TusM-anAj-CaPGKjHRAWYXMe5IPAK2CdPNFjtZ6QFy3E3NzSAN5KlNDH8wyoEUWjSrfI1wbB_Kga4LjpOtc66rqGYUro4MOda3evKWt6IcQVdotSrR_2k1eKhhCWRte5bsWcGbMWmq2E2uGfPPOk4vfaIj27726nIN1q1j8CCljCg8s9B2w91z1BPjOTg597itW0eOUMLeEK4JX186UnUOaKvMwOt0F7WoH08FM3V93p_6M_XdTZDYp7IaxYdDkwp8_GZGV0tf7V-Hl9bqu3-4yaHF5MqCFlCZ6EuK3FxZa8An0k6im6IIQ049tomckUq8XtUdNcEGg9xM6zD-AmMThIce8OTAHSALpr7KBVqV7J80zYZknPl2jhGaImGSPSetpbRB-xNbBthLKRP6Zm4meWTQZZW7YF1MmNYOuHRjO4tlklctnVOJ92UANAolGxVI2fzLLnENV5PBwtD3T1qtREP6_mj9IFedLw6g9qO8735uG0rKHY8XGCqUqb6lFv6EOY_uYr6MICW53D7en4SJYorT8egsrmMSDlq0KRep95-WE-fZO7nevwrJGFa09DziGV3QWTHbAC3oruKncjyfOkFVnUbOTbQnFF1kA8lDbdZLlUEDDpDUrkI2fWiTW4osNgPkTnEyj9XTU8W6JPE5v1qb46TdSRd8ZVilCXx2chD74k3d7Afre9alzyE38sn1p0C6mr8nxZzA1uZ8qYMYGQhfzX-j5vZZrik0GsYHwhzi9xQ7ko1M0Pu98wB-ky_KETUhtdRBKq4SxB1Mmf5LJDi075tiJcxOIy26hfekuRha-8LlxnYlM-3Ymr1t_NILBF-eUD33MI0YuDomLzD3vwUGf2IIbDOuHUTQx1PlpJz72JwTfbUh9a5zo1aQ0L-mY48XWb1x-skPp61nm9avKV9tqdo1PaecO8EjLxlbpoGoiuPG_y29UFVzMgLPc0vghXTAQPbnm-_pzZHmzK5JtuHPX8faUBe31Uly6WWUY-2jYB8NosHQiHPF5ncAxknuzXXXfwh_zyK3k2qkhwBTOWEYD-S7a0BHh0fgEUEOppfGWTQ1rtdGFjuVpaG9D8W3aI8b_vFG20xLbUpgoyQ0uy7dA4wm-XJoiMJW0dSWEbIrHFBJS9OYlTRJ0Fl5jtzLj5m1oLsltqOQR-6nYqXKAQMdPG2-YD3kYMtp8OxZ5TV6DCshwZSG0PC_vH3SKIfu8eNl8f6xs3pQIWF1eX6zI0K0-DF2Rsz6zBF70dJqwFOTfW31d3r4vZeRJWOIL-F7IisK86XUmuVMa5RY4RFBsb_u8f2TuKjM_kHEhtCKP2WEDqmggZ1iQ-YTI48ugH4NX4bEzauZOUbOVWST4PE-T7LTRw5kn5mT3FeWD4CJRvyzGUjx8HX1mtE6FlEDGUTJD2J_cg7Y_0IzrD9raqSC2YuSyd7DxYtlRFH_SJOBUJ3C0G2LD6QAlij4bYQwgs7beW3qsdeXfNadTYa3o68R3yrx8QaSMYoPiatY6TRL-W_)

#### Ключевые архитектурные решения:

**1. API Gateway (Proxy Service)**
- Единая точка входа для всех клиентов
- Реализация паттерна Strangler Fig для постепенной миграции
- Централизованная аутентификация и авторизация
- Rate limiting и защита от DDoS

**2. Event-Driven Architecture**
- Apache Kafka как центральная шина событий
- Асинхронная коммуникация между сервисами
- Event Sourcing для критических операций
- Гарантированная доставка сообщений

**3. Database per Service**
- Каждый сервис владеет своими данными
- PostgreSQL для всех транзакционных данных включая аналитику
- Redis для кэширования
- Elasticsearch для поиска по каталогу фильмов

**4. Постепенная миграция**
- Movies Service уже извлечен из монолита
- Следующие кандидаты: User Service, Payment Service  
- Монолит остается до полной миграции всех доменов
- Feature flags для управления трафиком между монолитом и микросервисами

**5. Analytics Service для рекомендаций**
- Analytics Service необходим для интеграции с внешними рекомендательными системами
- Собирает данные о поведении пользователей (просмотры, рейтинги, избранное)
- Передает аналитику во внешние ML-системы для генерации персональных рекомендаций
- Все взаимодействие с пользователем остается синхронным, как и было в As-Is архитектуре


## Задание 2

### 1. Proxy
Команда КиноБездны уже выделила сервис метаданных о фильмах movies и вам необходимо реализовать бесшовный переход с применением паттерна Strangler Fig в части реализации прокси-сервиса (API Gateway), с помощью которого можно будет постепенно переключать траффик, используя фиче-флаг.


Реализуйте сервис на любом языке программирования в ./src/microservices/proxy.
Конфигурация для запуска сервиса через docker-compose уже добавлена
```yaml
  proxy-service:
    build:
      context: ./src/microservices/proxy
      dockerfile: Dockerfile
    container_name: cinemaabyss-proxy-service
    depends_on:
      - monolith
      - movies-service
      - events-service
    ports:
      - "8000:8000"
    environment:
      PORT: 8000
      MONOLITH_URL: http://monolith:8080
      #монолит
      MOVIES_SERVICE_URL: http://movies-service:8081 #сервис movies
      EVENTS_SERVICE_URL: http://events-service:8082 
      GRADUAL_MIGRATION: "true" # вкл/выкл простого фиче-флага
      MOVIES_MIGRATION_PERCENT: "50" # процент миграции
    networks:
      - cinemaabyss-network
```

- После реализации запустите postman тесты - они все должны быть зеленые.
- Отправьте запросы к API Gateway:
   ```bash
   curl http://localhost:8000/api/movies
   ```
- Протестируйте постепенный переход, изменив переменную окружения MOVIES_MIGRATION_PERCENT в файле docker-compose.yml.

### 2. Kafka
 Вам как архитектуру нужно также проверить гипотезу насколько просто реализовать применение Kafka в данной архитектуре.

Для этого нужно сделать MVP сервис events, который будет при вызове API создавать и сам же читать сообщения в топике Kafka.

    - Разработайте сервис на любом языке программирования с consumer'ами и producer'ами.
    - Реализуйте простой API, при вызове которого будут создаваться события User/Payment/Movie и обрабатываться внутри сервиса с записью в лог
    - Добавьте в docker-compose новый сервис, kafka там уже есть

Необходимые тесты для проверки этого API вызываются при запуске npm run test:local из папки tests/postman 
Приложите скриншот тестов и скриншот состояния топиков Kafka http://localhost:8090 


## Задание 3

Команда начала переезд в Kubernetes для лучшего масштабирования и повышения надежности. 
Вам, как архитектору осталось самое сложное:
 - реализовать CI/CD для сборки прокси сервиса
 - реализовать необходимые конфигурационные файлы для переключения трафика.


### CI/CD

 В папке .github/worflows доработайте деплой новых сервисов proxy и events в docker-build-push.yml , чтобы api-tests при сборке отрабатывали корректно при отправке коммита в вашу новую ветку.

Нужно доработать 
```yaml
on:
  push:
    branches: [ main ]
    paths:
      - 'src/**'
      - '.github/workflows/docker-build-push.yml'
  release:
    types: [published]
```
и добавить необходимые шаги в блок
```yaml
jobs:
  build-and-push:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - name: Checkout repository
        uses: actions/checkout@v3

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2

      - name: Log in to the Container registry
        uses: docker/login-action@v2
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

```
Как только сборка отработает и в github registry появятся ваши образы, можно переходить к блоку настройки Kubernetes
Успешным результатом данного шага является "зеленая" сборка и "зеленые" тесты


### Proxy в Kubernetes

#### Шаг 1
Для деплоя в kubernetes необходимо залогиниться в docker registry Github'а.
1. Создайте Personal Access Token (PAT) https://github.com/settings/tokens . Создавайте class с правом read:packages
2. В src/kubernetes/*.yaml (event-service, monolith, movies-service и proxy-service)  отредактируйте путь до ваших образов 
```bash
 spec:
      containers:
      - name: events-service
        image: ghcr.io/ваш логин/имя репозитория/events-service:latest
```
3. Добавьте в секрет src/kubernetes/dockerconfigsecret.yaml в поле
```bash
 .dockerconfigjson: значение в base64 файла ~/.docker/config.json
```

4. Если в ~/.docker/config.json нет значения для аутентификации
```json
{
        "auths": {
                "ghcr.io": {
                       тут пусто
                }
        }
}
```
то выполните 

и добавьте

```json 
 "auth": "имя пользователя:токен в base64"
```

Чтобы получить значение в base64 можно выполнить команду
```bash
 echo -n ваш_логин:ваш_токен | base64
```

После заполнения config.json, также прогоните содержимое через base64

```bash
cat .docker/config.json | base64
```

и полученное значение добавляем в

```bash
 .dockerconfigjson: значение в base64 файла ~/.docker/config.json
```

#### Шаг 2

  Доработайте src/kubernetes/event-service.yaml и src/kubernetes/proxy-service.yaml

  - Необходимо создать Deployment и Service 
  - Доработайте ingress.yaml, чтобы можно было с помощью тестов проверить создание событий
  - Выполните дальшейшие шаги для поднятия кластера:

  1. Создайте namespace:
  ```bash
  kubectl apply -f src/kubernetes/namespace.yaml
  ```
  2. Создайте секреты и переменные
  ```bash
  kubectl apply -f src/kubernetes/configmap.yaml
  kubectl apply -f src/kubernetes/secret.yaml
  kubectl apply -f src/kubernetes/dockerconfigsecret.yaml
  kubectl apply -f src/kubernetes/postgres-init-configmap.yaml
  ```

  3. Разверните базу данных:
  ```bash
  kubectl apply -f src/kubernetes/postgres.yaml
  ```

  На этом этапе если вызвать команду
  ```bash
  kubectl -n cinemaabyss get pod
  ```
  Вы увидите

  NAME         READY   STATUS    
  postgres-0   1/1     Running   

  4. Разверните Kafka:
  ```bash
  kubectl apply -f src/kubernetes/kafka/kafka.yaml
  ```

  Проверьте, теперь должно быть запущено 3 пода, если что-то не так, то посмотрите логи
  ```bash
  kubectl -n cinemaabyss logs имя_пода (например - kafka-0)
  ```

  5. Разверните монолит:
  ```bash
  kubectl apply -f src/kubernetes/monolith.yaml
  ```
  6. Разверните микросервисы:
  ```bash
  kubectl apply -f src/kubernetes/movies-service.yaml
  kubectl apply -f src/kubernetes/events-service.yaml
  ```
  7. Разверните прокси-сервис:
  ```bash
  kubectl apply -f src/kubernetes/proxy-service.yaml
  ```

  После запуска и поднятия подов вывод команды 
  ```bash
  kubectl -n cinemaabyss get pod
  ```

  Будет наподобие такого

  NAME                              READY   STATUS    

  events-service-7587c6dfd5-6whzx   1/1     Running  

  kafka-0                           1/1     Running   

  monolith-8476598495-wmtmw         1/1     Running  

  movies-service-6d5697c584-4qfqs   1/1     Running  

  postgres-0                        1/1     Running  

  proxy-service-577d6c549b-6qfcv    1/1     Running  

  zookeeper-0                       1/1     Running 

  8. Добавим ingress

  - добавьте аддон
  ```bash
  minikube addons enable ingress
  ```
  ```bash
  kubectl apply -f src/kubernetes/ingress.yaml
  ```
  9. Добавьте в /etc/hosts
  127.0.0.1 cinemaabyss.example.com

  10. Вызовите
  ```bash
  minikube tunnel
  ```
  11. Вызовите https://cinemaabyss.example.com/api/movies
  Вы должны увидеть вывод списка фильмов
  Можно поэкспериментировать со значением   MOVIES_MIGRATION_PERCENT в src/kubernetes/configmap.yaml и убедится, что вызовы movies уходят полностью в новый сервис

  12. Запустите тесты из папки tests/postman
  ```bash
   npm run test:kubernetes
  ```
  Часть тестов с health-чек упадет, но создание событий отработает.
  Откройте логи event-service и сделайте скриншот обработки событий

#### Шаг 3
Добавьте сюда скриншота вывода при вызове https://cinemaabyss.example.com/api/movies и  скриншот вывода event-service после вызова тестов.


## Задание 4
Для простоты дальнейшего обновления и развертывания вам как архитектуру необходимо так же реализовать helm-чарты для прокси-сервиса и проверить работу 

Для этого:
1. Перейдите в директорию helm и отредактируйте файл values.yaml

```yaml
# Proxy service configuration
proxyService:
  enabled: true
  image:
    repository: ghcr.io/db-exp/cinemaabysstest/proxy-service
    tag: latest
    pullPolicy: Always
  replicas: 1
  resources:
    limits:
      cpu: 300m
      memory: 256Mi
    requests:
      cpu: 100m
      memory: 128Mi
  service:
    port: 80
    targetPort: 8000
    type: ClusterIP
```

- Вместо ghcr.io/db-exp/cinemaabysstest/proxy-service напишите свой путь до образа для всех сервисов
- для imagePullSecret проставьте свое значение (скопируйте из конфигурации kubernetes)
  ```yaml
  imagePullSecrets:
      dockerconfigjson: ewoJImF1dGhzIjogewoJCSJnaGNyLmlvIjogewoJCQkiYXV0aCI6ICJaR0l0Wlhod09tZG9jRjl2UTJocVZIa3dhMWhKVDIxWmFVZHJOV2hRUW10aFVXbFZSbTVaTjJRMFNYUjRZMWM9IgoJCX0KCX0sCgkiY3JlZHNTdG9yZSI6ICJkZXNrdG9wIiwKCSJjdXJyZW50Q29udGV4dCI6ICJkZXNrdG9wLWxpbnV4IiwKCSJwbHVnaW5zIjogewoJCSIteC1jbGktaGludHMiOiB7CgkJCSJlbmFibGVkIjogInRydWUiCgkJfQoJfSwKCSJmZWF0dXJlcyI6IHsKCQkiaG9va3MiOiAidHJ1ZSIKCX0KfQ==
  ```

2. В папке ./templates/services заполните шаблоны для proxy-service.yaml и events-service.yaml (опирайтесь на свою kubernetes конфигурацию - смысл helm'а сделать шаблоны для быстрого обновления и установки)

```yaml
template:
    metadata:
      labels:
        app: proxy-service
    spec:
      containers:
       Тут ваша конфигурация
```

3. Проверьте установку
Сначала удалим установку руками

```bash
kubectl delete all --all -n cinemaabyss
kubectl delete  namespace cinemaabyss
```
Запустите 
```bash
helm install cinemaabyss .\src\kubernetes\helm --namespace cinemaabyss --create-namespace
```
Если в процессе будет ошибка
```code
[2025-04-08 21:43:38,780] ERROR Fatal error during KafkaServer startup. Prepare to shutdown (kafka.server.KafkaServer)
kafka.common.InconsistentClusterIdException: The Cluster ID OkOjGPrdRimp8nkFohYkCw doesn't match stored clusterId Some(sbkcoiSiQV2h_mQpwy05zQ) in meta.properties. The broker is trying to join the wrong cluster. Configured zookeeper.connect may be wrong.
```

Проверьте развертывание:
```bash
kubectl get pods -n cinemaabyss
minikube tunnel
```

Потом вызовите 
https://cinemaabyss.example.com/api/movies
и приложите скриншот развертывания helm и вывода https://cinemaabyss.example.com/api/movies


# Задание 5
Компания планирует активно развиваться и для повышения надежности, безопасности, реализации сетевых паттернов типа Circuit Breaker и канареечного деплоя вам как архитектору необходимо развернуть istio и настроить circuit breaker для monolith и movies сервисов.

```bash

helm repo add istio https://istio-release.storage.googleapis.com/charts
helm repo update

helm install istio-base istio/base -n istio-system --set defaultRevision=default --create-namespace
helm install istio-ingressgateway istio/gateway -n istio-system
helm install istiod istio/istiod -n istio-system --wait

helm install cinemaabyss .\src\kubernetes\helm --namespace cinemaabyss --create-namespace

kubectl label namespace cinemaabyss istio-injection=enabled --overwrite

kubectl get namespace -L istio-injection

kubectl apply -f .\src\kubernetes\circuit-breaker-config.yaml -n cinemaabyss

```

Тестирование

# fortio
```bash
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.25/samples/httpbin/sample-client/fortio-deploy.yaml -n cinemaabyss
```

# Get the fortio pod name
```bash
FORTIO_POD=$(kubectl get pod -n cinemaabyss | grep fortio | awk '{print $1}')

kubectl exec -n cinemaabyss $FORTIO_POD -c fortio -- fortio load -c 50 -qps 0 -n 500 -loglevel Warning http://movies-service:8081/api/movies
```
Например,

```bash
kubectl exec -n cinemaabyss fortio-deploy-b6757cbbb-7c9qg  -c fortio -- fortio load -c 50 -qps 0 -n 500 -loglevel Warning http://movies-service:8081/api/movies
```

Вывод будет типа такого

```bash
IP addresses distribution:
10.106.113.46:8081: 421
Code 200 : 79 (15.8 %)
Code 500 : 22 (4.4 %)
Code 503 : 399 (79.8 %)
```
Можно еще проверить статистику

```bash
kubectl exec -n cinemaabyss fortio-deploy-b6757cbbb-7c9qg -c istio-proxy -- pilot-agent request GET stats | grep movies-service | grep pending
```

И там смотрим 

```bash
cluster.outbound|8081||movies-service.cinemaabyss.svc.cluster.local;.upstream_rq_pending_total: 311 - столько раз срабатывал circuit breaker
You can see 21 for the upstream_rq_pending_overflow value which means 21 calls so far have been flagged for circuit breaking.
```

Приложите скриншот работы circuit breaker'а

Удаляем все
```bash
istioctl uninstall --purge
kubectl delete namespace istio-system
kubectl delete all --all -n cinemaabyss
kubectl delete namespace cinemaabyss
```
