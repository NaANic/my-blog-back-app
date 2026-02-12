package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {

  private Long id;
  private String title;
  private String text;
  private List<String> tags;

  /**
   * Конвертация массива тегов в строку для БД
   * @return строка тегов
   */
  public String getTagsString() {
    if (tags == null || tags.isEmpty()) {
      return "";
    }
    return "#" + String.join("#", tags) + "#";
  }
}
