package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

  private String title;
  private String text;
  private List<String> tags;

  /**
   * Конвертация массива тегов в строку для БД
   * ["java", "spring"] → "#java#spring#"
   * @return строка тегов
   */
  public String getTagsString() {
    if (tags == null || tags.isEmpty()) {
      return "";
    }
    return "#" + String.join("#", tags) + "#";
  }
}
