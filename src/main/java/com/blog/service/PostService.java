package com.blog.service;

import com.blog.model.Post;
import com.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

  private final PostRepository postRepository;

  public List<Post> getAllPosts() {
    return postRepository.findAllOrderByCreatedAtDesc();
  }

  public Optional<Post> getPostById(Long id) {
    return postRepository.findById(id);
  }

  public Post createPost(Post post) {
    post.setLikesCount(0);
    post.setCommentsCount(0);
    post.setCreatedAt(LocalDateTime.now());
    post.setUpdatedAt(LocalDateTime.now());
    return postRepository.save(post);
  }

  public Optional<Post> updatePost(Long id, Post updatedPost) {
    return postRepository.findById(id)
        .map(existingPost -> {
          existingPost.setTitle(updatedPost.getTitle());
          existingPost.setText(updatedPost.getText());
          existingPost.setTags(updatedPost.getTags());
          existingPost.setUpdatedAt(LocalDateTime.now());
          return postRepository.save(existingPost);
        });
  }

  public boolean deletePost(Long id) {
    if (postRepository.existsById(id)) {
      postRepository.deleteById(id);
      return true;
    }
    return false;
  }

  public void incrementLikes(Long id) {
    postRepository.incrementLikesCount(id);
  }

  public void incrementCommentsCount(Long id) {
    postRepository.incrementCommentsCount(id);
  }

  public void decrementCommentsCount(Long id) {
    postRepository.decrementCommentsCount(id);
  }

  public List<Post> searchPosts(String keyword) {
    return postRepository.searchByTitleOrText(keyword);
  }
}
