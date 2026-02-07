package com.blog.service;

import com.blog.model.Comment;
import com.blog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

  private final CommentRepository commentRepository;
  private final PostService postService;

  public List<Comment> getCommentsByPostId(Long postId) {
    return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
  }

  public Optional<Comment> getCommentById(Long id) {
    return commentRepository.findById(id);
  }

  public Comment createComment(Comment comment) {
    comment.setCreatedAt(LocalDateTime.now());
    Comment savedComment = commentRepository.save(comment);

    // Увеличиваем счётчик комментариев у поста
    postService.incrementCommentsCount(comment.getPostId());

    return savedComment;
  }

  public boolean deleteComment(Long id) {
    return commentRepository.findById(id)
        .map(comment -> {
          commentRepository.deleteById(id);
          // Уменьшаем счётчик комментариев у поста
          postService.decrementCommentsCount(comment.getPostId());
          return true;
        })
        .orElse(false);
  }

  public int getCommentsCountByPostId(Long postId) {
    return commentRepository.countByPostId(postId);
  }
}
