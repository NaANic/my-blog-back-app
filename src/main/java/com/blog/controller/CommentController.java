package com.blog.controller;

import com.blog.model.Comment;
import com.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @GetMapping
  public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
    List<Comment> comments = commentService.getCommentsByPostId(postId);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<Comment> getCommentById(@PathVariable Long postId, @PathVariable Long commentId) {
    return commentService.getCommentById(commentId)
        .filter(comment -> comment.getPostId().equals(postId))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Comment> createComment(@PathVariable Long postId, @RequestBody Comment comment) {
    comment.setPostId(postId);
    Comment createdComment = commentService.createComment(comment);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
    if (commentService.deleteComment(commentId)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }
}
