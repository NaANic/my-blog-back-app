package com.blog.controller;

import com.blog.dto.CreatePostRequest;
import com.blog.dto.PostDTO;
import com.blog.dto.PostListResponse;
import com.blog.dto.UpdatePostRequest;
import com.blog.model.Post;
import com.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PostController {

  private final PostService postService;

  /**
   * GET /api/posts?search=&pageNumber=1&pageSize=5
   */
  @GetMapping
  public ResponseEntity<PostListResponse> getPosts(
      @RequestParam(value = "search", required = false, defaultValue = "") String search,
      @RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
      @RequestParam(value = "pageSize", defaultValue = "5") int pageSize
  ) {
    log.info("GET /posts - search: '{}', page: {}, size: {}", search, pageNumber, pageSize);
    PostListResponse response = postService.getPosts(search, pageNumber, pageSize);
    return ResponseEntity.ok(response);
  }

  /**
   * GET /api/posts/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<PostDTO> getPost(@PathVariable(value = "id") Long id) {
    log.info("GET /posts/{}", id);

    try {
      PostDTO post = postService.getPostById(id);
      return ResponseEntity.ok(post);
    } catch (RuntimeException e) {
      log.error("Post not found: {}", id);
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * POST /api/posts
   */
  @PostMapping
  public ResponseEntity<PostDTO> createPost(@RequestBody CreatePostRequest request) {
    log.info("POST /posts - title: '{}'", request.getTitle());

    Post post = new Post();
    post.setTitle(request.getTitle());
    post.setText(request.getText());
    post.setTags(request.getTagsString());

    Post created = postService.createPost(post);
    PostDTO dto = PostDTO.fromEntity(created, false);

    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  /**
   * PUT /api/posts/{id}
   */
  @PutMapping("/{id}")
  public ResponseEntity<PostDTO> updatePost(
      @PathVariable(value = "id") Long id,
      @RequestBody UpdatePostRequest request
  ) {
    log.info("PUT /posts/{} - title: '{}'", id, request.getTitle());

    Post post = new Post();
    post.setTitle(request.getTitle());
    post.setText(request.getText());
    post.setTags(request.getTagsString());

    return postService.updatePost(id, post)
        .map(updated -> ResponseEntity.ok(PostDTO.fromEntity(updated, false)))
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * DELETE /api/posts/{id}
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePost(@PathVariable(value = "id") Long id) {
    log.info("DELETE /posts/{}", id);

    if (postService.deletePost(id)) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  /**
   * POST /api/posts/{id}/likes
   */
  @PostMapping("/{id}/likes")
  public ResponseEntity<Integer> incrementLikes(@PathVariable(value = "id") Long id) {
    log.info("POST /posts/{}/likes", id);

    try {
      Integer newCount = postService.incrementLikesAndGetCount(id);
      return ResponseEntity.ok(newCount);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * PUT /api/posts/{id}/image
   */
  @PutMapping("/{id}/image")
  public ResponseEntity<Void> uploadImage(
      @PathVariable(value = "id") Long id,
      @RequestParam(value = "image") MultipartFile file
  ) {
    log.info("📤 PUT /posts/{}/image called", id);
    log.debug("File details: name={}, size={} bytes, contentType={}, originalFilename={}",
        file.getName(), file.getSize(), file.getContentType(), file.getOriginalFilename());

    try {
      // Проверка файла
      if (file.isEmpty()) {
        log.warn("Empty file received for post {}", id);
        return ResponseEntity.badRequest().build();
      }

      if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
        log.warn("File too large for post {}: {} bytes", id, file.getSize());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
      }

      // Проверяем, что это изображение
      String contentType = file.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
        log.warn("Invalid file type for post {}: {}", id, contentType);
        return ResponseEntity.badRequest()
            .header("X-Error", "File must be an image")
            .build();
      }

      // Получаем байты
      byte[] imageData = file.getBytes();
      log.info("Processing image for post {}: {} bytes, type: {}",
          id, imageData.length, contentType);

      // Сохраняем
      postService.saveImage(id, imageData);

      log.info("✅ Image uploaded successfully for post {}", id);
      return ResponseEntity.ok()
          .header("X-Message", "Image uploaded successfully")
          .build();

    } catch (IOException e) {
      log.error("❌ IO Error saving image for post {}: {}", id, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .header("X-Error", "Server error while saving image")
          .build();
    } catch (RuntimeException e) {
      log.error("❌ Post not found for image upload: {}", id, e);
      return ResponseEntity.notFound()
          .header("X-Error", "Post not found")
          .build();
    }
  }

  /**
   * GET /api/posts/{id}/image
   */
  @GetMapping("/{id}/image")
  public ResponseEntity<byte[]> getImage(@PathVariable(value = "id") String idStr) throws IOException {
    // Для undefined возвращаем дефолтное изображение
    if ("undefined".equals(idStr) || "null".equals(idStr)) {
      log.debug("Request with undefined ID, returning default image");
      byte[] defaultImg = postService.getDefaultImage();
      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_JPEG)
          .header("Cache-Control", "public, max-age=3600")
          .body(defaultImg);
    }

    Long id;
    try {
      id = Long.parseLong(idStr);
    } catch (NumberFormatException e) {
      log.warn("Invalid ID format: {}, returning default image", idStr);
      byte[] defaultImg = postService.getDefaultImage();
      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_JPEG)
          .body(defaultImg);
    }

    log.info("📥 GET /posts/{}/image called", id);

    byte[] image = postService.getImage(id);

    log.info("✅ Image retrieved successfully: {} bytes", image.length);

    // Определяем content-type
    MediaType mediaType = MediaType.IMAGE_JPEG;
    if (image.length >= 3) {
      if (image[0] == (byte) 0xFF && image[1] == (byte) 0xD8 && image[2] == (byte) 0xFF) {
        mediaType = MediaType.IMAGE_JPEG;
      } else if (image[0] == (byte) 0x89 && image[1] == 0x50 && image[2] == 0x4E) {
        mediaType = MediaType.IMAGE_PNG;
      } else if (image[0] == 0x47 && image[1] == 0x49 && image[2] == 0x46) {
        mediaType = MediaType.IMAGE_GIF;
      }
    }

    log.info("🖼️  Returning image: {} bytes, type: {}", image.length, mediaType);

    return ResponseEntity.ok()
        .contentType(mediaType)
        .header("Cache-Control", "public, max-age=3600")
        .body(image);
  }
}
