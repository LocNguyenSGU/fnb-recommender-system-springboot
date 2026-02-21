package com.example.demo.mapper;

import com.example.demo.dto.request.MenuItemRequestDTO;
import com.example.demo.dto.response.MenuItemResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Menu;
import com.example.demo.model.MenuItem;
import com.example.demo.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuItemMapper {
    
    public MenuItemResponseDTO toResponseDTO(MenuItem entity) {
        if (entity == null) {
            return null;
        }
        
        MenuItemResponseDTO dto = new MenuItemResponseDTO();
        dto.setId(entity.getId());
        
        if (entity.getMenu() != null) {
            dto.setMenuId(entity.getMenu().getId());
            dto.setMenuName(entity.getMenu().getName());
        }
        
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setImages(entity.getImages());
        dto.setIsAvailable(entity.getIsAvailable());
        dto.setIsHot(entity.getIsHot());
        dto.setIsSignature(entity.getIsSignature());
        dto.setViewCount(entity.getViewCount());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }
    
    public MenuItem toEntity(MenuItemRequestDTO dto, MenuRepository menuRepository) {
        if (dto == null) {
            return null;
        }
        
        MenuItem entity = new MenuItem();
        
        Menu menu = menuRepository.findById(dto.getMenuId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with id: " + dto.getMenuId()));
        entity.setMenu(menu);
        
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setImages(dto.getImages());
        entity.setIsAvailable(dto.getIsAvailable());
        entity.setIsHot(dto.getIsHot());
        entity.setIsSignature(dto.getIsSignature());
        
        return entity;
    }
    
    public void updateEntityFromDTO(MenuItem entity, MenuItemRequestDTO dto, MenuRepository menuRepository) {
        if (dto == null || entity == null) {
            return;
        }
        
        if (dto.getMenuId() != null) {
            Menu menu = menuRepository.findById(dto.getMenuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu not found with id: " + dto.getMenuId()));
            entity.setMenu(menu);
        }
        
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        
        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice());
        }
        
        if (dto.getImages() != null) {
            entity.setImages(dto.getImages());
        }
        
        if (dto.getIsAvailable() != null) {
            entity.setIsAvailable(dto.getIsAvailable());
        }
        
        if (dto.getIsHot() != null) {
            entity.setIsHot(dto.getIsHot());
        }
        
        if (dto.getIsSignature() != null) {
            entity.setIsSignature(dto.getIsSignature());
        }
    }
}
