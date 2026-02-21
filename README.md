# Blog Backend

REST API бэкенд для приложения-блога на Spring Framework 6.1 (без Spring Boot).

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
- **Spring Framework 6.1.14** (без Spring Boot)
- **Spring Data JDBC 3.3.5**
- **H2 Database** (in-memory)
- **Maven 3.9+**
- **Tomcat 10.1**
- **JUnit 5** + **Mockito** (тестирование)
- **Hibernate Validator** (Bean Validation)
- **Lombok** (упрощение кода)
- **Logback** (логирование)

## 📦 Требования

- JDK 21
- Maven 3.9+
- Apache Tomcat 10.1+
- Docker (для запуска фронтенда)

## 🚀 Быстрый старт

### 1. Клонирование репозитория

```bash
git clone https://github.com/your-username/nagatkin-blog-backend.git
cd nagatkin-blog-backend
```

### 2. Сборка проекта

```bash
mvn clean package
```

WAR-файл будет создан в `target/blog-backend.war`

### 3. Запуск тестов

```bash
mvn test
```

**Результат:**
```
Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS ✅
```

### 4. Деплой в Tomcat

#### Вариант A: Через IntelliJ IDEA (рекомендуется)

1. Откройте проект в IntelliJ IDEA
2. **Run** → **Edit Configurations**
3. Добавьте **Tomcat Server** → **Local**
4. Укажите путь к Tomcat
5. В **Deployment** добавьте: `nagatkin-blog-backend:war exploded`
6. **Application context**: `/`
7. Нажмите **Run**

#### Вариант B: Вручную

```bash
# Соберите WAR
mvn clean package

# Скопируйте в Tomcat
cp target/blog-backend.war $TOMCAT_HOME/webapps/

# Запустите Tomcat
$TOMCAT_HOME/bin/startup.sh

# Проверьте логи
tail -f $TOMCAT_HOME/logs/catalina.out
```

### 5. Проверка работы

Откройте в браузере:
```
http://localhost:8080/api/posts?search=&pageNumber=1&pageSize=5
```

Должен вернуться JSON с постами.

### 6. Запуск фронтенда (опционально)

```bash
cd docker-my-blog-front-app
docker compose up -d
```

Откройте: `http://localhost/`

## 📚 API Endpoints

### Посты

| Метод | Endpoint | Описание |
|-------|----------|----------|
| `GET` | `/api/posts?search=&pageNumber=1&pageSize=5` | Список постов с пагинацией и поиском |
| `GET` | `/api/posts/{id}` | Получить пост по ID |
| `POST` | `/api/posts` | Создать новый пост |
| `PUT` | `/api/posts/{id}` | Обновить пост |
| `DELETE` | `/api/posts/{id}` | Удалить пост (+ все комментарии) |
| `POST` | `/api/posts/{id}/likes` | Лайкнуть пост |
| `GET` | `/api/posts/{id}/image` | Получить изображение поста |
| `PUT` | `/api/posts/{id}/image` | Загрузить изображение |

### Комментарии

| Метод | Endpoint | Описание |
|-------|----------|----------|
| `GET` | `/api/posts/{postId}/comments` | Список комментариев поста |
| `GET` | `/api/posts/{postId}/comments/{id}` | Получить комментарий |
| `POST` | `/api/posts/{postId}/comments` | Создать комментарий |
| `PUT` | `/api/posts/{postId}/comments/{id}` | Обновить комментарий |
| `DELETE` | `/api/posts/{postId}/comments/{id}` | Удалить комментарий |

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
├── config/                    # Конфигурация Spring
│   ├── RootConfig.java       # Root context (БД, транзакции)
│   ├── WebConfig.java        # Web context (MVC, JSON)
│   ├── WebApplicationInitializer.java  # Servlet config
│   └── SimpleCorsFilter.java # CORS фильтр
├── controller/               # REST контроллеры
│   ├── PostController.java
│   ├── CommentController.java
│   └── GlobalExceptionHandler.java
├── dto/                      # Data Transfer Objects
│   ├── PostDTO.java
│   ├── PostListResponse.java
│   ├── CreatePostRequest.java
│   └── ...
├── exception/                # Кастомные исключения
│   └── PostNotFoundException.java
├── model/                    # Entity модели
│   ├── Post.java
│   └── Comment.java
├── repository/               # Spring Data JDBC
│   ├── PostRepository.java
│   └── CommentRepository.java
└── service/                  # Бизнес-логика
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

- **30 юнит-тестов** — тесты для всех сервисов
    - `PostServiceTest` — тесты CRUD операций
    - `PostSearchServiceTest` — тесты поиска и фильтрации
    - `ImageStorageServiceTest` — тесты работы с изображениями
    - `CommentServiceTest` — тесты комментариев
- **20 интеграционных тестов** — `PostControllerIntegrationTest`, `CommentControllerIntegrationTest`

### Запуск всех тестов

```bash
mvn test
```

### Запуск конкретного теста

```bash
mvn test -Dtest=PostServiceTest
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
22:31:50.016 [main] INFO  com.blog.service.PostService - ✅ Returning default image: 33328 bytes
```

## 🔧 Конфигурация

### application.properties

```properties
# Application
app.name=Blog Backend
app.version=1.0.0

# H2 Console (для отладки)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Logging
logging.level.com.blog=DEBUG
logging.level.org.springframework.jdbc=DEBUG
```

### CORS

CORS настроен для работы с фронтендом:
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
- **Connection pooling** — готово к добавлению (HikariCP)
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

**Модуль 1, Спринт 3** — Spring Framework без Spring Boot

### Требования проекта:

✅ Spring Framework 6.1+  
✅ Java 21  
✅ Maven  
✅ Сервлет-контейнер (Tomcat)  
✅ REST API  
✅ База данных (H2)  
✅ Тесты (50 юнит + интеграционных)  
✅ Git + GitHub  
✅ Bean Validation  
✅ Разделение ответственности (Single Responsibility Principle)

---

## 🎉 Изменения после ревью

### Рефакторинг архитектуры:
- ✅ Разделён монолитный `PostService` на **4 специализированных сервиса**:
    - `PostService` — базовые CRUD операции
    - `PostSearchService` — поиск и фильтрация
    - `ImageStorageService` — работа с изображениями
    - `CommentService` — работа с комментариями
- ✅ Улучшено разделение ответственности (Single Responsibility Principle)
- ✅ Каждый сервис отвечает за одну область функциональности

### Валидация:
- ✅ Добавлена Bean Validation для всех DTO
- ✅ Добавлены аннотации `@NotBlank`, `@Size`
- ✅ Контроллеры используют `@Valid`

### Обработка ошибок:
- ✅ Улучшен `GlobalExceptionHandler`
- ✅ Добавлена обработка `MethodArgumentNotValidException`
- ✅ Понятные сообщения об ошибках валидации

### Тестирование:
- ✅ Добавлены тесты для всех новых сервисов
- ✅ Обновлены интеграционные тесты
- ✅ **50 тестов проходят успешно**

### Качество кода:
- ✅ Улучшен `.gitignore`
- ✅ Чистая история коммитов
- ✅ Применён принцип Single Responsibility

---

**⭐ Если проект был полезен, поставьте звезду на GitHub!**
