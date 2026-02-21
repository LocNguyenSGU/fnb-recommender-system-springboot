package com.example.demo.controller;

import com.example.demo.dto.request.MenuItemRequestDTO;
import com.example.demo.dto.response.MenuItemResponseDTO;
import com.example.demo.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {
    
    private final MenuItemService menuItemService;
    
    @GetMapping
    public ResponseEntity<List<MenuItemResponseDTO>> getAllMenuItems() {
        return ResponseEntity.ok(menuItemService.getAllMenuItems());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> getMenuItemById(@PathVariable Long id) {
        return menuItemService.getMenuItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/menu/{menuId}")
    public ResponseEntity<List<MenuItemResponseDTO>> getMenuItemsByMenuId(@PathVariable Long menuId) {
        return ResponseEntity.ok(menuItemService.getMenuItemsByMenuId(menuId));
    }
    
    @GetMapping("/menu/{menuId}/available")
    public ResponseEntity<List<MenuItemResponseDTO>> getAvailableMenuItemsByMenuId(@PathVariable Long menuId) {
        return ResponseEntity.ok(menuItemService.getAvailableMenuItemsByMenuId(menuId));
    }
    
    @GetMapping("/hot")
    public ResponseEntity<List<MenuItemResponseDTO>> getHotMenuItems() {
        return ResponseEntity.ok(menuItemService.getHotMenuItems());
    }
    
    @GetMapping("/signature")
    public ResponseEntity<List<MenuItemResponseDTO>> getSignatureMenuItems() {
        return ResponseEntity.ok(menuItemService.getSignatureMenuItems());
    }
    
    @GetMapping("/top-viewed")
    public ResponseEntity<List<MenuItemResponseDTO>> getTopViewedMenuItems() {
        return ResponseEntity.ok(menuItemService.getTopViewedMenuItems());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<MenuItemResponseDTO>> searchMenuItemsByName(@RequestParam String name) {
        return ResponseEntity.ok(menuItemService.searchMenuItemsByName(name));
    }
    
    @PostMapping
    public ResponseEntity<MenuItemResponseDTO> createMenuItem(@Valid @RequestBody MenuItemRequestDTO menuItemRequestDTO) {
        MenuItemResponseDTO created = menuItemService.createMenuItem(menuItemRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequestDTO menuItemRequestDTO) {
        MenuItemResponseDTO updated = menuItemService.updateMenuItem(id, menuItemRequestDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long id) {
        menuItemService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }
}
