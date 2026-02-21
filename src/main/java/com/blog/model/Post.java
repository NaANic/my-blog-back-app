package com.blog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("POSTS")
public class Post {

  @Id
  @Column("ID")
  private Long id;

  @Column("TITLE")
  private String title;

  @Column("TEXT")
  private String text;

  @Column("TAGS")
  private String tags;

  @Column("LIKES_COUNT")
  private Integer likesCount = 0;

  @Column("COMMENTS_COUNT")
  private Integer commentsCount = 0;

  @Column("CREATED_AT")
  private LocalDateTime createdAt;

  @Column("UPDATED_AT")
  private LocalDateTime updatedAt;
}
