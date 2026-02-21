package com.example.demo.repository;

import com.example.demo.model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    
    List<Blog> findByAuthorId(Long authorId);
    
    List<Blog> findByStatusOrderByCreatedAtDesc(String status);
    
    List<Blog> findByTitleContainingIgnoreCase(String title);
    
    @Query("SELECT b FROM Blog b ORDER BY b.likesCount DESC")
    List<Blog> findTopLikedBlogs();
}
