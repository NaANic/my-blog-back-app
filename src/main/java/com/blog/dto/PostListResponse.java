package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {

  private List<PostDTO> posts;
  private Boolean hasPrev;
  private Boolean hasNext;
  private Integer lastPage;
}
