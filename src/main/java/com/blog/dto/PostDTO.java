package com.blog.dto;

import com.blog.model.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {

  private Long id;
  private String title;
  private String text;
  private List<String> tags;
  private Integer likesCount;
  private Integer commentsCount;

  /**
   * Конвертация из Entity в DTO
   * @param post - сущность из БД
   * @param truncateText - обрезать ли текст до 128 символов
   * @return PostDTO
   */
  public static PostDTO fromEntity(Post post, boolean truncateText) {
    PostDTO dto = new PostDTO();
    dto.setId(post.getId());
    dto.setTitle(post.getTitle());

    // Обрезка текста для списка постов
    if (truncateText && post.getText() != null && post.getText().length() > 128) {
      dto.setText(post.getText().substring(0, 128) + "...");
    } else {
      dto.setText(post.getText());
    }

    // Парсинг тегов: "#java#spring#" → ["java", "spring"]
    dto.setTags(parseTags(post.getTags()));
    dto.setLikesCount(post.getLikesCount());
    dto.setCommentsCount(post.getCommentsCount());

    return dto;
  }

  /**
   * Парсинг тегов из строки в список
   * @param tagsString - строка вида "#java#spring#"
   * @return список тегов ["java", "spring"]
   */
  private static List<String> parseTags(String tagsString) {
    if (tagsString == null || tagsString.isEmpty()) {
      return new ArrayList<>();
    }

    return Arrays.stream(tagsString.split("#"))
        .filter(tag -> !tag.isEmpty())
        .collect(Collectors.toList());
  }
}
