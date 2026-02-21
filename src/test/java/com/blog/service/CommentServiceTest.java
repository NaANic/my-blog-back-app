package com.blog.service;

import com.blog.model.Comment;
import com.blog.repository.CommentRepository;
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
class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private PostService postService;

  @InjectMocks
  private CommentService commentService;

  private Comment testComment;

  @BeforeEach
  void setUp() {
    testComment = new Comment();
    testComment.setId(1L);
    testComment.setPostId(1L);
    testComment.setText("Test comment");
    testComment.setCreatedAt(LocalDateTime.now());
  }

  @Test
  void getCommentsByPostId_ShouldReturnComments() {
    // Given
    List<Comment> comments = Arrays.asList(testComment);
    when(commentRepository.findByPostIdOrderByCreatedAtDesc(1L)).thenReturn(comments);

    // When
    List<Comment> result = commentService.getCommentsByPostId(1L);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testComment.getText(), result.get(0).getText());
    verify(commentRepository, times(1)).findByPostIdOrderByCreatedAtDesc(1L);
  }

  @Test
  void getCommentById_WhenExists_ShouldReturnComment() {
    // Given
    when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

    // When
    Optional<Comment> result = commentService.getCommentById(1L);

    // Then
    assertTrue(result.isPresent());
    assertEquals(testComment.getText(), result.get().getText());
    verify(commentRepository, times(1)).findById(1L);
  }

  @Test
  void createComment_ShouldSaveAndIncrementCounter() {
    // Given
    Comment newComment = new Comment();
    newComment.setPostId(1L);
    newComment.setText("New comment");

    when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
      Comment saved = invocation.getArgument(0);
      saved.setId(2L);
      return saved;
    });
    doNothing().when(postService).incrementCommentsCount(1L);

    // When
    Comment result = commentService.createComment(newComment);

    // Then
    assertNotNull(result);
    assertNotNull(result.getCreatedAt());
    verify(commentRepository, times(1)).save(any(Comment.class));
    verify(postService, times(1)).incrementCommentsCount(1L);
  }

  @Test
  void updateComment_WhenExists_ShouldUpdateText() {
    // Given
    when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
    when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Optional<Comment> result = commentService.updateComment(1L, "Updated text");

    // Then
    assertTrue(result.isPresent());
    assertEquals("Updated text", result.get().getText());
    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  void deleteComment_WhenExists_ShouldDeleteAndDecrementCounter() {
    // Given
    when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
    doNothing().when(commentRepository).deleteById(1L);
    doNothing().when(postService).decrementCommentsCount(1L);

    // When
    boolean result = commentService.deleteComment(1L);

    // Then
    assertTrue(result);
    verify(commentRepository, times(1)).deleteById(1L);
    verify(postService, times(1)).decrementCommentsCount(1L);
  }

  @Test
  void deleteComment_WhenNotExists_ShouldReturnFalse() {
    // Given
    when(commentRepository.findById(999L)).thenReturn(Optional.empty());

    // When
    boolean result = commentService.deleteComment(999L);

    // Then
    assertFalse(result);
    verify(commentRepository, never()).deleteById(anyLong());
  }

  @Test
  void getCommentsCountByPostId_ShouldReturnCount() {
    // Given
    when(commentRepository.countByPostId(1L)).thenReturn(5);

    // When
    int result = commentService.getCommentsCountByPostId(1L);

    // Then
    assertEquals(5, result);
    verify(commentRepository, times(1)).countByPostId(1L);
  }
}
