package com.blog.service;

import com.blog.model.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для поиска и фильтрации постов.
 * Отвечает за парсинг поискового запроса и фильтрацию результатов.
 */
@Slf4j
@Service
public class PostSearchService {

  /**
   * Распарсить строку поиска на теги и подстроку для поиска по названию
   */
  public SearchCriteria parseSearch(String search) {
    if (search == null || search.isBlank()) {
      return new SearchCriteria(null, List.of());
    }

    List<String> tags = new ArrayList<>();
    List<String> titleWords = new ArrayList<>();

    // Разбиваем на слова
    String[] words = search.trim().split("\\s+");

    for (String word : words) {
      if (word.isEmpty()) {
        continue;
      }

      if (word.startsWith("#")) {
        // Это тег
        String tag = word.substring(1).toLowerCase();
        if (!tag.isEmpty()) {
          tags.add(tag);
        }
      } else {
        // Это часть названия
        titleWords.add(word);
      }
    }

    String titleSearch = titleWords.isEmpty() ? null : String.join(" ", titleWords);

    log.debug("Parsed search: titleSearch='{}', tags={}", titleSearch, tags);
    return new SearchCriteria(titleSearch, tags);
  }

  /**
   * Фильтровать посты по тегам
   */
  public List<Post> filterByTags(List<Post> posts, List<String> tags) {
    if (tags.isEmpty()) {
      return posts;
    }

    return posts.stream()
        .filter(post -> postHasAllTags(post, tags))
        .collect(Collectors.toList());
  }

  /**
   * Проверить, содержит ли пост все указанные теги
   */
  private boolean postHasAllTags(Post post, List<String> requiredTags) {
    if (post.getTags() == null || post.getTags().isEmpty()) {
      return false;
    }

    List<String> postTags = parseTagsFromString(post.getTags());

    return postTags.containsAll(requiredTags);
  }

  /**
   * Распарсить теги из строки "#java#spring#" в список ["java", "spring"]
   */
  public List<String> parseTagsFromString(String tagsString) {
    if (tagsString == null || tagsString.isEmpty()) {
      return List.of();
    }

    return Arrays.stream(tagsString.split("#"))
        .map(String::trim)
        .filter(tag -> !tag.isEmpty())
        .map(String::toLowerCase)
        .collect(Collectors.toList());
  }

  /**
   * Преобразовать список тегов в строку для БД
   */
  public String formatTagsForDb(List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      return "";
    }

    return tags.stream()
        .map(String::toLowerCase)
        .map(tag -> "#" + tag)
        .collect(Collectors.joining()) + "#";
  }

  /**
   * Критерии поиска
   */
  public record SearchCriteria(String titleSearch, List<String> tags) {
  }
}
