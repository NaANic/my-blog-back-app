package com.blog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("posts")
public class Post {

  @Id
  private Long id;

  private String title;

  private String text;

  private String tags;

  private Integer likesCount = 0;

  private Integer commentsCount = 0;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}
