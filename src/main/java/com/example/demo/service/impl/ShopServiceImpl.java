package com.example.demo.service.impl;

import com.example.demo.dto.request.ShopRequestDTO;
import com.example.demo.dto.response.ShopResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ShopMapper;
import com.example.demo.model.Shop;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopServiceImpl implements ShopService {
    
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ShopMapper shopMapper;
    
    @Override
    public ShopResponseDTO createShop(ShopRequestDTO shopRequestDTO) {
        Shop shop = shopMapper.toEntity(shopRequestDTO, userRepository, categoryRepository);
        Shop savedShop = shopRepository.save(shop);
        return shopMapper.toResponseDTO(savedShop);
    }
    
    @Override
    public ShopResponseDTO updateShop(Long id, ShopRequestDTO shopRequestDTO) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", id));
        
        shopMapper.updateEntityFromDTO(shop, shopRequestDTO, userRepository, categoryRepository);
        Shop updatedShop = shopRepository.save(shop);
        return shopMapper.toResponseDTO(updatedShop);
    }
    
    @Override
    public void deleteShop(Long id) {
        if (!shopRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shop", "id", id);
        }
        shopRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ShopResponseDTO> getShopById(Long id) {
        return shopRepository.findById(id)
                .map(shopMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ShopResponseDTO> getAllShops() {
        return shopRepository.findAll().stream()
                .map(shopMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ShopResponseDTO> getShopsByOwnerId(Long ownerId) {
        return shopRepository.findByOwnerId(ownerId).stream()
                .map(shopMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ShopResponseDTO> getShopsByCategoryId(Long categoryId) {
        return shopRepository.findByCategoryId(categoryId).stream()
                .map(shopMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ShopResponseDTO> getShopsByStatus(String status) {
        return shopRepository.findByStatus(status).stream()
                .map(shopMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ShopResponseDTO> searchShopsByName(String name) {
        return shopRepository.findByNameContainingIgnoreCase(name).stream()
                .map(shopMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ShopResponseDTO> findShopsWithinRadius(Double latitude, Double longitude, Double radius) {
        return shopRepository.findShopsWithinRadius(latitude, longitude, radius).stream()
                .map(shopMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
