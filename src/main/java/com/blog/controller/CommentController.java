package com.blog.controller;

import com.blog.dto.CreateCommentRequest;
import com.blog.dto.UpdateCommentRequest;
import com.blog.model.Comment;
import com.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  /**
   * GET /api/posts/{postId}/comments
   */
  @GetMapping
  public ResponseEntity<List<Comment>> getCommentsByPostId(
      @PathVariable(value = "postId") String postIdStr
  ) {
    // Для undefined возвращаем пустой массив
    if (postIdStr == null || "undefined".equals(postIdStr) || "null".equals(postIdStr)) {
      log.debug("Invalid postId: {}, returning empty list", postIdStr);
      return ResponseEntity.ok(Collections.emptyList());
    }

    Long postId;
    try {
      postId = Long.parseLong(postIdStr);
    } catch (NumberFormatException e) {
      log.warn("Invalid postId format: {}, returning empty list", postIdStr);
      return ResponseEntity.ok(Collections.emptyList());
    }

    log.info("GET /posts/{}/comments", postId);

    List<Comment> comments = commentService.getCommentsByPostId(postId);
    return ResponseEntity.ok(comments);
  }

  /**
   * GET /api/posts/{postId}/comments/{commentId}
   */
  @GetMapping("/{commentId}")
  public ResponseEntity<Comment> getCommentById(
      @PathVariable(value = "postId") Long postId,
      @PathVariable(value = "commentId") Long commentId
  ) {
    log.info("GET /posts/{}/comments/{}", postId, commentId);

    return commentService.getCommentById(commentId)
        .filter(comment -> comment.getPostId().equals(postId))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * POST /api/posts/{postId}/comments
   */
  @PostMapping
  public ResponseEntity<Comment> createComment(
      @PathVariable(value = "postId") Long postId,
      @RequestBody CreateCommentRequest request
  ) {
    log.info("POST /posts/{}/comments - text length: {}", postId,
        request.getText() != null ? request.getText().length() : 0);

    Comment comment = new Comment();
    comment.setPostId(postId);
    comment.setText(request.getText());

    Comment createdComment = commentService.createComment(comment);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
  }

  /**
   * PUT /api/posts/{postId}/comments/{commentId}
   */
  @PutMapping("/{commentId}")
  public ResponseEntity<Comment> updateComment(
      @PathVariable(value = "postId") Long postId,
      @PathVariable(value = "commentId") Long commentId,
      @RequestBody UpdateCommentRequest request
  ) {
    log.info("PUT /posts/{}/comments/{}", postId, commentId);

    return commentService.updateComment(commentId, request.getText())
        .filter(comment -> comment.getPostId().equals(postId))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * DELETE /api/posts/{postId}/comments/{commentId}
   */
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable(value = "postId") Long postId,
      @PathVariable(value = "commentId") Long commentId
  ) {
    log.info("DELETE /posts/{}/comments/{}", postId, commentId);

    if (commentService.deleteComment(commentId)) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }
}
