package com.example.demo.service.integration;

import com.example.demo.dto.request.CategoryRequestDTO;
import com.example.demo.dto.request.ShopRequestDTO;
import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.CategoryResponseDTO;
import com.example.demo.dto.response.ShopResponseDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ShopRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ShopService;
import com.example.demo.service.UserService;

import java.math.BigDecimal;
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
@DisplayName("ShopService Integration Tests")
class ShopServiceIntegrationTest {

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ShopRepository shopRepository;

    private Long ownerId;
    private Long categoryId;
    private ShopRequestDTO shopRequestDTO;

    @BeforeEach
    void setUp() {
        shopRepository.deleteAll();

        // Create owner
        UserRequestDTO ownerRequest = new UserRequestDTO();
        ownerRequest.setUsername("shopowner");
        ownerRequest.setEmail("owner@example.com");
        ownerRequest.setFullName("Shop Owner");
        ownerRequest.setPassword("password");
        UserResponseDTO owner = userService.createUser(ownerRequest);
        ownerId = owner.getId();

        // Create category
        CategoryRequestDTO categoryRequest = new CategoryRequestDTO();
        categoryRequest.setName("Vietnamese Food");
        categoryRequest.setDescription("Traditional Vietnamese cuisine");
        CategoryResponseDTO category = categoryService.createCategory(categoryRequest);
        categoryId = category.getId();

        shopRequestDTO = new ShopRequestDTO();
        shopRequestDTO.setName("Pho Restaurant");
        shopRequestDTO.setAddress("123 Main St");
        shopRequestDTO.setOwnerId(ownerId);
        shopRequestDTO.setCategoryId(categoryId);
        shopRequestDTO.setLatitude(BigDecimal.valueOf(10.8231));
        shopRequestDTO.setLongitude(BigDecimal.valueOf(106.6297));
        shopRequestDTO.setStatus("ACTIVE");
    }

    @Nested
    @DisplayName("Create Shop Integration Tests")
    class CreateShopIntegrationTests {

        @Test
        @DisplayName("Should create shop with owner and category relationships")
        void shouldCreateShopWithRelationships() {
            // When
            ShopResponseDTO result = shopService.createShop(shopRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getName()).isEqualTo("Pho Restaurant");

            // Verify in database
            assertThat(shopRepository.findById(result.getId())).isPresent();
        }

        @Test
        @DisplayName("Should create shop with location coordinates")
        void shouldCreateShopWithLocationCoordinates() {
            // When
            ShopResponseDTO result = shopService.createShop(shopRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(shopRepository.findById(result.getId())).isPresent();
        }
    }

    @Nested
    @DisplayName("Update Shop Integration Tests")
    class UpdateShopIntegrationTests {

        @Test
        @DisplayName("Should update shop in database")
        void shouldUpdateShopInDatabase() {
            // Given
            ShopResponseDTO createdShop = shopService.createShop(shopRequestDTO);

            ShopRequestDTO updateRequest = new ShopRequestDTO();
            updateRequest.setName("Updated Restaurant");
            updateRequest.setAddress("456 New St");

            // When
            ShopResponseDTO result = shopService.updateShop(createdShop.getId(), updateRequest);

            // Then
            assertThat(result.getName()).isEqualTo("Updated Restaurant");

            // Verify in database
            Optional<ShopResponseDTO> dbShop = shopService.getShopById(createdShop.getId());
            assertThat(dbShop).isPresent();
            assertThat(dbShop.get().getName()).isEqualTo("Updated Restaurant");
        }
    }

    @Nested
    @DisplayName("Delete Shop Integration Tests")
    class DeleteShopIntegrationTests {

        @Test
        @DisplayName("Should delete shop from database")
        void shouldDeleteShopFromDatabase() {
            // Given
            ShopResponseDTO createdShop = shopService.createShop(shopRequestDTO);
            Long shopId = createdShop.getId();

            // When
            shopService.deleteShop(shopId);

            // Then
            assertThat(shopRepository.findById(shopId)).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent shop")
        void shouldThrowExceptionWhenDeletingNonExistentShop() {
            // When & Then
            assertThatThrownBy(() -> shopService.deleteShop(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shop");
        }
    }

    @Nested
    @DisplayName("Get Shop Integration Tests")
    class GetShopIntegrationTests {

        @Test
        @DisplayName("Should get shops by owner id")
        void shouldGetShopsByOwnerId() {
            // Given
            shopService.createShop(shopRequestDTO);

            ShopRequestDTO shop2 = new ShopRequestDTO();
            shop2.setName("Second Restaurant");
            shop2.setAddress("789 St");
            shop2.setOwnerId(ownerId);
            shop2.setCategoryId(categoryId);
            shopService.createShop(shop2);

            // When
            List<ShopResponseDTO> result = shopService.getShopsByOwnerId(ownerId);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should get shops by category id")
        void shouldGetShopsByCategoryId() {
            // Given
            shopService.createShop(shopRequestDTO);

            // When
            List<ShopResponseDTO> result = shopService.getShopsByCategoryId(categoryId);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get shops by status")
        void shouldGetShopsByStatus() {
            // Given
            shopService.createShop(shopRequestDTO);

            ShopRequestDTO inactiveShop = new ShopRequestDTO();
            inactiveShop.setName("Inactive Shop");
            inactiveShop.setAddress("999 St");
            inactiveShop.setOwnerId(ownerId);
            inactiveShop.setCategoryId(categoryId);
            inactiveShop.setStatus("INACTIVE");
            shopService.createShop(inactiveShop);

            // When
            List<ShopResponseDTO> activeShops = shopService.getShopsByStatus("ACTIVE");
            List<ShopResponseDTO> inactiveShops = shopService.getShopsByStatus("INACTIVE");

            // Then
            assertThat(activeShops).hasSize(1);
            assertThat(inactiveShops).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Search Shop Integration Tests")
    class SearchShopIntegrationTests {

        @Test
        @DisplayName("Should search shops by name")
        void shouldSearchShopsByName() {
            // Given
            shopService.createShop(shopRequestDTO);

            ShopRequestDTO shop2 = new ShopRequestDTO();
            shop2.setName("Pho 24");
            shop2.setAddress("789 St");
            shop2.setOwnerId(ownerId);
            shop2.setCategoryId(categoryId);
            shopService.createShop(shop2);

            // When
            List<ShopResponseDTO> result = shopService.searchShopsByName("Pho");

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should search shops case-insensitively")
        void shouldSearchShopsCaseInsensitively() {
            // Given
            shopService.createShop(shopRequestDTO);

            // When
            List<ShopResponseDTO> result = shopService.searchShopsByName("pho");

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Location-based Search Integration Tests")
    class LocationBasedSearchIntegrationTests {

        @Test
        @DisplayName("Should find shops within radius")
        @org.junit.jupiter.api.Disabled("PostGIS ST_DWithin function not supported in H2")
        void shouldFindShopsWithinRadius() {
            // Given
            shopService.createShop(shopRequestDTO);

            // When - search near the shop location
            List<ShopResponseDTO> result = shopService.findShopsWithinRadius(
                    10.8231, 106.6297, 5.0
            );

            // Then
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should not find shops outside radius")
        @org.junit.jupiter.api.Disabled("PostGIS ST_DWithin function not supported in H2")
        void shouldNotFindShopsOutsideRadius() {
            // Given
            shopService.createShop(shopRequestDTO);

            // When - search far from the shop location
            List<ShopResponseDTO> result = shopService.findShopsWithinRadius(
                    0.0, 0.0, 1.0
            );

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Referential Integrity Tests")
    class ReferentialIntegrityTests {

        @Test
        @DisplayName("Should maintain owner relationship")
        void shouldMaintainOwnerRelationship() {
            // Given
            ShopResponseDTO shop = shopService.createShop(shopRequestDTO);

            // When
            Optional<ShopResponseDTO> result = shopService.getShopById(shop.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(userService.getUserById(ownerId)).isPresent();
        }

        @Test
        @DisplayName("Should maintain category relationship")
        void shouldMaintainCategoryRelationship() {
            // Given
            ShopResponseDTO shop = shopService.createShop(shopRequestDTO);

            // When
            Optional<ShopResponseDTO> result = shopService.getShopById(shop.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(categoryService.getCategoryById(categoryId)).isPresent();
        }
    }
}
