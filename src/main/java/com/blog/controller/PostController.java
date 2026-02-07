package com.blog.controller;

import com.blog.model.Post;
import com.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  @GetMapping
  public ResponseEntity<List<Post>> getAllPosts() {
    List<Post> posts = postService.getAllPosts();
    return ResponseEntity.ok(posts);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Post> getPostById(@PathVariable Long id) {
    return postService.getPostById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Post> createPost(@RequestBody Post post) {
    Post createdPost = postService.createPost(post);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post post) {
    return postService.updatePost(id, post)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePost(@PathVariable Long id) {
    if (postService.deletePost(id)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping("/{id}/like")
  public ResponseEntity<Void> likePost(@PathVariable Long id) {
    if (postService.getPostById(id).isPresent()) {
      postService.incrementLikes(id);
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/search")
  public ResponseEntity<List<Post>> searchPosts(@RequestParam String keyword) {
    List<Post> posts = postService.searchPosts(keyword);
    return ResponseEntity.ok(posts);
  }
}
