package com.example.demo.service.impl;

import com.example.demo.dto.request.MenuRequestDTO;
import com.example.demo.dto.response.MenuResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Menu;
import com.example.demo.model.Shop;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuServiceImpl implements MenuService {
    
    private final MenuRepository menuRepository;
    private final ShopRepository shopRepository;
    
    @Override
    public MenuResponseDTO createMenu(MenuRequestDTO menuRequestDTO) {
        Menu menu = mapToEntity(menuRequestDTO);
        Menu savedMenu = menuRepository.save(menu);
        return mapToResponseDTO(savedMenu);
    }
    
    @Override
    public MenuResponseDTO updateMenu(Long id, MenuRequestDTO menuRequestDTO) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + id));
        
        updateEntityFromDTO(menu, menuRequestDTO);
        Menu updatedMenu = menuRepository.save(menu);
        return mapToResponseDTO(updatedMenu);
    }
    
    @Override
    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<MenuResponseDTO> getMenuById(Long id) {
        return menuRepository.findById(id)
                .map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuResponseDTO> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuResponseDTO> getMenusByShopId(Long shopId) {
        return menuRepository.findByShopId(shopId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuResponseDTO> searchMenusByName(String name) {
        return menuRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    // Mapper methods
    private Menu mapToEntity(MenuRequestDTO dto) {
        Menu menu = new Menu();
        
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + dto.getShopId()));
        menu.setShop(shop);
        
        menu.setName(dto.getName());
        menu.setImages(dto.getImages());
        
        return menu;
    }
    
    private void updateEntityFromDTO(Menu menu, MenuRequestDTO dto) {
        if (dto.getShopId() != null) {
            Shop shop = shopRepository.findById(dto.getShopId())
                    .orElseThrow(() -> new RuntimeException("Shop not found with id: " + dto.getShopId()));
            menu.setShop(shop);
        }
        
        menu.setName(dto.getName());
        menu.setImages(dto.getImages());
    }
    
    private MenuResponseDTO mapToResponseDTO(Menu menu) {
        MenuResponseDTO dto = new MenuResponseDTO();
        dto.setId(menu.getId());
        
        if (menu.getShop() != null) {
            dto.setShopId(menu.getShop().getId());
            dto.setShopName(menu.getShop().getName());
        }
        
        dto.setName(menu.getName());
        dto.setImages(menu.getImages());
        dto.setCreatedAt(menu.getCreatedAt());
        dto.setUpdatedAt(menu.getUpdatedAt());
        
        return dto;
    }
}
