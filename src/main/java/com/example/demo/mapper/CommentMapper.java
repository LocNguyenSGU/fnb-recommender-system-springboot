package com.example.demo.mapper;

import com.example.demo.dto.request.CommentRequestDTO;
import com.example.demo.dto.response.CommentResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Blog;
import com.example.demo.model.Comment;
import com.example.demo.model.User;
import com.example.demo.repository.BlogRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper {
    
    public CommentResponseDTO toResponseDTO(Comment entity) {
        if (entity == null) {
            return null;
        }
        
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(entity.getId());
        
        if (entity.getBlog() != null) {
            dto.setBlogId(entity.getBlog().getId());
        }
        
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserName(entity.getUser().getFullName());
        }
        
        dto.setContent(entity.getContent());
        dto.setReplies(entity.getReplies());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }
    
    public Comment toEntity(CommentRequestDTO dto, BlogRepository blogRepository, UserRepository userRepository) {
        if (dto == null) {
            return null;
        }
        
        Comment entity = new Comment();
        
        Blog blog = blogRepository.findById(dto.getBlogId())
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + dto.getBlogId()));
        entity.setBlog(blog);
        
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
        entity.setUser(user);
        
        entity.setContent(dto.getContent());
        
        return entity;
    }
    
    public void updateEntityFromDTO(Comment entity, CommentRequestDTO dto, BlogRepository blogRepository, UserRepository userRepository) {
        if (dto == null || entity == null) {
            return;
        }
        
        if (dto.getBlogId() != null) {
            Blog blog = blogRepository.findById(dto.getBlogId())
                    .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + dto.getBlogId()));
            entity.setBlog(blog);
        }
        
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
            entity.setUser(user);
        }
        
        if (dto.getContent() != null) {
            entity.setContent(dto.getContent());
        }
    }
}
