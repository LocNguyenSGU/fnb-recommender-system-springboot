package com.example.demo.service.impl;

import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUsername("testuser");
        userRequestDTO.setEmail("test@example.com");
        userRequestDTO.setFullName("Test User");

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setUsername("testuser");
        userResponseDTO.setEmail("test@example.com");
        userResponseDTO.setFullName("Test User");
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userMapper.toEntity(any(UserRequestDTO.class))).thenReturn(user);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

            // When
            UserResponseDTO result = userService.createUser(userRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");

            verify(userRepository).existsByEmail("test@example.com");
            verify(userRepository).existsByUsername("testuser");
            verify(userRepository).save(any(User.class));
            verify(userMapper).toEntity(userRequestDTO);
            verify(userMapper).toResponseDTO(user);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(userRequestDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");

            verify(userRepository).existsByEmail("test@example.com");
            verify(userRepository, never()).existsByUsername(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(userRequestDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("username");

            verify(userRepository).existsByEmail("test@example.com");
            verify(userRepository).existsByUsername("testuser");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle null values in request")
        void shouldHandleNullValuesInRequest() {
            // Given
            UserRequestDTO emptyRequest = new UserRequestDTO();
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(userRepository.existsByUsername(any())).thenReturn(false);
            when(userMapper.toEntity(any(UserRequestDTO.class))).thenReturn(user);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

            // When
            UserResponseDTO result = userService.createUser(emptyRequest);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            doNothing().when(userMapper).updateEntityFromDTO(any(User.class), any(UserRequestDTO.class));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

            // When
            UserResponseDTO result = userService.updateUser(userId, userRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);

            verify(userRepository).findById(userId);
            verify(userMapper).updateEntityFromDTO(user, userRequestDTO);
            verify(userRepository).save(user);
            verify(userMapper).toResponseDTO(user);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(userId, userRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User")
                    .hasMessageContaining("id");

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            // Given
            Long userId = 1L;
            UserRequestDTO partialUpdate = new UserRequestDTO();
            partialUpdate.setFullName("Updated Name");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            doNothing().when(userMapper).updateEntityFromDTO(any(User.class), any(UserRequestDTO.class));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

            // When
            UserResponseDTO result = userService.updateUser(userId, partialUpdate);

            // Then
            assertThat(result).isNotNull();
            verify(userMapper).updateEntityFromDTO(user, partialUpdate);
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            // Given
            Long userId = 1L;
            doNothing().when(userRepository).deleteById(userId);

            // When
            userService.deleteUser(userId);

            // Then
            verify(userRepository).deleteById(userId);
        }

        @Test
        @DisplayName("Should not throw exception when deleting non-existent user")
        void shouldHandleDeleteNonExistentUser() {
            // Given
            Long userId = 999L;
            doNothing().when(userRepository).deleteById(userId);

            // When
            userService.deleteUser(userId);

            // Then
            verify(userRepository).deleteById(userId);
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by id successfully")
        void shouldGetUserByIdSuccessfully() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

            // When
            Optional<UserResponseDTO> result = userService.getUserById(userId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(userId);

            verify(userRepository).findById(userId);
            verify(userMapper).toResponseDTO(user);
        }

        @Test
        @DisplayName("Should return empty when user not found by id")
        void shouldReturnEmptyWhenUserNotFoundById() {
            // Given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When
            Optional<UserResponseDTO> result = userService.getUserById(userId);

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findById(userId);
            verify(userMapper, never()).toResponseDTO(any(User.class));
        }

        @Test
        @DisplayName("Should get user by username successfully")
        void shouldGetUserByUsernameSuccessfully() {
            // Given
            String username = "testuser";
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

            // When
            Optional<UserResponseDTO> result = userService.getUserByUsername(username);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo(username);

            verify(userRepository).findByUsername(username);
            verify(userMapper).toResponseDTO(user);
        }

        @Test
        @DisplayName("Should return empty when user not found by username")
        void shouldReturnEmptyWhenUserNotFoundByUsername() {
            // Given
            String username = "nonexistent";
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            // When
            Optional<UserResponseDTO> result = userService.getUserByUsername(username);

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findByUsername(username);
        }

        @Test
        @DisplayName("Should get user by email successfully")
        void shouldGetUserByEmailSuccessfully() {
            // Given
            String email = "test@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

            // When
            Optional<UserResponseDTO> result = userService.getUserByEmail(email);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(email);

            verify(userRepository).findByEmail(email);
            verify(userMapper).toResponseDTO(user);
        }

        @Test
        @DisplayName("Should return empty when user not found by email")
        void shouldReturnEmptyWhenUserNotFoundByEmail() {
            // Given
            String email = "nonexistent@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // When
            Optional<UserResponseDTO> result = userService.getUserByEmail(email);

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findByEmail(email);
        }

        @Test
        @DisplayName("Should get all users successfully")
        void shouldGetAllUsersSuccessfully() {
            // Given
            User user2 = new User();
            user2.setId(2L);
            user2.setUsername("testuser2");

            UserResponseDTO userResponseDTO2 = new UserResponseDTO();
            userResponseDTO2.setId(2L);
            userResponseDTO2.setUsername("testuser2");

            List<User> users = Arrays.asList(user, user2);

            when(userRepository.findAll()).thenReturn(users);
            when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);
            when(userMapper.toResponseDTO(user2)).thenReturn(userResponseDTO2);

            // When
            List<UserResponseDTO> result = userService.getAllUsers();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUsername()).isEqualTo("testuser");
            assertThat(result.get(1).getUsername()).isEqualTo("testuser2");

            verify(userRepository).findAll();
            verify(userMapper, times(2)).toResponseDTO(any(User.class));
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsersExist() {
            // Given
            when(userRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<UserResponseDTO> result = userService.getAllUsers();

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Exists Tests")
    class ExistsTests {

        @Test
        @DisplayName("Should return true when username exists")
        void shouldReturnTrueWhenUsernameExists() {
            // Given
            String username = "testuser";
            when(userRepository.existsByUsername(username)).thenReturn(true);

            // When
            boolean result = userService.existsByUsername(username);

            // Then
            assertThat(result).isTrue();
            verify(userRepository).existsByUsername(username);
        }

        @Test
        @DisplayName("Should return false when username does not exist")
        void shouldReturnFalseWhenUsernameDoesNotExist() {
            // Given
            String username = "nonexistent";
            when(userRepository.existsByUsername(username)).thenReturn(false);

            // When
            boolean result = userService.existsByUsername(username);

            // Then
            assertThat(result).isFalse();
            verify(userRepository).existsByUsername(username);
        }

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            // Given
            String email = "test@example.com";
            when(userRepository.existsByEmail(email)).thenReturn(true);

            // When
            boolean result = userService.existsByEmail(email);

            // Then
            assertThat(result).isTrue();
            verify(userRepository).existsByEmail(email);
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // Given
            String email = "nonexistent@example.com";
            when(userRepository.existsByEmail(email)).thenReturn(false);

            // When
            boolean result = userService.existsByEmail(email);

            // Then
            assertThat(result).isFalse();
            verify(userRepository).existsByEmail(email);
        }

        @Test
        @DisplayName("Should handle null username check")
        void shouldHandleNullUsernameCheck() {
            // Given
            when(userRepository.existsByUsername(null)).thenReturn(false);

            // When
            boolean result = userService.existsByUsername(null);

            // Then
            assertThat(result).isFalse();
            verify(userRepository).existsByUsername(null);
        }

        @Test
        @DisplayName("Should handle null email check")
        void shouldHandleNullEmailCheck() {
            // Given
            when(userRepository.existsByEmail(null)).thenReturn(false);

            // When
            boolean result = userService.existsByEmail(null);

            // Then
            assertThat(result).isFalse();
            verify(userRepository).existsByEmail(null);
        }
    }
}
