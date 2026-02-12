package com.blog.service;

import com.blog.dto.PostDTO;
import com.blog.dto.PostListResponse;
import com.blog.model.Post;
import com.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.blog.exception.PostNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

  private final PostRepository postRepository;
  private byte[] defaultImage;
  private static final String UPLOADS_DIR = "uploads";

  @PostConstruct
  public void init() {
    loadDefaultImage();
    ensureUploadsDirectory();
  }

  private void loadDefaultImage() {
    try {
      ClassPathResource resource = new ClassPathResource("default-image.jpg");
      if (resource.exists()) {
        defaultImage = resource.getInputStream().readAllBytes();
        log.info("✅ Default image loaded from resources: {} bytes", defaultImage.length);
      } else {
        log.warn("⚠️  Default image not found in resources, creating placeholder");
        defaultImage = createPlaceholderImage();
      }
    } catch (Exception e) {
      log.error("❌ Failed to load default image: {}", e.getMessage(), e);
      defaultImage = createPlaceholderImage();
    }
  }

  private byte[] createPlaceholderImage() {
    // Простой серый квадрат 1x1 пиксель в формате JPEG
    return new byte[] {
        (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46,
        0x49, 0x46, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
        (byte) 0xFF, (byte) 0xDB, 0x00, 0x43, 0x00, 0x08, 0x06, 0x06, 0x07, 0x06,
        0x05, 0x08, 0x07, 0x07, 0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D,
        0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12, 0x13, 0x0F, 0x14, 0x1D, 0x1A, 0x1F,
        0x1E, 0x1D, 0x1A, 0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27, 0x20, 0x22, 0x2C,
        0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C, 0x30, 0x31, 0x34, 0x34, 0x34,
        0x1F, 0x27, 0x39, 0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32,
        (byte) 0xFF, (byte) 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01,
        0x01, 0x11, 0x00, (byte) 0xFF, (byte) 0xC4, 0x00, 0x14, 0x00, 0x01, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xC4, 0x00, 0x14, 0x10, 0x01, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00,
        0x00, 0x3F, 0x00, 0x00
    };
  }

  private void ensureUploadsDirectory() {
    try {
      Path uploadsPath = Paths.get(UPLOADS_DIR);
      if (!Files.exists(uploadsPath)) {
        Files.createDirectories(uploadsPath);
        log.info("📁 Created uploads directory: {}", uploadsPath.toAbsolutePath());
      }
    } catch (IOException e) {
      log.error("Failed to create uploads directory", e);
    }
  }

  /**
   * Сохранить изображение для поста
   */
  public void saveImage(Long postId, byte[] imageData) throws IOException {
    // Проверяем существование поста
    if (!postRepository.existsById(postId)) {
      throw new RuntimeException("Post not found with id: " + postId);
    }

    // Сохраняем файл
    Path imagePath = Paths.get(UPLOADS_DIR, postId + ".jpg");
    Files.write(imagePath, imageData);

    log.info("✅ Image saved for post {}: {} bytes at {}",
        postId, imageData.length, imagePath.toAbsolutePath());
  }

  /**
   * Получить изображение поста
   */
  public byte[] getImage(Long postId) throws IOException {
    log.debug("🔍 Looking for image for post {}", postId);

    // ✅ ПРОВЕРЯЕМ СУЩЕСТВОВАНИЕ ПОСТА
    if (!postRepository.existsById(postId)) {
      log.warn("⚠️  Post {} not found", postId);
      throw new PostNotFoundException(postId);
    }

    // Пытаемся найти сохранённое изображение
    Path imagePath = Paths.get(UPLOADS_DIR, postId + ".jpg");
    log.debug("📂 Checking path: {}", imagePath.toAbsolutePath());

    if (Files.exists(imagePath)) {
      byte[] imageData = Files.readAllBytes(imagePath);
      log.info("✅ Found saved image for post {}: {} bytes", postId, imageData.length);
      return imageData;
    }

    // Если изображения нет - возвращаем дефолтное
    log.debug("Image not found for post {}, returning default image", postId);

    if (defaultImage == null || defaultImage.length == 0) {
      log.error("❌ Default image not available!");
      throw new IOException("Default image not available");
    }

    log.info("✅ Returning default image: {} bytes", defaultImage.length);
    return defaultImage;
  }

  /**
   * Удалить изображение поста
   */
  private void deleteImage(Long postId) throws IOException {
    Path imagePath = Paths.get(UPLOADS_DIR, postId + ".jpg");
    if (Files.exists(imagePath)) {
      Files.delete(imagePath);
      log.info("Deleted image for post {}", postId);
    }
  }

  /**
   * Получить список постов с пагинацией и поиском
   */
  public PostListResponse getPosts(String search, int pageNumber, int pageSize) {
    log.debug("getPosts: search='{}', page={}, size={}", search, pageNumber, pageSize);

    // 1. Парсинг строки поиска
    SearchParams params = parseSearchString(search);
    log.debug("Parsed search: titleSearch='{}', tags={}", params.titleSearch, params.tags);

    // 2. Получаем ВСЕ посты для фильтрации
    List<Post> allPosts;
    if (params.titleSearch != null && !params.titleSearch.isEmpty()) {
      allPosts = postRepository.findAllWithTitleFilter(params.titleSearch);
    } else {
      allPosts = postRepository.findAllOrderByCreatedAtDesc();
    }
    log.debug("Found {} posts after title filter", allPosts.size());

    // 3. Фильтрация по тегам
    if (!params.tags.isEmpty()) {
      allPosts = filterByTags(allPosts, params.tags);
      log.debug("Found {} posts after tag filter", allPosts.size());
    }

    // 4. Применяем пагинацию вручную
    int totalCount = allPosts.size();
    int lastPage = (int) Math.ceil((double) totalCount / pageSize);
    if (lastPage == 0) lastPage = 1;

    int start = Math.min((pageNumber - 1) * pageSize, totalCount);
    int end = Math.min(start + pageSize, totalCount);

    List<Post> pagedPosts = (start < end) ? allPosts.subList(start, end) : List.of();

    // 5. Конвертация в DTO
    List<PostDTO> postDTOs = pagedPosts.stream()
        .map(post -> PostDTO.fromEntity(post, true))
        .collect(Collectors.toList());

    boolean hasPrev = pageNumber > 1;
    boolean hasNext = pageNumber < lastPage;

    return new PostListResponse(postDTOs, hasPrev, hasNext, lastPage);
  }

  /**
   * Получить пост по ID (с полным текстом)
   */
  public PostDTO getPostById(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

    return PostDTO.fromEntity(post, false); // false = НЕ обрезать текст
  }

  /**
   * Создать новый пост
   */
  public Post createPost(Post post) {
    post.setLikesCount(0);
    post.setCommentsCount(0);
    post.setCreatedAt(LocalDateTime.now());
    post.setUpdatedAt(LocalDateTime.now());

    return postRepository.save(post);
  }

  /**
   * Обновить существующий пост
   */
  public Optional<Post> updatePost(Long id, Post updatedPost) {
    return postRepository.findById(id)
        .map(existingPost -> {
          existingPost.setTitle(updatedPost.getTitle());
          existingPost.setText(updatedPost.getText());
          existingPost.setTags(updatedPost.getTags());
          existingPost.setUpdatedAt(LocalDateTime.now());
          return postRepository.save(existingPost);
        });
  }

  /**
   * Удалить пост (комментарии удалятся автоматически через CASCADE)
   */
  public boolean deletePost(Long id) {
    if (postRepository.existsById(id)) {
      postRepository.deleteById(id);
      // Удаляем также изображение если есть
      try {
        deleteImage(id);
      } catch (IOException e) {
        log.warn("Failed to delete image for post {}: {}", id, e.getMessage());
      }
      return true;
    }
    return false;
  }

  /**
   * Увеличить лайки и вернуть новое количество
   */
  public Integer incrementLikesAndGetCount(Long id) {
    if (!postRepository.existsById(id)) {
      throw new RuntimeException("Post not found with id: " + id);
    }

    postRepository.incrementLikesCount(id);
    return postRepository.getLikesCount(id);
  }

  /**
   * Увеличить счётчик комментариев
   */
  public void incrementCommentsCount(Long id) {
    postRepository.incrementCommentsCount(id);
  }

  /**
   * Уменьшить счётчик комментариев
   */
  public void decrementCommentsCount(Long id) {
    postRepository.decrementCommentsCount(id);
  }

  // ============= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =============

  /**
   * Парсинг строки поиска на title и теги
   */
  private SearchParams parseSearchString(String search) {
    SearchParams params = new SearchParams();

    if (search == null || search.trim().isEmpty()) {
      return params;
    }

    String[] words = search.trim().split("\\s+");
    List<String> nonTagWords = new ArrayList<>();

    for (String word : words) {
      if (word.startsWith("#")) {
        // Это тег
        String tag = word.substring(1);
        if (!tag.isEmpty()) {
          params.tags.add(tag.toLowerCase());
        }
      } else {
        // Это часть поиска по title
        nonTagWords.add(word);
      }
    }

    // Склеиваем все слова без # через пробел
    if (!nonTagWords.isEmpty()) {
      params.titleSearch = String.join(" ", nonTagWords);
    }

    return params;
  }

  /**
   * Фильтрация постов по тегам в Java
   */
  private List<Post> filterByTags(List<Post> posts, List<String> requiredTags) {
    return posts.stream()
        .filter(post -> {
          if (post.getTags() == null || post.getTags().isEmpty()) {
            return false;
          }

          String tagsLower = post.getTags().toLowerCase();

          // Проверяем что ВСЕ теги присутствуют
          for (String tag : requiredTags) {
            String searchPattern = "#" + tag.toLowerCase() + "#";
            if (!tagsLower.contains(searchPattern)) {
              return false;
            }
          }

          return true;
        })
        .collect(Collectors.toList());
  }

  /**
   * Вспомогательный класс для результатов парсинга поиска
   */
  private static class SearchParams {
    String titleSearch = null;
    List<String> tags = new ArrayList<>();
  }

  /**
   * Получить дефолтное изображение
   */
  public byte[] getDefaultImage() {
    if (defaultImage == null || defaultImage.length == 0) {
      return createPlaceholderImage();
    }
    return defaultImage;
  }
}
