package com.example.demo.service;

import com.example.demo.dto.request.UpdateUserRequestDTO;
import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;

import java.util.List;
import java.util.Optional;

public interface UserService {
    
    UserResponseDTO createUser(UserRequestDTO userRequestDTO);
    
    UserResponseDTO updateUser(Long id, UpdateUserRequestDTO userRequestDTO);
    
    void deleteUser(Long id);
    
    Optional<UserResponseDTO> getUserById(Long id);
    
    Optional<UserResponseDTO> getUserByUsername(String username);
    
    Optional<UserResponseDTO> getUserByEmail(String email);
    
    List<UserResponseDTO> getAllUsers();
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
