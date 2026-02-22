package com.example.demo.service.integration;

import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserService Integration Tests")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserRequestDTO userRequestDTO;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUsername("testuser");
        userRequestDTO.setEmail("test@example.com");
        userRequestDTO.setFullName("Test User");
        userRequestDTO.setPassword("password123");
    }

    @Nested
    @DisplayName("Create User Integration Tests")
    class CreateUserIntegrationTests {

        @Test
        @DisplayName("Should create user with full transaction")
        void shouldCreateUserWithFullTransaction() {
            // When
            UserResponseDTO result = userService.createUser(userRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");

            // Verify in database
            assertThat(userRepository.findById(result.getId())).isPresent();
        }

        @Test
        @DisplayName("Should throw exception for duplicate email")
        void shouldThrowExceptionForDuplicateEmail() {
            // Given
            userService.createUser(userRequestDTO);

            UserRequestDTO duplicateEmailUser = new UserRequestDTO();
            duplicateEmailUser.setUsername("differentuser");
            duplicateEmailUser.setEmail("test@example.com");
            duplicateEmailUser.setFullName("Different User");

            // When & Then
            assertThatThrownBy(() -> userService.createUser(duplicateEmailUser))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("Should throw exception for duplicate username")
        void shouldThrowExceptionForDuplicateUsername() {
            // Given
            userService.createUser(userRequestDTO);

            UserRequestDTO duplicateUsernameUser = new UserRequestDTO();
            duplicateUsernameUser.setUsername("testuser");
            duplicateUsernameUser.setEmail("different@example.com");
            duplicateUsernameUser.setFullName("Different User");

            // When & Then
            assertThatThrownBy(() -> userService.createUser(duplicateUsernameUser))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("username");
        }
    }

    @Nested
    @DisplayName("Update User Integration Tests")
    class UpdateUserIntegrationTests {

        @Test
        @DisplayName("Should update user with full transaction")
        void shouldUpdateUserWithFullTransaction() {
            // Given
            UserResponseDTO createdUser = userService.createUser(userRequestDTO);

            UserRequestDTO updateRequest = new UserRequestDTO();
            updateRequest.setFullName("Updated Name");
            updateRequest.setEmail("updated@example.com");

            // When
            UserResponseDTO result = userService.updateUser(createdUser.getId(), updateRequest);

            // Then
            assertThat(result.getId()).isEqualTo(createdUser.getId());
            assertThat(result.getFullName()).isEqualTo("Updated Name");

            // Verify in database
            Optional<UserResponseDTO> dbUser = userService.getUserById(createdUser.getId());
            assertThat(dbUser).isPresent();
            assertThat(dbUser.get().getFullName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent user")
        void shouldThrowExceptionWhenUpdatingNonExistentUser() {
            // When & Then
            assertThatThrownBy(() -> userService.updateUser(999L, userRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }
    }

    @Nested
    @DisplayName("Delete User Integration Tests")
    class DeleteUserIntegrationTests {

        @Test
        @DisplayName("Should delete user from database")
        void shouldDeleteUserFromDatabase() {
            // Given
            UserResponseDTO createdUser = userService.createUser(userRequestDTO);
            Long userId = createdUser.getId();

            // When
            userService.deleteUser(userId);

            // Then
            assertThat(userRepository.findById(userId)).isEmpty();
            assertThat(userService.getUserById(userId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get User Integration Tests")
    class GetUserIntegrationTests {

        @Test
        @DisplayName("Should get user by id from database")
        void shouldGetUserByIdFromDatabase() {
            // Given
            UserResponseDTO createdUser = userService.createUser(userRequestDTO);

            // When
            Optional<UserResponseDTO> result = userService.getUserById(createdUser.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(createdUser.getId());
            assertThat(result.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should get user by username from database")
        void shouldGetUserByUsernameFromDatabase() {
            // Given
            userService.createUser(userRequestDTO);

            // When
            Optional<UserResponseDTO> result = userService.getUserByUsername("testuser");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should get user by email from database")
        void shouldGetUserByEmailFromDatabase() {
            // Given
            userService.createUser(userRequestDTO);

            // When
            Optional<UserResponseDTO> result = userService.getUserByEmail("test@example.com");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should get all users from database")
        void shouldGetAllUsersFromDatabase() {
            // Given
            userService.createUser(userRequestDTO);

            UserRequestDTO user2 = new UserRequestDTO();
            user2.setUsername("user2");
            user2.setEmail("user2@example.com");
            user2.setFullName("User 2");
            userService.createUser(user2);

            // When
            List<UserResponseDTO> result = userService.getAllUsers();

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Exists Integration Tests")
    class ExistsIntegrationTests {

        @Test
        @DisplayName("Should check if username exists in database")
        void shouldCheckIfUsernameExistsInDatabase() {
            // Given
            userService.createUser(userRequestDTO);

            // When
            boolean exists = userService.existsByUsername("testuser");
            boolean notExists = userService.existsByUsername("nonexistent");

            // Then
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }

        @Test
        @DisplayName("Should check if email exists in database")
        void shouldCheckIfEmailExistsInDatabase() {
            // Given
            userService.createUser(userRequestDTO);

            // When
            boolean exists = userService.existsByEmail("test@example.com");
            boolean notExists = userService.existsByEmail("nonexistent@example.com");

            // Then
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }
    }

    @Nested
    @DisplayName("Transaction Rollback Tests")
    class TransactionRollbackTests {

        @Test
        @DisplayName("Should rollback on error")
        void shouldRollbackOnError() {
            // Given
            userService.createUser(userRequestDTO);
            long initialCount = userRepository.count();

            // When trying to create duplicate
            try {
                userService.createUser(userRequestDTO);
            } catch (DuplicateResourceException e) {
                // Expected
            }

            // Then - count should remain the same
            assertThat(userRepository.count()).isEqualTo(initialCount);
        }
    }
}
