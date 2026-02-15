package com.blog.service;

import com.blog.exception.PostNotFoundException;
import com.blog.model.Post;
import com.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Сервис для управления постами.
 * Отвечает только за CRUD операции.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;

  /**
   * Получить пост по ID
   */
  @Transactional(readOnly = true)
  public Post getPostById(Long id) {
    return postRepository.findById(id)
        .orElseThrow(() -> new PostNotFoundException(id));
  }

  /**
   * Создать новый пост
   */
  public Post createPost(Post post) {
    post.setLikesCount(0);
    post.setCommentsCount(0);
    post.setCreatedAt(LocalDateTime.now());
    post.setUpdatedAt(LocalDateTime.now());
    Post savedPost = postRepository.save(post);
    log.info("Post created: id={}, title='{}'", savedPost.getId(), savedPost.getTitle());
    return savedPost;
  }

  /**
   * Обновить пост
   */
  public Post updatePost(Long id, Post updatedPost) {
    Post existingPost = getPostById(id);
    existingPost.setTitle(updatedPost.getTitle());
    existingPost.setText(updatedPost.getText());
    existingPost.setTags(updatedPost.getTags());
    existingPost.setUpdatedAt(LocalDateTime.now());
    Post saved = postRepository.save(existingPost);
    log.info("Post updated: id={}", id);
    return saved;
  }

  /**
   * Удалить пост
   */
  public void deletePost(Long id) {
    if (!postRepository.existsById(id)) {
      throw new PostNotFoundException(id);
    }
    postRepository.deleteById(id);
    log.info("Post deleted: id={}", id);
  }

  /**
   * Увеличить счётчик лайков
   */
  public Integer incrementLikes(Long id) {
    if (!postRepository.existsById(id)) {
      throw new PostNotFoundException(id);
    }
    postRepository.incrementLikesCount(id);
    Integer newCount = postRepository.getLikesCount(id);
    log.info("Post {} likes: {}", id, newCount);
    return newCount;
  }

  /**
   * Увеличить счётчик комментариев
   */
  public void incrementCommentsCount(Long postId) {
    postRepository.incrementCommentsCount(postId);
    log.debug("Comments count incremented for post {}", postId);
  }

  /**
   * Уменьшить счётчик комментариев
   */
  public void decrementCommentsCount(Long postId) {
    postRepository.decrementCommentsCount(postId);
    log.debug("Comments count decremented for post {}", postId);
  }

  /**
   * Проверить существование поста
   */
  @Transactional(readOnly = true)
  public boolean existsById(Long id) {
    return postRepository.existsById(id);
  }
}
