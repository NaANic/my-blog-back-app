package com.blog.controller;

import com.blog.config.RootConfig;
import com.blog.config.WebConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(classes = {RootConfig.class, WebConfig.class})
@WebAppConfiguration
class PostControllerIntegrationTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private ObjectMapper objectMapper;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  void getPosts_ShouldReturnPostList() throws Exception {
    MvcResult result = mockMvc.perform(get("/posts")
            .param("search", "")
            .param("pageNumber", "1")
            .param("pageSize", "5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String content = result.getResponse().getContentAsString();
    assertNotNull(content);
    assertTrue(content.contains("posts"));
    assertTrue(content.contains("hasPrev"));
    assertTrue(content.contains("hasNext"));
    assertTrue(content.contains("lastPage"));
  }

  @Test
  void getPosts_WithSearch_ShouldFilterResults() throws Exception {
    MvcResult result = mockMvc.perform(get("/posts")
            .param("search", "Spring")
            .param("pageNumber", "1")
            .param("pageSize", "10"))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();
    assertTrue(content.toLowerCase().contains("spring"));
  }

  @Test
  void getPosts_WithTagSearch_ShouldFilterByTag() throws Exception {
    MvcResult result = mockMvc.perform(get("/posts")
            .param("search", "#java")
            .param("pageNumber", "1")
            .param("pageSize", "10"))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();
    assertTrue(content.contains("java"));
  }

  @Test
  void getPosts_Pagination_ShouldWorkCorrectly() throws Exception {
    // First page
    MvcResult result1 = mockMvc.perform(get("/posts")
            .param("search", "")
            .param("pageNumber", "1")
            .param("pageSize", "2"))
        .andExpect(status().isOk())
        .andReturn();

    String content1 = result1.getResponse().getContentAsString();
    var json1 = objectMapper.readTree(content1);

    assertFalse(json1.get("hasPrev").asBoolean());
    assertTrue(json1.get("hasNext").asBoolean());

    // Second page
    MvcResult result2 = mockMvc.perform(get("/posts")
            .param("search", "")
            .param("pageNumber", "2")
            .param("pageSize", "2"))
        .andExpect(status().isOk())
        .andReturn();

    String content2 = result2.getResponse().getContentAsString();
    var json2 = objectMapper.readTree(content2);

    assertTrue(json2.get("hasPrev").asBoolean());
  }

  @Test
  void getPost_WhenExists_ShouldReturnPost() throws Exception {
    MvcResult result = mockMvc.perform(get("/posts/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String content = result.getResponse().getContentAsString();
    assertNotNull(content);
    assertFalse(content.isEmpty());

    // Парсим JSON и проверяем поля
    var json = objectMapper.readTree(content);
    assertEquals(1, json.get("id").asInt());
    assertNotNull(json.get("title"));
    assertNotNull(json.get("text"));
    assertNotNull(json.get("tags"));
  }

  @Test
  void getPost_WhenNotExists_ShouldReturn404() throws Exception {
    mockMvc.perform(get("/posts/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getPost_WithInvalidId_ShouldReturn400() throws Exception {
    mockMvc.perform(get("/posts/invalid"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createPost_WithValidData_ShouldReturnCreated() throws Exception {
    String postJson = """
      {
        "title": "Test Post",
        "text": "Test content for integration test",
        "tags": ["test", "integration"]
      }
      """;

    MvcResult result = mockMvc.perform(post("/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(postJson))
        .andExpect(status().isCreated())
        .andReturn();

    String content = result.getResponse().getContentAsString();
    var json = objectMapper.readTree(content);

    assertTrue(json.has("id"));
    assertEquals("Test Post", json.get("title").asText());
    assertEquals("Test content for integration test", json.get("text").asText());
    assertEquals(0, json.get("likesCount").asInt());
    assertEquals(0, json.get("commentsCount").asInt());

    // Проверяем теги
    var tagsArray = json.get("tags");
    assertTrue(tagsArray.isArray());
    assertEquals(2, tagsArray.size());
  }

  @Test
  void updatePost_WhenExists_ShouldReturnUpdated() throws Exception {
    String updateJson = """
        {
          "id": 1,
          "title": "Updated Title",
          "text": "Updated content",
          "tags": ["updated"]
        }
        """;

    MvcResult result = mockMvc.perform(put("/posts/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateJson))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();
    assertTrue(content.contains("Updated Title"));
    assertTrue(content.contains("Updated content"));
  }

  @Test
  void deletePost_WhenExists_ShouldReturn200() throws Exception {
    // Create a post first
    String postJson = """
        {
          "title": "To Delete",
          "text": "This will be deleted",
          "tags": []
        }
        """;

    MvcResult createResult = mockMvc.perform(post("/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(postJson))
        .andExpect(status().isCreated())
        .andReturn();

    String response = createResult.getResponse().getContentAsString();
    Long postId = objectMapper.readTree(response).get("id").asLong();

    // Delete the post
    mockMvc.perform(delete("/posts/" + postId))
        .andExpect(status().isOk());

    // Verify it's deleted
    mockMvc.perform(get("/posts/" + postId))
        .andExpect(status().isNotFound());
  }

  @Test
  void incrementLikes_ShouldReturnNewCount() throws Exception {
    MvcResult result = mockMvc.perform(post("/posts/1/likes"))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();
    assertNotNull(content);
    int likesCount = Integer.parseInt(content);
    assertTrue(likesCount > 0);
  }

  @Test
  void getImage_WhenPostExists_ShouldReturnImage() throws Exception {
    MvcResult result = mockMvc.perform(get("/posts/1/image"))
        .andExpect(status().isOk())
        .andReturn();

    String contentType = result.getResponse().getContentType();
    assertNotNull(contentType);
    assertTrue(contentType.startsWith("image/"));
  }

  @Test
  void getImage_WhenPostNotExists_ShouldReturn404() throws Exception {
    mockMvc.perform(get("/posts/999/image"))
        .andExpect(status().isNotFound());
  }
}
