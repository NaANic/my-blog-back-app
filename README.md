# Blog Backend

REST API бэкенд для приложения-блога на Spring Boot 3.2.

## 📋 Описание

Полнофункциональный бэкенд блога с поддержкой:
- CRUD операций для постов и комментариев
- Поиска и фильтрации по названию и тегам
- Пагинации
- Загрузки и отображения изображений
- Лайков постов
- Каскадного удаления
- Bean Validation для входных данных

## 🛠 Технологии

- **Java 21**
- **Spring Boot 3.2.4**
- **Spring Data JDBC 3.2.2**
- **H2 Database** (in-memory)
- **Gradle 8.4** (с Wrapper)
- **JUnit 5** + **Mockito** (тестирование)
- **Spring Boot Test** (интеграционные тесты)
- **Hibernate Validator** (Bean Validation)
- **Lombok** (упрощение кода)
- **Logback** (логирование)

## 📦 Требования

- JDK 21
- Любая современная IDE (IntelliJ IDEA, Eclipse и т.д.)

## 🚀 Быстрый старт

### 1. Клонирование репозитория

```bash
git clone https://github.com/your-username/nagatkin-blog-backend.git
cd nagatkin-blog-backend
```

### 2. Сборка проекта

```bash
./gradlew clean build
```

Исполняемый JAR-файл будет создан в `build/libs/blog-backend-1.0.0.jar`.

### 3. Запуск тестов

```bash
./gradlew test
```

**Результат:**
```
Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESSFUL ✅
```

### 4. Запуск приложения

#### Вариант A: Через Gradle (рекомендуется для разработки)
```bash
./gradlew bootRun
```

#### Вариант B: Запуск исполняемого JAR
```bash
java -jar build/libs/blog-backend-1.0.0.jar
```

После запуска приложение будет доступно по адресу: `http://localhost:8080/api`

### 5. Проверка работы

```bash
curl http://localhost:8080/api/posts?search=&pageNumber=1&pageSize=5
```

Должен вернуться JSON со списком постов.

### 6. Запуск фронтенда (опционально)

```bash
cd docker-my-blog-front-app
docker compose up -d
```

Откройте: `http://localhost/`

## 📚 API Endpoints

**Базовый URL:** `http://localhost:8080/api`

### Посты

| Метод | Endpoint | Описание |
|-------|----------|----------|
| `GET` | `/posts?search=&pageNumber=1&pageSize=5` | Список постов с пагинацией и поиском |
| `GET` | `/posts/{id}` | Получить пост по ID |
| `POST` | `/posts` | Создать новый пост |
| `PUT` | `/posts/{id}` | Обновить пост |
| `DELETE` | `/posts/{id}` | Удалить пост (+ все комментарии) |
| `POST` | `/posts/{id}/likes` | Лайкнуть пост |
| `GET` | `/posts/{id}/image` | Получить изображение поста |
| `PUT` | `/posts/{id}/image` | Загрузить изображение |

### Комментарии

| Метод | Endpoint | Описание |
|-------|----------|----------|
| `GET` | `/posts/{postId}/comments` | Список комментариев поста |
| `GET` | `/posts/{postId}/comments/{id}` | Получить комментарий |
| `POST` | `/posts/{postId}/comments` | Создать комментарий |
| `PUT` | `/posts/{postId}/comments/{id}` | Обновить комментарий |
| `DELETE` | `/posts/{postId}/comments/{id}` | Удалить комментарий |

## 💡 Примеры запросов

### Получить список постов

```bash
curl "http://localhost:8080/api/posts?search=&pageNumber=1&pageSize=5"
```

**Ответ:**
```json
{
  "posts": [
    {
      "id": 1,
      "title": "Getting Started with Spring Framework",
      "text": "Spring Framework is a powerful framework...",
      "tags": ["java", "spring", "tutorial"],
      "likesCount": 10,
      "commentsCount": 2
    }
  ],
  "hasPrev": false,
  "hasNext": true,
  "lastPage": 2
}
```

### Создать пост

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My New Post",
    "text": "Post content in **Markdown**",
    "tags": ["java", "spring"]
  }'
```

### Загрузить изображение

```bash
curl -X PUT http://localhost:8080/api/posts/1/image \
  -F "image=@/path/to/image.jpg"
```

### Добавить комментарий

```bash
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Great post!",
    "postId": 1
  }'
```

### Поиск по тегу

```bash
curl "http://localhost:8080/api/posts?search=%23java&pageNumber=1&pageSize=10"
```

### Комбинированный поиск

```bash
curl "http://localhost:8080/api/posts?search=Spring%20%23java&pageNumber=1&pageSize=10"
```

## 🏗 Архитектура

```
src/main/java/com/blog/
├── BlogApplication.java           # Главный класс Spring Boot
├── config/                       
│   └── WebConfig.java             # Глобальная настройка CORS
├── controller/                    # REST контроллеры
│   ├── PostController.java
│   ├── CommentController.java
│   └── GlobalExceptionHandler.java
├── dto/                           # Data Transfer Objects
│   ├── PostDTO.java
│   ├── PostListResponse.java
│   ├── CreatePostRequest.java
│   └── ...
├── exception/                     # Кастомные исключения
│   └── PostNotFoundException.java
├── model/                         # Entity модели
│   ├── Post.java
│   └── Comment.java
├── repository/                    # Spring Data JDBC репозитории
│   ├── PostRepository.java
│   └── CommentRepository.java
└── service/                       # Бизнес-логика
    ├── PostService.java           # CRUD операции с постами
    ├── PostSearchService.java     # Поиск и фильтрация
    ├── ImageStorageService.java   # Работа с изображениями
    └── CommentService.java        # Работа с комментариями
```

### Разделение ответственности (Single Responsibility Principle)

#### PostService
**Ответственность:** Базовые CRUD операции с постами
- Создание, чтение, обновление, удаление постов
- Управление счётчиками (лайки, комментарии)
- Проверка существования постов

#### PostSearchService
**Ответственность:** Поиск и фильтрация постов
- Парсинг поисковых запросов (теги + текст)
- Фильтрация по названию и тегам
- Пагинация результатов

#### ImageStorageService
**Ответственность:** Работа с изображениями
- Сохранение изображений в файловую систему
- Загрузка изображений
- Управление дефолтным изображением
- Удаление изображений

#### CommentService
**Ответственность:** Работа с комментариями
- CRUD операции с комментариями
- Автоматическое обновление счётчиков в постах
- Каскадное удаление

## 🗄 База данных

### Схема БД

**Таблица `posts`:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `title` (VARCHAR(255))
- `text` (TEXT)
- `tags` (VARCHAR(1000)) — формат: `#tag1#tag2#`
- `likes_count` (INT, DEFAULT 0)
- `comments_count` (INT, DEFAULT 0)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Таблица `comments`:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `post_id` (BIGINT, FK → posts.id, ON DELETE CASCADE)
- `text` (TEXT)
- `created_at` (TIMESTAMP)

### Тестовые данные

При старте автоматически создаются:
- **5 постов** (Spring, Java, REST API и др.)
- **22 комментария**

## 🧪 Тестирование

### Структура тестов

- **30 юнит-тестов** — тесты для всех сервисов с использованием Mockito
  - `PostServiceTest` — тесты CRUD операций
  - `PostSearchServiceTest` — тесты поиска и фильтрации
  - `ImageStorageServiceTest` — тесты работы с изображениями
  - `CommentServiceTest` — тесты комментариев
- **20 интеграционных тестов** — `PostControllerIntegrationTest`, `CommentControllerIntegrationTest` с использованием `@SpringBootTest` и `@AutoConfigureMockMvc`

### Запуск всех тестов

```bash
./gradlew test
```

### Запуск конкретного теста

```bash
./gradlew test --tests PostServiceTest
```

### Покрытие

Тесты покрывают:
- ✅ CRUD операции
- ✅ Поиск и фильтрация
- ✅ Пагинация
- ✅ Загрузка изображений
- ✅ Валидация входных данных
- ✅ Обработка ошибок
- ✅ Разделение ответственности сервисов

## 📝 Логирование

Логи настроены через **Logback** (`src/main/resources/logback.xml`).

### Уровни логирования:

- `com.blog` — **DEBUG**
- `org.springframework` — **INFO**
- `org.springframework.jdbc` — **DEBUG**

### Примеры логов:

```
22:31:49.822 [main] INFO  c.blog.controller.CommentController - POST /posts/1/comments - text length: 34
22:31:49.866 [main] INFO  com.blog.service.CommentService - Comment created: id=23, postId=1
22:31:50.016 [main] INFO  com.blog.service.ImageStorageService - ✅ Default image loaded: 33328 bytes
```

## 🔧 Конфигурация

### application.properties

```properties
# Application
app.name=Blog Backend
app.version=1.0.0
app.upload.dir=uploads

# Server
server.port=8080
server.servlet.context-path=/api

# Datasource (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:blogdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (для отладки)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# SQL initialization
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql

# Logging
logging.level.com.blog=DEBUG
logging.level.org.springframework.jdbc=DEBUG

# Multipart (загрузка файлов)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=20MB
spring.servlet.multipart.enabled=true
```

### CORS

CORS настроен глобально через класс `WebConfig`:
- **Allowed Origins:** `*`
- **Allowed Methods:** `GET, POST, PUT, DELETE, PATCH, OPTIONS`
- **Allowed Headers:** `*`

### Multipart (загрузка файлов)

- **Max File Size:** 10 MB
- **Max Request Size:** 20 MB
- **Upload Directory:** `uploads/` (настраивается через `app.upload.dir`)

### Bean Validation

Все входные данные валидируются с помощью Bean Validation:
- `@NotBlank` — для обязательных строк
- `@Size` — для ограничения длины
- `@Valid` — для вложенных объектов

## 🐛 Известные особенности

### Запросы с `undefined`

Фронтенд может делать запросы с `undefined` в URL (например, `/api/posts/undefined/comments`). Это нормальное поведение React при монтировании компонентов.

**Решение:** Бэкенд возвращает пустые данные вместо ошибок:
- `/api/posts/undefined/comments` → `[]` (пустой массив)
- `/api/posts/undefined/image` → дефолтное изображение

### Дефолтное изображение

Если у поста нет изображения, возвращается дефолтное из `src/main/resources/default-image.jpg`.

## 📊 Производительность

- **H2 in-memory** — быстрая БД для разработки
- **Connection pooling** — HikariCP (встроен в Spring Boot)
- **Кеширование изображений** — через HTTP заголовки (`Cache-Control: max-age=3600`)
- **@Transactional(readOnly = true)** — оптимизация для операций чтения

## 🔐 Безопасность

⚠️ **Внимание:** Текущая версия НЕ содержит аутентификацию/авторизацию.

Для продакшена добавьте:
- Spring Security
- JWT токены

## 🚧 Roadmap

- [ ] Добавить Spring Security
- [ ] Миграция на PostgreSQL
- [ ] Добавить пользователей и авторизацию
- [ ] Реализовать вложенные комментарии
- [ ] Добавить категории постов
- [ ] Полнотекстовый поиск (Elasticsearch)

## 👨‍💻 Автор

**Алексей Нагаткин**

- GitHub: [@NaANic](https://github.com/NaANic)
- Email: a.nagatkin@mail.ru

## 📄 Лицензия

MIT License

---

## 🎓 Проектная работа

Этот проект выполнен в рамках курса **"Java-разработчик"** от Яндекс Практикум.

**Модуль 1, Спринт 4** — Spring Boot

### Требования проекта:

✅ Spring Boot 3.2+  
✅ Java 21  
✅ Gradle (вместо Maven)  
✅ Встроенный сервлет-контейнер (Tomcat)  
✅ REST API  
✅ База данных (H2)  
✅ Тесты (юнит + интеграционные с `@SpringBootTest`)  
✅ Git + GitHub  
✅ Bean Validation  
✅ Разделение ответственности (Single Responsibility Principle)

---

## 🎉 Изменения

- ✅ Переход на Spring Boot
- ✅ Замена Maven на Gradle
- ✅ Удаление устаревшей Java-конфигурации (RootConfig, WebConfig и др.)
- ✅ Настройка `application.properties` вместо XML/Java Config
- ✅ Интеграционные тесты с `@SpringBootTest` и `@AutoConfigureMockMvc`
- ✅ Глобальная конфигурация CORS
- ✅ Исполняемый JAR для простого запуска

---

**⭐ Если проект был полезен, поставьте звезду на GitHub!**
