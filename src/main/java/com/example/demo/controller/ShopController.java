package com.example.demo.controller;

import com.example.demo.dto.request.ShopRequestDTO;
import com.example.demo.dto.response.ShopResponseDTO;
import com.example.demo.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {
    
    private final ShopService shopService;
    
    @GetMapping
    public ResponseEntity<List<ShopResponseDTO>> getAllShops() {
        return ResponseEntity.ok(shopService.getAllShops());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ShopResponseDTO> getShopById(@PathVariable Long id) {
        return shopService.getShopById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ShopResponseDTO>> getShopsByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(shopService.getShopsByOwnerId(ownerId));
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ShopResponseDTO>> getShopsByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(shopService.getShopsByCategoryId(categoryId));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ShopResponseDTO>> getShopsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(shopService.getShopsByStatus(status));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ShopResponseDTO>> searchShopsByName(@RequestParam String name) {
        return ResponseEntity.ok(shopService.searchShopsByName(name));
    }
    
    @GetMapping("/nearby")
    public ResponseEntity<List<ShopResponseDTO>> findShopsWithinRadius(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radius) {
        return ResponseEntity.ok(shopService.findShopsWithinRadius(latitude, longitude, radius));
    }
    
    @PostMapping
    public ResponseEntity<ShopResponseDTO> createShop(@Valid @RequestBody ShopRequestDTO shopRequestDTO) {
        ShopResponseDTO created = shopService.createShop(shopRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ShopResponseDTO> updateShop(
            @PathVariable Long id,
            @Valid @RequestBody ShopRequestDTO shopRequestDTO) {
        ShopResponseDTO updated = shopService.updateShop(id, shopRequestDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}
