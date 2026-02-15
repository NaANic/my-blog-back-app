package com.blog.service;

import com.blog.dto.PostListResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostSearchServiceTest {

  @Mock
  private PostRepository postRepository;

  @InjectMocks
  private PostSearchService searchService;

  private List<Post> testPosts;

  @BeforeEach
  void setUp() {
    testPosts = Arrays.asList(
        createPost(1L, "Spring Framework", "Content 1", "#java#spring#"),
        createPost(2L, "Java Tutorial", "Content 2", "#java#tutorial#"),
        createPost(3L, "Python Guide", "Content 3", "#python#"),
        createPost(4L, "Spring Boot", "Content 4", "#java#spring#boot#"),
        createPost(5L, "React Tutorial", "Content 5", "#react#javascript#")
    );
  }

  @Test
  void searchPosts_EmptySearch_ShouldReturnAllPosts() {
    // Given
    when(postRepository.findAllOrderByCreatedAtDesc()).thenReturn(testPosts);

    // When
    PostListResponse result = searchService.searchPosts("", 1, 10);

    // Then
    assertNotNull(result);
    assertEquals(5, result.getPosts().size());
    assertFalse(result.getHasPrev());
    assertFalse(result.getHasNext());
    assertEquals(1, result.getLastPage());
  }

  @Test
  void searchPosts_ByTitle_ShouldFilterPosts() {
    // Given
    when(postRepository.findAllWithTitleFilter("Spring"))
        .thenReturn(Arrays.asList(testPosts.get(0), testPosts.get(3)));

    // When
    PostListResponse result = searchService.searchPosts("Spring", 1, 10);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getPosts().size());
    assertTrue(result.getPosts().stream()
        .allMatch(p -> p.getTitle().contains("Spring")));
  }

  @Test
  void searchPosts_ByTag_ShouldFilterPosts() {
    // Given
    when(postRepository.findAllOrderByCreatedAtDesc()).thenReturn(testPosts);

    // When
    PostListResponse result = searchService.searchPosts("#java", 1, 10);

    // Then
    assertNotNull(result);
    assertEquals(3, result.getPosts().size());
    assertTrue(result.getPosts().stream()
        .allMatch(p -> p.getTags().contains("java")));
  }

  @Test
  void searchPosts_ByTitleAndTag_ShouldFilterPosts() {
    // Given
    when(postRepository.findAllWithTitleFilter("Spring"))
        .thenReturn(Arrays.asList(testPosts.get(0), testPosts.get(3)));

    // When
    PostListResponse result = searchService.searchPosts("Spring #java", 1, 10);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getPosts().size());
  }

  @Test
  void searchPosts_WithPagination_FirstPage() {
    // Given
    when(postRepository.findAllOrderByCreatedAtDesc()).thenReturn(testPosts);

    // When
    PostListResponse result = searchService.searchPosts("", 1, 2);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getPosts().size());
    assertFalse(result.getHasPrev());
    assertTrue(result.getHasNext());
    assertEquals(3, result.getLastPage());
  }

  @Test
  void searchPosts_WithPagination_MiddlePage() {
    // Given
    when(postRepository.findAllOrderByCreatedAtDesc()).thenReturn(testPosts);

    // When
    PostListResponse result = searchService.searchPosts("", 2, 2);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getPosts().size());
    assertTrue(result.getHasPrev());
    assertTrue(result.getHasNext());
    assertEquals(3, result.getLastPage());
  }

  @Test
  void searchPosts_WithPagination_LastPage() {
    // Given
    when(postRepository.findAllOrderByCreatedAtDesc()).thenReturn(testPosts);

    // When
    PostListResponse result = searchService.searchPosts("", 3, 2);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getPosts().size());
    assertTrue(result.getHasPrev());
    assertFalse(result.getHasNext());
    assertEquals(3, result.getLastPage());
  }

  private Post createPost(Long id, String title, String text, String tags) {
    Post post = new Post();
    post.setId(id);
    post.setTitle(title);
    post.setText(text);
    post.setTags(tags);
    post.setLikesCount(0);
    post.setCommentsCount(0);
    post.setCreatedAt(LocalDateTime.now());
    post.setUpdatedAt(LocalDateTime.now());
    return post;
  }
}
