package com.example.demo.mapper;

import com.example.demo.dto.request.BlogRequestDTO;
import com.example.demo.dto.response.BlogResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Blog;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlogMapper {
    
    public BlogResponseDTO toResponseDTO(Blog entity) {
        if (entity == null) {
            return null;
        }
        
        BlogResponseDTO dto = new BlogResponseDTO();
        dto.setId(entity.getId());
        
        if (entity.getAuthor() != null) {
            dto.setAuthorId(entity.getAuthor().getId());
            dto.setAuthorName(entity.getAuthor().getFullName());
        }
        
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setImages(entity.getImages());
        dto.setLikesCount(entity.getLikesCount());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }
    
    public Blog toEntity(BlogRequestDTO dto, UserRepository userRepository) {
        if (dto == null) {
            return null;
        }
        
        Blog entity = new Blog();
        
        User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getAuthorId()));
        entity.setAuthor(author);
        
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setImages(dto.getImages());
        entity.setStatus(dto.getStatus());
        
        return entity;
    }
    
    public void updateEntityFromDTO(Blog entity, BlogRequestDTO dto, UserRepository userRepository) {
        if (dto == null || entity == null) {
            return;
        }
        
        if (dto.getAuthorId() != null) {
            User author = userRepository.findById(dto.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getAuthorId()));
            entity.setAuthor(author);
        }
        
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        
        if (dto.getContent() != null) {
            entity.setContent(dto.getContent());
        }
        
        if (dto.getImages() != null) {
            entity.setImages(dto.getImages());
        }
        
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }
}

