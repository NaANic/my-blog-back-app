package com.blog.service;

import com.blog.dto.PostDTO;
import com.blog.dto.PostListResponse;
import com.blog.model.Post;
import com.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для поиска и фильтрации постов.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostSearchService {

  private final PostRepository postRepository;

  /**
   * Поиск постов с фильтрацией и пагинацией
   *
   * @param search строка поиска (может содержать теги и текст)
   * @param pageNumber номер страницы (начиная с 1)
   * @param pageSize размер страницы
   * @return результаты поиска с пагинацией
   */
  public PostListResponse searchPosts(String search, Integer pageNumber, Integer pageSize) {
    log.debug("searchPosts: search='{}', page={}, size={}", search, pageNumber, pageSize);

    // Парсинг поискового запроса
    SearchCriteria criteria = parseSearchString(search);
    log.debug("Parsed search: titleSearch='{}', tags={}", criteria.titleSearch, criteria.tags);

    // Получение постов из БД
    List<Post> allPosts = fetchPosts(criteria);
    log.debug("Found {} posts after filtering", allPosts.size());

    // Фильтрация по тегам
    List<Post> filteredPosts = filterByTags(allPosts, criteria.tags);
    log.debug("Found {} posts after tag filter", filteredPosts.size());

    // Пагинация
    return paginatePosts(filteredPosts, pageNumber, pageSize);
  }

  /**
   * Парсинг строки поиска на теги и текст
   */
  private SearchCriteria parseSearchString(String search) {
    if (search == null || search.isBlank()) {
      return new SearchCriteria(null, List.of());
    }

    List<String> tags = new ArrayList<>();
    List<String> titleWords = new ArrayList<>();

    String[] words = search.trim().split("\\s+");
    for (String word : words) {
      if (word.startsWith("#")) {
        tags.add(word.substring(1).toLowerCase());
      } else if (!word.isBlank()) {
        titleWords.add(word);
      }
    }

    String titleSearch = titleWords.isEmpty() ? null : String.join(" ", titleWords);
    return new SearchCriteria(titleSearch, tags);
  }

  /**
   * Получение постов из БД с учётом поиска по названию
   */
  private List<Post> fetchPosts(SearchCriteria criteria) {
    if (criteria.titleSearch != null && !criteria.titleSearch.isBlank()) {
      return postRepository.findAllWithTitleFilter(criteria.titleSearch);
    } else {
      List<Post> posts = new ArrayList<>();
      postRepository.findAllOrderByCreatedAtDesc().forEach(posts::add);
      return posts;
    }
  }

  /**
   * Фильтрация постов по тегам (логика "И")
   */
  private List<Post> filterByTags(List<Post> posts, List<String> tags) {
    if (tags.isEmpty()) {
      return posts;
    }

    return posts.stream()
        .filter(post -> {
          String postTags = post.getTags() != null ? post.getTags().toLowerCase() : "";
          return tags.stream().allMatch(tag -> postTags.contains("#" + tag + "#"));
        })
        .collect(Collectors.toList());
  }

  /**
   * Пагинация результатов
   */
  private PostListResponse paginatePosts(List<Post> posts, Integer pageNumber, Integer pageSize) {
    int totalPosts = posts.size();
    int lastPage = (int) Math.ceil((double) totalPosts / pageSize);

    int startIndex = (pageNumber - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, totalPosts);

    List<Post> pagePosts = posts.subList(startIndex, endIndex);
    List<PostDTO> postDTOs = pagePosts.stream()
        .map(post -> PostDTO.fromEntity(post, true)) // truncate = true для списка
        .collect(Collectors.toList());

    return PostListResponse.builder()
        .posts(postDTOs)
        .hasPrev(pageNumber > 1)
        .hasNext(pageNumber < lastPage)
        .lastPage(lastPage)
        .build();
  }

  /**
   * Внутренний класс для хранения критериев поиска
   */
  private record SearchCriteria(String titleSearch, List<String> tags) {}
}
