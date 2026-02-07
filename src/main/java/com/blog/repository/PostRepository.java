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

  @Query("SELECT * FROM posts ORDER BY created_at DESC")
  List<Post> findAllOrderByCreatedAtDesc();

  @Query("SELECT * FROM posts WHERE title LIKE CONCAT('%', :keyword, '%') OR text LIKE CONCAT('%', :keyword, '%')")
  List<Post> searchByTitleOrText(@Param("keyword") String keyword);

  @Modifying
  @Query("UPDATE posts SET likes_count = likes_count + 1 WHERE id = :id")
  void incrementLikesCount(@Param("id") Long id);

  @Modifying
  @Query("UPDATE posts SET comments_count = comments_count + 1 WHERE id = :id")
  void incrementCommentsCount(@Param("id") Long id);

  @Modifying
  @Query("UPDATE posts SET comments_count = comments_count - 1 WHERE id = :id AND comments_count > 0")
  void decrementCommentsCount(@Param("id") Long id);
}
