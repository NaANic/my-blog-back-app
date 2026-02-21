package com.blog.service;

import com.blog.exception.PostNotFoundException;
import com.blog.model.Post;
import com.blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
    testPost.setText(
        "This is a test post with some content that is longer than 128 characters to test the truncation functionality in the service layer.");
    testPost.setTags("#java#spring#");
    testPost.setLikesCount(5);
    testPost.setCommentsCount(3);
    testPost.setCreatedAt(LocalDateTime.now());
    testPost.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void createPost_ShouldSetDefaultValues() {
    // Given
    Post newPost = new Post();
    newPost.setTitle("New Post");
    newPost.setText("Content");
    newPost.setTags("#test#");

    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
      Post saved = invocation.getArgument(0);
      saved.setId(1L);
      return saved;
    });

    // When
    Post result = postService.createPost(newPost);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getLikesCount());
    assertEquals(0, result.getCommentsCount());
    assertNotNull(result.getCreatedAt());
    assertNotNull(result.getUpdatedAt());
    verify(postRepository, times(1)).save(any(Post.class));
  }

  @Test
  void getPostById_WhenExists_ShouldReturnPost() {
    // Given
    when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

    // When
    Post result = postService.getPostById(1L);

    // Then
    assertNotNull(result);
    assertEquals(testPost.getId(), result.getId());
    assertEquals(testPost.getTitle(), result.getTitle());
    verify(postRepository, times(1)).findById(1L);
  }

  @Test
  void getPostById_WhenNotExists_ShouldThrowException() {
    // Given
    when(postRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(PostNotFoundException.class, () -> postService.getPostById(999L));
    verify(postRepository, times(1)).findById(999L);
  }

  @Test
  void updatePost_WhenExists_ShouldUpdateFields() {
    // Given
    Post updatedData = new Post();
    updatedData.setTitle("Updated Title");
    updatedData.setText("Updated Content");
    updatedData.setTags("#updated#");

    when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Post result = postService.updatePost(1L, updatedData);

    // Then
    assertNotNull(result);
    assertEquals("Updated Title", result.getTitle());
    assertEquals("Updated Content", result.getText());
    assertEquals("#updated#", result.getTags());
    assertNotNull(result.getUpdatedAt());
    verify(postRepository, times(1)).save(any(Post.class));
  }

  @Test
  void deletePost_WhenExists_ShouldDelete() {
    // Given
    when(postRepository.existsById(1L)).thenReturn(true);
    doNothing().when(postRepository).deleteById(1L);

    // When
    postService.deletePost(1L);

    // Then
    verify(postRepository, times(1)).deleteById(1L);
  }

  @Test
  void deletePost_WhenNotExists_ShouldThrowException() {
    // Given
    when(postRepository.existsById(999L)).thenReturn(false);

    // When & Then
    assertThrows(PostNotFoundException.class, () -> postService.deletePost(999L));
    verify(postRepository, never()).deleteById(anyLong());
  }

  @Test
  void incrementLikes_ShouldIncrementAndReturn() {
    // Given
    when(postRepository.existsById(1L)).thenReturn(true);
    doNothing().when(postRepository).incrementLikesCount(1L);
    when(postRepository.getLikesCount(1L)).thenReturn(6);

    // When
    Integer result = postService.incrementLikes(1L);

    // Then
    assertEquals(6, result);
    verify(postRepository, times(1)).incrementLikesCount(1L);
    verify(postRepository, times(1)).getLikesCount(1L);
  }

  @Test
  void incrementLikes_WhenPostNotExists_ShouldThrowException() {
    // Given
    when(postRepository.existsById(999L)).thenReturn(false);

    // When & Then
    assertThrows(PostNotFoundException.class, () -> postService.incrementLikes(999L));
  }

  @Test
  void incrementCommentsCount_ShouldCallRepository() {
    // Given
    doNothing().when(postRepository).incrementCommentsCount(1L);

    // When
    postService.incrementCommentsCount(1L);

    // Then
    verify(postRepository, times(1)).incrementCommentsCount(1L);
  }

  @Test
  void decrementCommentsCount_ShouldCallRepository() {
    // Given
    doNothing().when(postRepository).decrementCommentsCount(1L);

    // When
    postService.decrementCommentsCount(1L);

    // Then
    verify(postRepository, times(1)).decrementCommentsCount(1L);
  }

  @Test
  void existsById_ShouldReturnTrue_WhenExists() {
    // Given
    when(postRepository.existsById(1L)).thenReturn(true);

    // When
    boolean result = postService.existsById(1L);

    // Then
    assertTrue(result);
    verify(postRepository, times(1)).existsById(1L);
  }

  @Test
  void existsById_ShouldReturnFalse_WhenNotExists() {
    // Given
    when(postRepository.existsById(999L)).thenReturn(false);

    // When
    boolean result = postService.existsById(999L);

    // Then
    assertFalse(result);
    verify(postRepository, times(1)).existsById(999L);
  }
}
