package com.blog.service;

import com.blog.model.Comment;
import com.blog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

  private final CommentRepository commentRepository;
  private final PostService postService;

  /**
   * Получить все комментарии поста
   */
  public List<Comment> getCommentsByPostId(Long postId) {
    return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
  }

  /**
   * Получить комментарий по ID
   */
  public Optional<Comment> getCommentById(Long id) {
    return commentRepository.findById(id);
  }

  /**
   * Создать новый комментарий
   */
  public Comment createComment(Comment comment) {
    comment.setCreatedAt(LocalDateTime.now());
    Comment savedComment = commentRepository.save(comment);

    // Увеличиваем счётчик комментариев у поста
    postService.incrementCommentsCount(comment.getPostId());

    log.info("Comment created: id={}, postId={}", savedComment.getId(), comment.getPostId());

    return savedComment;
  }

  /**
   * Обновить существующий комментарий
   */
  public Optional<Comment> updateComment(Long commentId, String newText) {
    return commentRepository.findById(commentId)
        .map(comment -> {
          comment.setText(newText);
          Comment updated = commentRepository.save(comment);

          log.info("Comment updated: id={}", commentId);

          return updated;
        });
  }

  /**
   * Удалить комментарий
   */
  public boolean deleteComment(Long id) {
    return commentRepository.findById(id)
        .map(comment -> {
          commentRepository.deleteById(id);

          // Уменьшаем счётчик комментариев у поста
          postService.decrementCommentsCount(comment.getPostId());

          log.info("Comment deleted: id={}, postId={}", id, comment.getPostId());

          return true;
        })
        .orElse(false);
  }

  /**
   * Подсчёт комментариев у поста
   */
  public int getCommentsCountByPostId(Long postId) {
    return commentRepository.countByPostId(postId);
  }
}
