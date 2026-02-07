package com.blog.service;

import com.blog.model.Post;
import com.blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  @Mock
  private PostRepository postRepository;

  @InjectMocks
  private PostService postService;

  private Post testPost;

  @BeforeEach
  void setUp() {
    testPost = new Post();
    testPost.setId(1L);
    testPost.setTitle("Test Post");
    testPost.setText("Test content");
    testPost.setTags("test,junit");
    testPost.setLikesCount(0);
    testPost.setCommentsCount(0);
    testPost.setCreatedAt(LocalDateTime.now());
    testPost.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void getAllPosts_ShouldReturnAllPosts() {
    // Given
    List<Post> expectedPosts = Arrays.asList(testPost);
    when(postRepository.findAllOrderByCreatedAtDesc()).thenReturn(expectedPosts);

    // When
    List<Post> actualPosts = postService.getAllPosts();

    // Then
    assertEquals(expectedPosts.size(), actualPosts.size());
    assertEquals(expectedPosts.get(0).getTitle(), actualPosts.get(0).getTitle());
    verify(postRepository, times(1)).findAllOrderByCreatedAtDesc();
  }

  @Test
  void getPostById_WhenExists_ShouldReturnPost() {
    // Given
    when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

    // When
    Optional<Post> result = postService.getPostById(1L);

    // Then
    assertTrue(result.isPresent());
    assertEquals(testPost.getTitle(), result.get().getTitle());
    verify(postRepository, times(1)).findById(1L);
  }

  @Test
  void createPost_ShouldSaveAndReturnPost() {
    // Given
    when(postRepository.save(any(Post.class))).thenReturn(testPost);

    // When
    Post result = postService.createPost(testPost);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getLikesCount());
    assertEquals(0, result.getCommentsCount());
    assertNotNull(result.getCreatedAt());
    verify(postRepository, times(1)).save(any(Post.class));
  }

  @Test
  void deletePost_WhenExists_ShouldReturnTrue() {
    // Given
    when(postRepository.existsById(1L)).thenReturn(true);
    doNothing().when(postRepository).deleteById(1L);

    // When
    boolean result = postService.deletePost(1L);

    // Then
    assertTrue(result);
    verify(postRepository, times(1)).deleteById(1L);
  }

  @Test
  void incrementLikes_ShouldCallRepository() {
    // Given
    doNothing().when(postRepository).incrementLikesCount(1L);

    // When
    postService.incrementLikes(1L);

    // Then
    verify(postRepository, times(1)).incrementLikesCount(1L);
  }
}
