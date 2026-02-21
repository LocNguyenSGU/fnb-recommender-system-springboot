package com.example.demo.mapper;

import com.example.demo.dto.request.CategoryRequestDTO;
import com.example.demo.dto.response.CategoryResponseDTO;
import com.example.demo.model.Category;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {
    
    private final ModelMapper modelMapper;
    
    public Category toEntity(CategoryRequestDTO dto) {
        return modelMapper.map(dto, Category.class);
    }
    
    public CategoryResponseDTO toResponseDTO(Category entity) {
        return modelMapper.map(entity, CategoryResponseDTO.class);
    }
    
    public void updateEntityFromDTO(Category entity, CategoryRequestDTO dto) {
        modelMapper.map(dto, entity);
    }
}
