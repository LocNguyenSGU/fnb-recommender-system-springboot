package com.example.demo.service;

import com.example.demo.dto.request.BlogRequestDTO;
import com.example.demo.dto.response.BlogResponseDTO;

import java.util.List;
import java.util.Optional;

public interface BlogService {
    
    BlogResponseDTO createBlog(BlogRequestDTO blogRequestDTO);
    
    BlogResponseDTO updateBlog(Long id, BlogRequestDTO blogRequestDTO);
    
    void deleteBlog(Long id);
    
    Optional<BlogResponseDTO> getBlogById(Long id);
    
    List<BlogResponseDTO> getAllBlogs();
    
    List<BlogResponseDTO> getBlogsByAuthorId(Long authorId);
    
    List<BlogResponseDTO> getBlogsByStatus(String status);
    
    List<BlogResponseDTO> searchBlogsByTitle(String title);
    
    List<BlogResponseDTO> getTopLikedBlogs();
    
    void incrementLikesCount(Long id);
}
