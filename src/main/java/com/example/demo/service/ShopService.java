package com.example.demo.service;

import com.example.demo.dto.request.ShopRequestDTO;
import com.example.demo.dto.response.ShopResponseDTO;

import java.util.List;
import java.util.Optional;

public interface ShopService {
    
    ShopResponseDTO createShop(ShopRequestDTO shopRequestDTO);
    
    ShopResponseDTO updateShop(Long id, ShopRequestDTO shopRequestDTO);
    
    void deleteShop(Long id);
    
    Optional<ShopResponseDTO> getShopById(Long id);
    
    List<ShopResponseDTO> getAllShops();
    
    List<ShopResponseDTO> getShopsByOwnerId(Long ownerId);
    
    List<ShopResponseDTO> getShopsByCategoryId(Long categoryId);
    
    List<ShopResponseDTO> getShopsByStatus(String status);
    
    List<ShopResponseDTO> searchShopsByName(String name);
    
    List<ShopResponseDTO> findShopsWithinRadius(Double latitude, Double longitude, Double radius);
}
