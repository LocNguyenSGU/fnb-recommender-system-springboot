package com.example.demo.mapper;

import com.example.demo.dto.request.ShopRequestDTO;
import com.example.demo.dto.response.ShopResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Category;
import com.example.demo.model.Shop;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopMapper {
    
    public ShopResponseDTO toResponseDTO(Shop entity) {
        if (entity == null) {
            return null;
        }
        
        ShopResponseDTO dto = new ShopResponseDTO();
        dto.setId(entity.getId());
        
        if (entity.getOwner() != null) {
            dto.setOwnerId(entity.getOwner().getId());
            dto.setOwnerName(entity.getOwner().getFullName());
        }
        
        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
            dto.setCategoryName(entity.getCategory().getName());
        }
        
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setOpenTime(entity.getOpenTime());
        dto.setCloseTime(entity.getCloseTime());
        dto.setStatus(entity.getStatus());
        dto.setImages(entity.getImages());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }
    
    public Shop toEntity(ShopRequestDTO dto, UserRepository userRepository, CategoryRepository categoryRepository) {
        if (dto == null) {
            return null;
        }
        
        Shop entity = new Shop();
        
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getOwnerId()));
        entity.setOwner(owner);
        
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
        entity.setCategory(category);
        
        entity.setName(dto.getName());
        entity.setAddress(dto.getAddress());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setOpenTime(dto.getOpenTime());
        entity.setCloseTime(dto.getCloseTime());
        entity.setStatus(dto.getStatus());
        entity.setImages(dto.getImages());
        
        return entity;
    }
    
    public void updateEntityFromDTO(Shop entity, ShopRequestDTO dto, UserRepository userRepository, CategoryRepository categoryRepository) {
        if (dto == null || entity == null) {
            return;
        }
        
        if (dto.getOwnerId() != null) {
            User owner = userRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getOwnerId()));
            entity.setOwner(owner);
        }
        
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            entity.setCategory(category);
        }
        
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        
        if (dto.getAddress() != null) {
            entity.setAddress(dto.getAddress());
        }
        
        if (dto.getLatitude() != null) {
            entity.setLatitude(dto.getLatitude());
        }
        
        if (dto.getLongitude() != null) {
            entity.setLongitude(dto.getLongitude());
        }
        
        if (dto.getOpenTime() != null) {
            entity.setOpenTime(dto.getOpenTime());
        }
        
        if (dto.getCloseTime() != null) {
            entity.setCloseTime(dto.getCloseTime());
        }
        
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        
        if (dto.getImages() != null) {
            entity.setImages(dto.getImages());
        }
    }
}
