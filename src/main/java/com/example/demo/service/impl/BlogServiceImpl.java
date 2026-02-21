package com.example.demo.service.impl;

import com.example.demo.dto.request.BlogRequestDTO;
import com.example.demo.dto.response.BlogResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.BlogMapper;
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
    private final BlogMapper blogMapper;
    
    @Override
    public BlogResponseDTO createBlog(BlogRequestDTO blogRequestDTO) {
        Blog blog = blogMapper.toEntity(blogRequestDTO, userRepository);
        Blog savedBlog = blogRepository.save(blog);
        return blogMapper.toResponseDTO(savedBlog);
    }
    
    @Override
    public BlogResponseDTO updateBlog(Long id, BlogRequestDTO blogRequestDTO) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id));
        
        blogMapper.updateEntityFromDTO(blog, blogRequestDTO, userRepository);
        Blog updatedBlog = blogRepository.save(blog);
        return blogMapper.toResponseDTO(updatedBlog);
    }
    
    @Override
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BlogResponseDTO> getBlogById(Long id) {
        return blogRepository.findById(id)
                .map(blogMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getAllBlogs() {
        return blogRepository.findAll().stream()
                .map(blogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getBlogsByAuthorId(Long authorId) {
        return blogRepository.findByAuthorId(authorId).stream()
                .map(blogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getBlogsByStatus(String status) {
        return blogRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(blogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> searchBlogsByTitle(String title) {
        return blogRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(blogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getTopLikedBlogs() {
        return blogRepository.findTopLikedBlogs().stream()
                .map(blogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public void incrementLikesCount(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id));
        blog.setLikesCount(blog.getLikesCount() + 1);
        blogRepository.save(blog);
    }
}
