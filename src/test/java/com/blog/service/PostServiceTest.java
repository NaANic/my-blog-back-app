package com.blog.service;

import com.blog.dto.PostDTO;
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
    testPost.setText("This is a test post with some content that is longer than 128 characters to test the truncation functionality in the service layer.");
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
  void getPostById_WhenExists_ShouldReturnFullPost() {
    // Given
    when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

    // When
    PostDTO result = postService.getPostById(1L);

    // Then
    assertNotNull(result);
    assertEquals(testPost.getId(), result.getId());
    assertEquals(testPost.getTitle(), result.getTitle());
    assertEquals(testPost.getText(), result.getText()); // Полный текст
    assertEquals(2, result.getTags().size());
    assertTrue(result.getTags().contains("java"));
    assertTrue(result.getTags().contains("spring"));
    verify(postRepository, times(1)).findById(1L);
  }

  @Test
  void getPostById_WhenNotExists_ShouldThrowException() {
    // Given
    when(postRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(RuntimeException.class, () -> postService.getPostById(999L));
    verify(postRepository, times(1)).findById(999L);
  }

  @Test
  void getPosts_ShouldTruncateTextInList() {
    // Given
    when(postRepository.findAllOrderByCreatedAtDesc()).thenReturn(Arrays.asList(testPost));

    // When
    PostListResponse response = postService.getPosts("", 1, 5);

    // Then
    assertNotNull(response);
    assertEquals(1, response.getPosts().size());
    PostDTO dto = response.getPosts().get(0);
    assertTrue(dto.getText().endsWith("...")); // Текст обрезан
    assertTrue(dto.getText().length() <= 131); // 128 + "..."
  }

  @Test
  void getPosts_WithTitleSearch_ShouldFilterPosts() {
    // Given
    Post post1 = createPost(1L, "Spring Framework", "Content 1", "#java#");
    Post post2 = createPost(2L, "Java Tutorial", "Content 2", "#java#");
    Post post3 = createPost(3L, "Python Guide", "Content 3", "#python#");

    when(postRepository.findAllWithTitleFilter("Spring"))
        .thenReturn(Arrays.asList(post1));

    // When
    PostListResponse response = postService.getPosts("Spring", 1, 10);

    // Then
    assertNotNull(response);
    assertEquals(1, response.getPosts().size());
    assertEquals("Spring Framework", response.getPosts().get(0).getTitle());
  }

  @Test
  void getPosts_WithTagSearch_ShouldFilterByTag() {
    // Given
    Post post1 = createPost(1L, "Post 1", "Content 1", "#java#spring#");
    Post post2 = createPost(2L, "Post 2", "Content 2", "#java#");
    Post post3 = createPost(3L, "Post 3", "Content 3", "#python#");

    when(postRepository.findAllOrderByCreatedAtDesc())
        .thenReturn(Arrays.asList(post1, post2, post3));

    // When
    PostListResponse response = postService.getPosts("#java", 1, 10);

    // Then
    assertNotNull(response);
    assertEquals(2, response.getPosts().size());
  }

  @Test
  void getPosts_Pagination_FirstPage() {
    // Given
    List<Post> allPosts = Arrays.asList(
        createPost(1L, "Post 1", "Content 1", ""),
        createPost(2L, "Post 2", "Content 2", ""),
        createPost(3L, "Post 3", "Content 3", ""),
        createPost(4L, "Post 4", "Content 4", ""),
        createPost(5L, "Post 5", "Content 5", "")
    );

    when(postRepository.findAllOrderByCreatedAtDesc()).thenReturn(allPosts);

    // When
    PostListResponse response = postService.getPosts("", 1, 2);

    // Then
    assertNotNull(response);
    assertEquals(2, response.getPosts().size());
    assertFalse(response.getHasPrev());
    assertTrue(response.getHasNext());
    assertEquals(3, response.getLastPage());
  }

  @Test
  void getPosts_Pagination_LastPage() {
    // Given
    List<Post> allPosts = Arrays.asList(
        createPost(1L, "Post 1", "Content 1", ""),
        createPost(2L, "Post 2", "Content 2", ""),
        createPost(3L, "Post 3", "Content 3", "")
    );

    when(postRepository.findAllOrderByCreatedAtDesc()).thenReturn(allPosts);

    // When
    PostListResponse response = postService.getPosts("", 2, 2);

    // Then
    assertNotNull(response);
    assertEquals(1, response.getPosts().size());
    assertTrue(response.getHasPrev());
    assertFalse(response.getHasNext());
    assertEquals(2, response.getLastPage());
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
    Optional<Post> result = postService.updatePost(1L, updatedData);

    // Then
    assertTrue(result.isPresent());
    assertEquals("Updated Title", result.get().getTitle());
    assertEquals("Updated Content", result.get().getText());
    assertEquals("#updated#", result.get().getTags());
    assertNotNull(result.get().getUpdatedAt());
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
  void deletePost_WhenNotExists_ShouldReturnFalse() {
    // Given
    when(postRepository.existsById(999L)).thenReturn(false);

    // When
    boolean result = postService.deletePost(999L);

    // Then
    assertFalse(result);
    verify(postRepository, never()).deleteById(anyLong());
  }

  @Test
  void incrementLikesAndGetCount_ShouldIncrementAndReturn() {
    // Given
    when(postRepository.existsById(1L)).thenReturn(true);
    doNothing().when(postRepository).incrementLikesCount(1L);
    when(postRepository.getLikesCount(1L)).thenReturn(6);

    // When
    Integer result = postService.incrementLikesAndGetCount(1L);

    // Then
    assertEquals(6, result);
    verify(postRepository, times(1)).incrementLikesCount(1L);
    verify(postRepository, times(1)).getLikesCount(1L);
  }

  @Test
  void incrementLikesAndGetCount_WhenPostNotExists_ShouldThrowException() {
    // Given
    when(postRepository.existsById(999L)).thenReturn(false);

    // When & Then
    assertThrows(RuntimeException.class, () -> postService.incrementLikesAndGetCount(999L));
  }

  // Helper method
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
