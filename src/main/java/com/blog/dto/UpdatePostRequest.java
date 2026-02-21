package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePostRequest {

  @NotNull(message = "ID is required")
  private Long id;

  @NotBlank(message = "Title is required")
  @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
  private String title;

  @NotBlank(message = "Text is required")
  @Size(min = 10, message = "Text must be at least 10 characters")
  private String text;

  @NotNull(message = "Tags cannot be null")
  private List<String> tags;
}
