package com.example.demo.service.impl;

import com.example.demo.dto.request.MenuRequestDTO;
import com.example.demo.dto.response.MenuResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.MenuMapper;
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
    private final MenuMapper menuMapper;
    
    @Override
    public MenuResponseDTO createMenu(MenuRequestDTO menuRequestDTO) {
        Menu menu = menuMapper.toEntity(menuRequestDTO, shopRepository);
        Menu savedMenu = menuRepository.save(menu);
        return menuMapper.toResponseDTO(savedMenu);
    }
    
    @Override
    public MenuResponseDTO updateMenu(Long id, MenuRequestDTO menuRequestDTO) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", id));
        
        menuMapper.updateEntityFromDTO(menu, menuRequestDTO, shopRepository);
        Menu updatedMenu = menuRepository.save(menu);
        return menuMapper.toResponseDTO(updatedMenu);
    }
    
    @Override
    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<MenuResponseDTO> getMenuById(Long id) {
        return menuRepository.findById(id)
                .map(menuMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuResponseDTO> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(menuMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuResponseDTO> getMenusByShopId(Long shopId) {
        return menuRepository.findByShopId(shopId).stream()
                .map(menuMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MenuResponseDTO> searchMenusByName(String name) {
        return menuRepository.findByNameContainingIgnoreCase(name).stream()
                .map(menuMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
