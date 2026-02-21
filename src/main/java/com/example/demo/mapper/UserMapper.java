package com.example.demo.mapper;

import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.model.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    
    private final ModelMapper modelMapper;
    
    public User toEntity(UserRequestDTO dto) {
        return modelMapper.map(dto, User.class);
    }
    
    public UserResponseDTO toResponseDTO(User entity) {
        return modelMapper.map(entity, UserResponseDTO.class);
    }
    
    public void updateEntityFromDTO(User entity, UserRequestDTO dto) {
        // Update only non-null fields
        if (dto.getUsername() != null) {
            entity.setUsername(dto.getUsername());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            entity.setPassword(dto.getPassword());
        }
        if (dto.getFullName() != null) {
            entity.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone());
        }
        if (dto.getAvatarUrl() != null) {
            entity.setAvatarUrl(dto.getAvatarUrl());
        }
    }
}
