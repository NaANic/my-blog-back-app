package com.blog.repository;

import com.blog.model.Post;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {

  /**
   * Найти все посты с фильтром по title (без пагинации, для фильтрации по тегам)
   */
  @Query("SELECT * FROM posts WHERE " +
      "(:titleSearch IS NULL OR :titleSearch = '' OR LOWER(title) LIKE LOWER(CONCAT('%', :titleSearch, '%'))) " +
      "ORDER BY created_at DESC")
  List<Post> findAllWithTitleFilter(@Param("titleSearch") String titleSearch);

  /**
   * Поиск постов с фильтрацией и пагинацией
   */
  @Query("SELECT * FROM posts WHERE " +
      "(:titleSearch IS NULL OR :titleSearch = '' OR LOWER(title) LIKE LOWER(CONCAT('%', :titleSearch, '%'))) " +
      "ORDER BY created_at DESC " +
      "LIMIT :limit OFFSET :offset")
  List<Post> findWithTitleFilter(
      @Param("titleSearch") String titleSearch,
      @Param("limit") int limit,
      @Param("offset") int offset
  );

  /**
   * Подсчёт общего количества постов с фильтрацией
   */
  @Query("SELECT COUNT(*) FROM posts WHERE " +
      "(:titleSearch IS NULL OR :titleSearch = '' OR LOWER(title) LIKE LOWER(CONCAT('%', :titleSearch, '%')))")
  long countWithTitleFilter(@Param("titleSearch") String titleSearch);

  /**
   * Поиск всех постов без фильтра с пагинацией
   */
  @Query("SELECT * FROM posts ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
  List<Post> findAllWithPagination(@Param("limit") int limit, @Param("offset") int offset);

  /**
   * Общее количество постов
   */
  @Query("SELECT COUNT(*) FROM posts")
  long countAllPosts();

  /**
   * Найти все посты (с сортировкой по дате)
   */
  @Query("SELECT * FROM posts ORDER BY created_at DESC")
  List<Post> findAllOrderByCreatedAtDesc();

  /**
   * Увеличить счётчик лайков
   */
  @Modifying
  @Query("UPDATE posts SET likes_count = likes_count + 1 WHERE id = :id")
  void incrementLikesCount(@Param("id") Long id);

  /**
   * Получить текущее количество лайков
   */
  @Query("SELECT likes_count FROM posts WHERE id = :id")
  Integer getLikesCount(@Param("id") Long id);

  /**
   * Увеличить счётчик комментариев
   */
  @Modifying
  @Query("UPDATE posts SET comments_count = comments_count + 1 WHERE id = :id")
  void incrementCommentsCount(@Param("id") Long id);

  /**
   * Уменьшить счётчик комментариев
   */
  @Modifying
  @Query("UPDATE posts SET comments_count = comments_count - 1 WHERE id = :id AND comments_count > 0")
  void decrementCommentsCount(@Param("id") Long id);

  /**
   * Поиск по title или тексту (для простого поиска)
   */
  @Query("SELECT * FROM posts WHERE " +
      "LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
      "LOWER(text) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "ORDER BY created_at DESC")
  List<Post> searchByTitleOrText(@Param("keyword") String keyword);
}
