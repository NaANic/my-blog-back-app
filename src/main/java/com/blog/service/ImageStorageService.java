package com.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Сервис для работы с изображениями постов.
 * Отвечает за сохранение, загрузку и удаление изображений.
 */
@Slf4j
@Service
public class ImageStorageService {

  @Value("${app.upload.dir:uploads}")
  private String uploadDir;

  private byte[] defaultImage;

  @PostConstruct
  public void init() {
    loadDefaultImage();
    ensureUploadDirectory();
  }

  /**
   * Загрузка дефолтного изображения из ресурсов
   */
  private void loadDefaultImage() {
    try {
      ClassPathResource resource = new ClassPathResource("default-image.jpg");
      if (resource.exists()) {
        defaultImage = resource.getInputStream().readAllBytes();
        log.info("✅ Default image loaded: {} bytes", defaultImage.length);
      } else {
        log.warn("⚠️  Default image not found, creating placeholder");
        defaultImage = createPlaceholderImage();
      }
    } catch (Exception e) {
      log.error("❌ Failed to load default image: {}", e.getMessage(), e);
      defaultImage = createPlaceholderImage();
    }
  }

  /**
   * Создание директории для загрузок
   */
  private void ensureUploadDirectory() {
    try {
      Path path = Paths.get(uploadDir);
      if (!Files.exists(path)) {
        Files.createDirectories(path);
        log.info("📁 Created upload directory: {}", path.toAbsolutePath());
      }
    } catch (IOException e) {
      log.error("❌ Failed to create upload directory: {}", e.getMessage(), e);
    }
  }

  /**
   * Сохранить изображение поста
   *
   * @param postId ID поста
   * @param imageData данные изображения
   * @throws IOException если не удалось сохранить
   */
  public void saveImage(Long postId, byte[] imageData) throws IOException {
    Path imagePath = Paths.get(uploadDir, postId + ".jpg");
    Files.write(imagePath, imageData);
    log.info("💾 Image saved for post {}: {} bytes", postId, imageData.length);
  }

  /**
   * Получить изображение поста
   *
   * @param postId ID поста
   * @return данные изображения
   * @throws IOException если не удалось загрузить
   */
  public byte[] getImage(Long postId) throws IOException {
    Path imagePath = Paths.get(uploadDir, postId + ".jpg");

    if (Files.exists(imagePath)) {
      byte[] imageData = Files.readAllBytes(imagePath);
      log.debug("✅ Found image for post {}: {} bytes", postId, imageData.length);
      return imageData;
    }

    log.debug("📷 Image not found for post {}, returning default", postId);
    return getDefaultImage();
  }

  /**
   * Удалить изображение поста
   *
   * @param postId ID поста
   */
  public void deleteImage(Long postId) {
    try {
      Path imagePath = Paths.get(uploadDir, postId + ".jpg");
      if (Files.exists(imagePath)) {
        Files.delete(imagePath);
        log.info("🗑️  Image deleted for post {}", postId);
      }
    } catch (IOException e) {
      log.warn("⚠️  Failed to delete image for post {}: {}", postId, e.getMessage());
    }
  }

  /**
   * Получить дефолтное изображение
   *
   * @return данные дефолтного изображения
   */
  public byte[] getDefaultImage() {
    if (defaultImage == null || defaultImage.length == 0) {
      return createPlaceholderImage();
    }
    return defaultImage;
  }

  /**
   * Создать placeholder изображение (1x1 прозрачный PNG)
   */
  private byte[] createPlaceholderImage() {
    // Минимальный 1x1 прозрачный PNG
    return new byte[]{
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
        0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
        (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
        0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
        0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00,
        0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42,
        0x60, (byte) 0x82
    };
  }
}
