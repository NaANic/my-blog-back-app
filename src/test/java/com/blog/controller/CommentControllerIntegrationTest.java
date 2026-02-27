package com.blog.controller;

import com.blog.BlogApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BlogApplication.class)
@AutoConfigureMockMvc
class CommentControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void getComments_ShouldReturnCommentsList() throws Exception {
    MvcResult result = mockMvc.perform(get("/posts/1/comments"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String content = result.getResponse().getContentAsString();
    assertTrue(content.startsWith("["));
    assertTrue(content.contains("text"));
    assertTrue(content.contains("postId"));
  }

  @Test
  void getComments_WithInvalidPostId_ShouldReturnEmptyList() throws Exception {
    MvcResult result = mockMvc.perform(get("/posts/undefined/comments"))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();
    var json = objectMapper.readTree(content);

    assertTrue(json.isArray());
    assertEquals(0, json.size()); // Пустой массив
  }

  @Test
  void createComment_WithValidData_ShouldReturnCreated() throws Exception {
    String commentJson = """
        {
          "text": "Test comment from integration test",
          "postId": 1
        }
        """;

    MvcResult result = mockMvc.perform(post("/posts/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(commentJson))
        .andExpect(status().isCreated())
        .andReturn();

    String content = result.getResponse().getContentAsString();
    var json = objectMapper.readTree(content);

    assertTrue(json.has("id"));
    assertEquals("Test comment from integration test", json.get("text").asText());
    assertEquals(1, json.get("postId").asInt());
    assertTrue(json.has("createdAt"));
  }

  @Test
  void updateComment_WhenExists_ShouldReturnUpdated() throws Exception {
    // Create comment first
    String createJson = """
        {
          "text": "Original comment",
          "postId": 1
        }
        """;

    MvcResult createResult = mockMvc.perform(post("/posts/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createJson))
        .andExpect(status().isCreated())
        .andReturn();

    String createResponse = createResult.getResponse().getContentAsString();
    Long commentId = objectMapper.readTree(createResponse).get("id").asLong();

    // Update comment
    String updateJson = """
        {
          "id": %d,
          "text": "Updated comment text",
          "postId": 1
        }
        """.formatted(commentId);

    MvcResult updateResult = mockMvc.perform(put("/posts/1/comments/" + commentId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateJson))
        .andExpect(status().isOk())
        .andReturn();

    String updateResponse = updateResult.getResponse().getContentAsString();
    assertTrue(updateResponse.contains("Updated comment text"));
  }

  @Test
  void deleteComment_WhenExists_ShouldReturn200() throws Exception {
    // Create comment first
    String commentJson = """
        {
          "text": "Comment to delete",
          "postId": 1
        }
        """;

    MvcResult createResult = mockMvc.perform(post("/posts/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(commentJson))
        .andExpect(status().isCreated())
        .andReturn();

    String response = createResult.getResponse().getContentAsString();
    Long commentId = objectMapper.readTree(response).get("id").asLong();

    // Delete comment
    mockMvc.perform(delete("/posts/1/comments/" + commentId))
        .andExpect(status().isOk());
  }

  @Test
  void deleteComment_WhenNotExists_ShouldReturn404() throws Exception {
    mockMvc.perform(delete("/posts/1/comments/999"))
        .andExpect(status().isNotFound());
  }
}
