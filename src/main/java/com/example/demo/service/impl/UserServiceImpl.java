package com.example.demo.service.impl;

import com.example.demo.dto.request.UpdateUserRequestDTO;
import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        // Check duplicate email
        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new DuplicateResourceException("User", "email", userRequestDTO.getEmail());
        }
        
        // Check duplicate username
        if (userRepository.existsByUsername(userRequestDTO.getUsername())) {
            throw new DuplicateResourceException("User", "username", userRequestDTO.getUsername());
        }
        
        User user = userMapper.toEntity(userRequestDTO);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }
    
    @Override
    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO userRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        userMapper.updateEntityFromDTO(user, userRequestDTO);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDTO(updatedUser);
    }
    
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
