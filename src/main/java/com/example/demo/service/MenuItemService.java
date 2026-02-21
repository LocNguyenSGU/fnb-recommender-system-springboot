package com.example.demo.service;

import com.example.demo.dto.request.MenuItemRequestDTO;
import com.example.demo.dto.response.MenuItemResponseDTO;

import java.util.List;
import java.util.Optional;

public interface MenuItemService {
    
    MenuItemResponseDTO createMenuItem(MenuItemRequestDTO menuItemRequestDTO);
    
    MenuItemResponseDTO updateMenuItem(Long id, MenuItemRequestDTO menuItemRequestDTO);
    
    void deleteMenuItem(Long id);
    
    Optional<MenuItemResponseDTO> getMenuItemById(Long id);
    
    List<MenuItemResponseDTO> getAllMenuItems();
    
    List<MenuItemResponseDTO> getMenuItemsByMenuId(Long menuId);
    
    List<MenuItemResponseDTO> getAvailableMenuItemsByMenuId(Long menuId);
    
    List<MenuItemResponseDTO> getHotMenuItems();
    
    List<MenuItemResponseDTO> getSignatureMenuItems();
    
    List<MenuItemResponseDTO> searchMenuItemsByName(String name);
    
    List<MenuItemResponseDTO> getTopViewedMenuItems();
    
    void incrementViewCount(Long id);
}
