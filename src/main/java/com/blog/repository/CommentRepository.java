package com.blog.repository;

import com.blog.model.Comment;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {

  @Query("SELECT * FROM comments WHERE post_id = :postId ORDER BY created_at DESC")
  List<Comment> findByPostIdOrderByCreatedAtDesc(@Param("postId") Long postId);

  @Query("SELECT COUNT(*) FROM comments WHERE post_id = :postId")
  int countByPostId(@Param("postId") Long postId);
}
