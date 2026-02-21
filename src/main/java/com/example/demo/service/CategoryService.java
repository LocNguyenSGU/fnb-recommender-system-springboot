package com.example.demo.service;

import com.example.demo.dto.request.CategoryRequestDTO;
import com.example.demo.dto.response.CategoryResponseDTO;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    
    CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO);
    
    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO);
    
    void deleteCategory(Long id);
    
    Optional<CategoryResponseDTO> getCategoryById(Long id);
    
    Optional<CategoryResponseDTO> getCategoryByName(String name);
    
    List<CategoryResponseDTO> getAllCategories();
    
    boolean existsByName(String name);
}
