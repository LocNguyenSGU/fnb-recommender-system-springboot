package com.example.demo.controller;

import com.example.demo.dto.request.MenuRequestDTO;
import com.example.demo.dto.response.MenuResponseDTO;
import com.example.demo.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {
    
    private final MenuService menuService;
    
    @GetMapping
    public ResponseEntity<List<MenuResponseDTO>> getAllMenus() {
        return ResponseEntity.ok(menuService.getAllMenus());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MenuResponseDTO> getMenuById(@PathVariable Long id) {
        return menuService.getMenuById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<MenuResponseDTO>> getMenusByShopId(@PathVariable Long shopId) {
        return ResponseEntity.ok(menuService.getMenusByShopId(shopId));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<MenuResponseDTO>> searchMenusByName(@RequestParam String name) {
        return ResponseEntity.ok(menuService.searchMenusByName(name));
    }
    
    @PostMapping
    public ResponseEntity<MenuResponseDTO> createMenu(@Valid @RequestBody MenuRequestDTO menuRequestDTO) {
        MenuResponseDTO created = menuService.createMenu(menuRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MenuResponseDTO> updateMenu(
            @PathVariable Long id,
            @Valid @RequestBody MenuRequestDTO menuRequestDTO) {
        MenuResponseDTO updated = menuService.updateMenu(id, menuRequestDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }
}
