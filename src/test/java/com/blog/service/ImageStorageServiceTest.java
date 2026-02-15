package com.blog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageStorageServiceTest {

  private ImageStorageService imageService;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    imageService = new ImageStorageService();
    ReflectionTestUtils.setField(imageService, "uploadDir", tempDir.toString());
    imageService.init();
  }

  @Test
  void saveImage_ShouldSaveFile() throws IOException {
    // Given
    Long postId = 1L;
    byte[] imageData = "test image data".getBytes();

    // When
    imageService.saveImage(postId, imageData);

    // Then
    Path imagePath = tempDir.resolve(postId + ".jpg");
    assertTrue(Files.exists(imagePath));
    assertArrayEquals(imageData, Files.readAllBytes(imagePath));
  }

  @Test
  void getImage_WhenExists_ShouldReturnImage() throws IOException {
    // Given
    Long postId = 1L;
    byte[] imageData = "test image data".getBytes();
    imageService.saveImage(postId, imageData);

    // When
    byte[] result = imageService.getImage(postId);

    // Then
    assertArrayEquals(imageData, result);
  }

  @Test
  void getImage_WhenNotExists_ShouldReturnDefaultImage() throws IOException {
    // Given
    Long postId = 999L;

    // When
    byte[] result = imageService.getImage(postId);

    // Then
    assertNotNull(result);
    assertTrue(result.length > 0);
  }

  @Test
  void deleteImage_ShouldDeleteFile() throws IOException {
    // Given
    Long postId = 1L;
    byte[] imageData = "test image data".getBytes();
    imageService.saveImage(postId, imageData);

    // When
    imageService.deleteImage(postId);

    // Then
    Path imagePath = tempDir.resolve(postId + ".jpg");
    assertFalse(Files.exists(imagePath));
  }

  @Test
  void getDefaultImage_ShouldReturnImage() {
    // When
    byte[] result = imageService.getDefaultImage();

    // Then
    assertNotNull(result);
    assertTrue(result.length > 0);
  }
}
