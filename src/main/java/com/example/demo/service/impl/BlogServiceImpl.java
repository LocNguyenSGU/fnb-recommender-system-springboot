package com.example.demo.service.impl;

import com.example.demo.dto.request.BlogRequestDTO;
import com.example.demo.dto.response.BlogResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Blog;
import com.example.demo.model.User;
import com.example.demo.repository.BlogRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogServiceImpl implements BlogService {
    
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    
    @Override
    public BlogResponseDTO createBlog(BlogRequestDTO blogRequestDTO) {
        Blog blog = mapToEntity(blogRequestDTO);
        Blog savedBlog = blogRepository.save(blog);
        return mapToResponseDTO(savedBlog);
    }
    
    @Override
    public BlogResponseDTO updateBlog(Long id, BlogRequestDTO blogRequestDTO) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));
        
        updateEntityFromDTO(blog, blogRequestDTO);
        Blog updatedBlog = blogRepository.save(blog);
        return mapToResponseDTO(updatedBlog);
    }
    
    @Override
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BlogResponseDTO> getBlogById(Long id) {
        return blogRepository.findById(id)
                .map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getAllBlogs() {
        return blogRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getBlogsByAuthorId(Long authorId) {
        return blogRepository.findByAuthorId(authorId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getBlogsByStatus(String status) {
        return blogRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> searchBlogsByTitle(String title) {
        return blogRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getTopLikedBlogs() {
        return blogRepository.findTopLikedBlogs().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public void incrementLikesCount(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));
        blog.setLikesCount(blog.getLikesCount() + 1);
        blogRepository.save(blog);
    }
    
    // Mapper methods
    private Blog mapToEntity(BlogRequestDTO dto) {
        Blog blog = new Blog();
        
        User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + dto.getAuthorId()));
        blog.setAuthor(author);
        
        blog.setTitle(dto.getTitle());
        blog.setContent(dto.getContent());
        blog.setImages(dto.getImages());
        blog.setStatus(dto.getStatus() != null ? dto.getStatus() : "pending");
        
        return blog;
    }
    
    private void updateEntityFromDTO(Blog blog, BlogRequestDTO dto) {
        if (dto.getAuthorId() != null) {
            User author = userRepository.findById(dto.getAuthorId())
                    .orElseThrow(() -> new RuntimeException("Author not found with id: " + dto.getAuthorId()));
            blog.setAuthor(author);
        }
        
        blog.setTitle(dto.getTitle());
        blog.setContent(dto.getContent());
        blog.setImages(dto.getImages());
        if (dto.getStatus() != null) {
            blog.setStatus(dto.getStatus());
        }
    }
    
    private BlogResponseDTO mapToResponseDTO(Blog blog) {
        BlogResponseDTO dto = new BlogResponseDTO();
        dto.setId(blog.getId());
        
        if (blog.getAuthor() != null) {
            dto.setAuthorId(blog.getAuthor().getId());
            dto.setAuthorName(blog.getAuthor().getFullName());
        }
        
        dto.setTitle(blog.getTitle());
        dto.setContent(blog.getContent());
        dto.setImages(blog.getImages());
        dto.setLikesCount(blog.getLikesCount());
        dto.setStatus(blog.getStatus());
        dto.setCreatedAt(blog.getCreatedAt());
        dto.setUpdatedAt(blog.getUpdatedAt());
        
        return dto;
    }
}
