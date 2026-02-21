package com.example.demo.service.impl;

import com.example.demo.dto.request.MenuItemRequestDTO;
import com.example.demo.dto.response.MenuItemResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.MenuItemMapper;
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
    private final MenuItemMapper menuItemMapper;
    
    @Override
    public MenuItemResponseDTO createMenuItem(MenuItemRequestDTO menuItemRequestDTO) {
        MenuItem menuItem = menuItemMapper.toEntity(menuItemRequestDTO, menuRepository);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return menuItemMapper.toResponseDTO(savedMenuItem);
    }
    
    @Override
    public MenuItemResponseDTO updateMenuItem(Long id, MenuItemRequestDTO menuItemRequestDTO) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        
        menuItemMapper.updateEntityFromDTO(menuItem, menuItemRequestDTO, menuRepository);
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return menuItemMapper.toResponseDTO(updatedMenuItem);
    }
    
    @Override
    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<MenuItemResponseDTO> getMenuItemById(Long id) {
        return menuItemRepository.findById(id)
                .map(menuItemMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getAllMenuItems() {
        return menuItemRepository.findAll().stream()
                .map(menuItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getMenuItemsByMenuId(Long menuId) {
        return menuItemRepository.findByMenuId(menuId).stream()
                .map(menuItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getAvailableMenuItemsByMenuId(Long menuId) {
        return menuItemRepository.findAvailableItemsByMenuId(menuId).stream()
                .map(menuItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getHotMenuItems() {
        return menuItemRepository.findByIsHot(true).stream()
                .map(menuItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getSignatureMenuItems() {
        return menuItemRepository.findByIsSignature(true).stream()
                .map(menuItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> searchMenuItemsByName(String name) {
        return menuItemRepository.findByNameContainingIgnoreCase(name).stream()
                .map(menuItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getTopViewedMenuItems() {
        return menuItemRepository.findTopViewedItems().stream()
                .map(menuItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public void incrementViewCount(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        menuItem.setViewCount(menuItem.getViewCount() + 1);
        menuItemRepository.save(menuItem);
    }
}
