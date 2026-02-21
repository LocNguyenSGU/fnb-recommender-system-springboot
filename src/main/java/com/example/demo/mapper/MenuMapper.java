package com.example.demo.mapper;

import com.example.demo.dto.request.MenuRequestDTO;
import com.example.demo.dto.response.MenuResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Menu;
import com.example.demo.model.Shop;
import com.example.demo.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuMapper {
    
    public MenuResponseDTO toResponseDTO(Menu entity) {
        if (entity == null) {
            return null;
        }
        
        MenuResponseDTO dto = new MenuResponseDTO();
        dto.setId(entity.getId());
        
        if (entity.getShop() != null) {
            dto.setShopId(entity.getShop().getId());
            dto.setShopName(entity.getShop().getName());
        }
        
        dto.setName(entity.getName());
        dto.setImages(entity.getImages());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }
    
    public Menu toEntity(MenuRequestDTO dto, ShopRepository shopRepository) {
        if (dto == null) {
            return null;
        }
        
        Menu entity = new Menu();
        
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + dto.getShopId()));
        entity.setShop(shop);
        
        entity.setName(dto.getName());
        entity.setImages(dto.getImages());
        
        return entity;
    }
    
    public void updateEntityFromDTO(Menu entity, MenuRequestDTO dto, ShopRepository shopRepository) {
        if (dto == null || entity == null) {
            return;
        }
        
        if (dto.getShopId() != null) {
            Shop shop = shopRepository.findById(dto.getShopId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + dto.getShopId()));
            entity.setShop(shop);
        }
        
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        
        if (dto.getImages() != null) {
            entity.setImages(dto.getImages());
        }
    }
}

