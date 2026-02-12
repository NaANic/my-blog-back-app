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
@Table("COMMENTS")
public class Comment {

  @Id
  @Column("ID")
  private Long id;

  @Column("POST_ID")
  private Long postId;

  @Column("TEXT")
  private String text;

  @Column("CREATED_AT")
  private LocalDateTime createdAt;
}
