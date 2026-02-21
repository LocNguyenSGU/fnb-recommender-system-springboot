package com.example.demo.service.impl;

import com.example.demo.dto.request.MenuItemRequestDTO;
import com.example.demo.dto.response.MenuItemResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Menu;
import com.example.demo.model.MenuItem;
import com.example.demo.repository.MenuItemRepository;
import com.example.demo.repository.MenuRepository;
import com.example.demo.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuItemServiceImpl implements MenuItemService {
    
    private final MenuItemRepository menuItemRepository;
    private final MenuRepository menuRepository;
    
    @Override
    public MenuItemResponseDTO createMenuItem(MenuItemRequestDTO menuItemRequestDTO) {
        MenuItem menuItem = mapToEntity(menuItemRequestDTO);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return mapToResponseDTO(savedMenuItem);
    }
    
    @Override
    public MenuItemResponseDTO updateMenuItem(Long id, MenuItemRequestDTO menuItemRequestDTO) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));
        
        updateEntityFromDTO(menuItem, menuItemRequestDTO);
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return mapToResponseDTO(updatedMenuItem);
    }
    
    @Override
    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<MenuItemResponseDTO> getMenuItemById(Long id) {
        return menuItemRepository.findById(id)
                .map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getAllMenuItems() {
        return menuItemRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getMenuItemsByMenuId(Long menuId) {
        return menuItemRepository.findByMenuId(menuId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getAvailableMenuItemsByMenuId(Long menuId) {
        return menuItemRepository.findAvailableItemsByMenuId(menuId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getHotMenuItems() {
        return menuItemRepository.findByIsHot(true).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getSignatureMenuItems() {
        return menuItemRepository.findByIsSignature(true).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> searchMenuItemsByName(String name) {
        return menuItemRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getTopViewedMenuItems() {
        return menuItemRepository.findTopViewedItems().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public void incrementViewCount(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));
        menuItem.setViewCount(menuItem.getViewCount() + 1);
        menuItemRepository.save(menuItem);
    }
    
    // Mapper methods
    private MenuItem mapToEntity(MenuItemRequestDTO dto) {
        MenuItem menuItem = new MenuItem();
        
        Menu menu = menuRepository.findById(dto.getMenuId())
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + dto.getMenuId()));
        menuItem.setMenu(menu);
        
        menuItem.setName(dto.getName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setPrice(dto.getPrice());
        menuItem.setImages(dto.getImages());
        menuItem.setIsAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true);
        menuItem.setIsHot(dto.getIsHot() != null ? dto.getIsHot() : false);
        menuItem.setIsSignature(dto.getIsSignature() != null ? dto.getIsSignature() : false);
        
        return menuItem;
    }
    
    private void updateEntityFromDTO(MenuItem menuItem, MenuItemRequestDTO dto) {
        if (dto.getMenuId() != null) {
            Menu menu = menuRepository.findById(dto.getMenuId())
                    .orElseThrow(() -> new RuntimeException("Menu not found with id: " + dto.getMenuId()));
            menuItem.setMenu(menu);
        }
        
        menuItem.setName(dto.getName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setPrice(dto.getPrice());
        menuItem.setImages(dto.getImages());
        if (dto.getIsAvailable() != null) {
            menuItem.setIsAvailable(dto.getIsAvailable());
        }
        if (dto.getIsHot() != null) {
            menuItem.setIsHot(dto.getIsHot());
        }
        if (dto.getIsSignature() != null) {
            menuItem.setIsSignature(dto.getIsSignature());
        }
    }
    
    private MenuItemResponseDTO mapToResponseDTO(MenuItem menuItem) {
        MenuItemResponseDTO dto = new MenuItemResponseDTO();
        dto.setId(menuItem.getId());
        
        if (menuItem.getMenu() != null) {
            dto.setMenuId(menuItem.getMenu().getId());
            dto.setMenuName(menuItem.getMenu().getName());
        }
        
        dto.setName(menuItem.getName());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice());
        dto.setImages(menuItem.getImages());
        dto.setIsAvailable(menuItem.getIsAvailable());
        dto.setIsHot(menuItem.getIsHot());
        dto.setIsSignature(menuItem.getIsSignature());
        dto.setViewCount(menuItem.getViewCount());
        dto.setCreatedAt(menuItem.getCreatedAt());
        dto.setUpdatedAt(menuItem.getUpdatedAt());
        
        return dto;
    }
}
