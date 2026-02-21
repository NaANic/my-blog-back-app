package com.blog.controller;

import com.blog.dto.CreatePostRequest;
import com.blog.dto.PostDTO;
import com.blog.dto.PostListResponse;
import com.blog.dto.UpdatePostRequest;
import com.blog.exception.PostNotFoundException;
import com.blog.model.Post;
import com.blog.service.ImageStorageService;
import com.blog.service.PostSearchService;
import com.blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST контроллер для работы с постами
 */
@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;
  private final PostSearchService searchService;
  private final ImageStorageService imageService;

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
    PostListResponse response = searchService.searchPosts(search, pageNumber, pageSize);
    return ResponseEntity.ok(response);
  }

  /**
   * GET /api/posts/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<PostDTO> getPost(@PathVariable(value = "id") Long id) {
    log.info("GET /posts/{}", id);
    Post post = postService.getPostById(id);
    PostDTO dto = PostDTO.fromEntity(post, false);
    return ResponseEntity.ok(dto);
  }

  /**
   * POST /api/posts
   */
  @PostMapping
  public ResponseEntity<PostDTO> createPost(@Valid @RequestBody CreatePostRequest request) {
    log.info("POST /posts - title: '{}'", request.getTitle());
    Post post = new Post();
    post.setTitle(request.getTitle());
    post.setText(request.getText());
    post.setTags(String.join("", request.getTags().stream()
        .map(tag -> "#" + tag + "#")
        .toList()));
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
      @Valid @RequestBody UpdatePostRequest request
  ) {
    log.info("PUT /posts/{} - title: '{}'", id, request.getTitle());
    Post post = new Post();
    post.setTitle(request.getTitle());
    post.setText(request.getText());
    post.setTags(String.join("", request.getTags().stream()
        .map(tag -> "#" + tag + "#")
        .toList()));
    Post updated = postService.updatePost(id, post);
    PostDTO dto = PostDTO.fromEntity(updated, false);
    return ResponseEntity.ok(dto);
  }

  /**
   * DELETE /api/posts/{id}
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePost(@PathVariable(value = "id") Long id) {
    log.info("DELETE /posts/{}", id);
    postService.deletePost(id);
    imageService.deleteImage(id);
    return ResponseEntity.ok().build();
  }

  /**
   * POST /api/posts/{id}/likes
   */
  @PostMapping("/{id}/likes")
  public ResponseEntity<Integer> incrementLikes(@PathVariable(value = "id") Long id) {
    log.info("POST /posts/{}/likes", id);
    Integer newCount = postService.incrementLikes(id);
    return ResponseEntity.ok(newCount);
  }

  /**
   * GET /api/posts/{id}/image
   */
  @GetMapping("/{id}/image")
  public ResponseEntity<byte[]> getImage(@PathVariable(value = "id") Long id) throws IOException {
    log.info("📥 GET /posts/{}/image", id);
    if (!postService.existsById(id)) {
      throw new PostNotFoundException(id);
    }
    byte[] image = imageService.getImage(id);
    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_JPEG)
        .header("Cache-Control", "public, max-age=3600")
        .body(image);
  }

  /**
   * PUT /api/posts/{id}/image
   */
  @PutMapping("/{id}/image")
  public ResponseEntity<Void> uploadImage(
      @PathVariable(value = "id") Long id,
      @RequestParam(value = "image") MultipartFile file
  ) throws IOException {
    log.info("📤 PUT /posts/{}/image - {} bytes", id, file.getSize());
    if (!postService.existsById(id)) {
      throw new PostNotFoundException(id);
    }
    imageService.saveImage(id, file.getBytes());
    return ResponseEntity.ok().build();
  }
}
