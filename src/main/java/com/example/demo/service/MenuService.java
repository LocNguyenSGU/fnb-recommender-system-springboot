package com.example.demo.service;

import com.example.demo.dto.request.MenuRequestDTO;
import com.example.demo.dto.response.MenuResponseDTO;

import java.util.List;
import java.util.Optional;

public interface MenuService {
    
    MenuResponseDTO createMenu(MenuRequestDTO menuRequestDTO);
    
    MenuResponseDTO updateMenu(Long id, MenuRequestDTO menuRequestDTO);
    
    void deleteMenu(Long id);
    
    Optional<MenuResponseDTO> getMenuById(Long id);
    
    List<MenuResponseDTO> getAllMenus();
    
    List<MenuResponseDTO> getMenusByShopId(Long shopId);
    
    List<MenuResponseDTO> searchMenusByName(String name);
}
