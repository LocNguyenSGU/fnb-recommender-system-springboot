package com.example.demo.mapper;

import com.example.demo.dto.request.ReviewRequestDTO;
import com.example.demo.dto.response.ReviewResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Review;
import com.example.demo.model.Shop;
import com.example.demo.model.User;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    
    public ReviewResponseDTO toResponseDTO(Review entity) {
        if (entity == null) {
            return null;
        }
        
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(entity.getId());
        
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserName(entity.getUser().getFullName());
        }
        
        if (entity.getShop() != null) {
            dto.setShopId(entity.getShop().getId());
            dto.setShopName(entity.getShop().getName());
        }
        
        dto.setRating(entity.getRating());
        dto.setContent(entity.getContent());
        dto.setReplies(entity.getReplies());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }
    
    public Review toEntity(ReviewRequestDTO dto, UserRepository userRepository, ShopRepository shopRepository) {
        if (dto == null) {
            return null;
        }
        
        Review entity = new Review();
        
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
        entity.setUser(user);
        
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + dto.getShopId()));
        entity.setShop(shop);
        
        entity.setRating(dto.getRating());
        entity.setContent(dto.getContent());
        
        return entity;
    }
    
    public void updateEntityFromDTO(Review entity, ReviewRequestDTO dto, UserRepository userRepository, ShopRepository shopRepository) {
        if (dto == null || entity == null) {
            return;
        }
        
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
            entity.setUser(user);
        }
        
        if (dto.getShopId() != null) {
            Shop shop = shopRepository.findById(dto.getShopId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + dto.getShopId()));
            entity.setShop(shop);
        }
        
        if (dto.getRating() != null) {
            entity.setRating(dto.getRating());
        }
        
        if (dto.getContent() != null) {
            entity.setContent(dto.getContent());
        }
    }
}
