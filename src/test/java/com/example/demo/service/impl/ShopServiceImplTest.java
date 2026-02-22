package com.example.demo.service.impl;

import com.example.demo.dto.request.ShopRequestDTO;
import com.example.demo.dto.response.ShopResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ShopMapper;
import com.example.demo.model.Shop;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.UserRepository;

import java.math.BigDecimal;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShopService Unit Tests")
class ShopServiceImplTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ShopMapper shopMapper;

    @InjectMocks
    private ShopServiceImpl shopService;

    private Shop shop;
    private ShopRequestDTO shopRequestDTO;
    private ShopResponseDTO shopResponseDTO;

    @BeforeEach
    void setUp() {
        shop = new Shop();
        shop.setId(1L);
        shop.setName("Pho Restaurant");
        shop.setAddress("123 Main St");
        shop.setStatus("ACTIVE");
        shop.setLatitude(BigDecimal.valueOf(10.8231));
        shop.setLongitude(BigDecimal.valueOf(106.6297));

        shopRequestDTO = new ShopRequestDTO();
        shopRequestDTO.setName("Pho Restaurant");
        shopRequestDTO.setAddress("123 Main St");
        shopRequestDTO.setOwnerId(1L);
        shopRequestDTO.setCategoryId(1L);

        shopResponseDTO = new ShopResponseDTO();
        shopResponseDTO.setId(1L);
        shopResponseDTO.setName("Pho Restaurant");
        shopResponseDTO.setAddress("123 Main St");
    }

    @Nested
    @DisplayName("Create Shop Tests")
    class CreateShopTests {

        @Test
        @DisplayName("Should create shop successfully")
        void shouldCreateShopSuccessfully() {
            // Given
            when(shopMapper.toEntity(any(ShopRequestDTO.class), any(UserRepository.class), any(CategoryRepository.class)))
                    .thenReturn(shop);
            when(shopRepository.save(any(Shop.class))).thenReturn(shop);
            when(shopMapper.toResponseDTO(any(Shop.class))).thenReturn(shopResponseDTO);

            // When
            ShopResponseDTO result = shopService.createShop(shopRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Pho Restaurant");

            verify(shopMapper).toEntity(shopRequestDTO, userRepository, categoryRepository);
            verify(shopRepository).save(shop);
            verify(shopMapper).toResponseDTO(shop);
        }

        @Test
        @DisplayName("Should create shop with location coordinates")
        void shouldCreateShopWithLocationCoordinates() {
            // Given
            shopRequestDTO.setLatitude(BigDecimal.valueOf(10.8231));
            shopRequestDTO.setLongitude(BigDecimal.valueOf(106.6297));

            when(shopMapper.toEntity(any(ShopRequestDTO.class), any(UserRepository.class), any(CategoryRepository.class)))
                    .thenReturn(shop);
            when(shopRepository.save(any(Shop.class))).thenReturn(shop);
            when(shopMapper.toResponseDTO(any(Shop.class))).thenReturn(shopResponseDTO);

            // When
            ShopResponseDTO result = shopService.createShop(shopRequestDTO);

            // Then
            assertThat(result).isNotNull();
            verify(shopRepository).save(any(Shop.class));
        }
    }

    @Nested
    @DisplayName("Update Shop Tests")
    class UpdateShopTests {

        @Test
        @DisplayName("Should update shop successfully")
        void shouldUpdateShopSuccessfully() {
            // Given
            Long shopId = 1L;
            when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
            doNothing().when(shopMapper).updateEntityFromDTO(any(Shop.class), any(ShopRequestDTO.class), 
                    any(UserRepository.class), any(CategoryRepository.class));
            when(shopRepository.save(any(Shop.class))).thenReturn(shop);
            when(shopMapper.toResponseDTO(any(Shop.class))).thenReturn(shopResponseDTO);

            // When
            ShopResponseDTO result = shopService.updateShop(shopId, shopRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(shopId);

            verify(shopRepository).findById(shopId);
            verify(shopMapper).updateEntityFromDTO(shop, shopRequestDTO, userRepository, categoryRepository);
            verify(shopRepository).save(shop);
        }

        @Test
        @DisplayName("Should throw exception when shop not found")
        void shouldThrowExceptionWhenShopNotFound() {
            // Given
            Long shopId = 999L;
            when(shopRepository.findById(shopId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shopService.updateShop(shopId, shopRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shop")
                    .hasMessageContaining("id");

            verify(shopRepository).findById(shopId);
            verify(shopRepository, never()).save(any(Shop.class));
        }
    }

    @Nested
    @DisplayName("Delete Shop Tests")
    class DeleteShopTests {

        @Test
        @DisplayName("Should delete shop successfully")
        void shouldDeleteShopSuccessfully() {
            // Given
            Long shopId = 1L;
            when(shopRepository.existsById(shopId)).thenReturn(true);
            doNothing().when(shopRepository).deleteById(shopId);

            // When
            shopService.deleteShop(shopId);

            // Then
            verify(shopRepository).existsById(shopId);
            verify(shopRepository).deleteById(shopId);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent shop")
        void shouldThrowExceptionWhenDeletingNonExistentShop() {
            // Given
            Long shopId = 999L;
            when(shopRepository.existsById(shopId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> shopService.deleteShop(shopId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shop");

            verify(shopRepository).existsById(shopId);
            verify(shopRepository, never()).deleteById(shopId);
        }
    }

    @Nested
    @DisplayName("Get Shop Tests")
    class GetShopTests {

        @Test
        @DisplayName("Should get shop by id successfully")
        void shouldGetShopByIdSuccessfully() {
            // Given
            Long shopId = 1L;
            when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
            when(shopMapper.toResponseDTO(any(Shop.class))).thenReturn(shopResponseDTO);

            // When
            Optional<ShopResponseDTO> result = shopService.getShopById(shopId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(shopId);

            verify(shopRepository).findById(shopId);
            verify(shopMapper).toResponseDTO(shop);
        }

        @Test
        @DisplayName("Should return empty when shop not found")
        void shouldReturnEmptyWhenShopNotFound() {
            // Given
            Long shopId = 999L;
            when(shopRepository.findById(shopId)).thenReturn(Optional.empty());

            // When
            Optional<ShopResponseDTO> result = shopService.getShopById(shopId);

            // Then
            assertThat(result).isEmpty();
            verify(shopRepository).findById(shopId);
            verify(shopMapper, never()).toResponseDTO(any(Shop.class));
        }

        @Test
        @DisplayName("Should get all shops successfully")
        void shouldGetAllShopsSuccessfully() {
            // Given
            Shop shop2 = new Shop();
            shop2.setId(2L);
            shop2.setName("Bun Bo Hue");

            ShopResponseDTO shopResponseDTO2 = new ShopResponseDTO();
            shopResponseDTO2.setId(2L);
            shopResponseDTO2.setName("Bun Bo Hue");

            List<Shop> shops = Arrays.asList(shop, shop2);

            when(shopRepository.findAll()).thenReturn(shops);
            when(shopMapper.toResponseDTO(shop)).thenReturn(shopResponseDTO);
            when(shopMapper.toResponseDTO(shop2)).thenReturn(shopResponseDTO2);

            // When
            List<ShopResponseDTO> result = shopService.getAllShops();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Pho Restaurant");
            assertThat(result.get(1).getName()).isEqualTo("Bun Bo Hue");

            verify(shopRepository).findAll();
            verify(shopMapper, times(2)).toResponseDTO(any(Shop.class));
        }

        @Test
        @DisplayName("Should return empty list when no shops exist")
        void shouldReturnEmptyListWhenNoShopsExist() {
            // Given
            when(shopRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<ShopResponseDTO> result = shopService.getAllShops();

            // Then
            assertThat(result).isEmpty();
            verify(shopRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Get Shops By Owner Tests")
    class GetShopsByOwnerTests {

        @Test
        @DisplayName("Should get shops by owner id successfully")
        void shouldGetShopsByOwnerIdSuccessfully() {
            // Given
            Long ownerId = 1L;
            List<Shop> ownerShops = Arrays.asList(shop);

            when(shopRepository.findByOwnerId(ownerId)).thenReturn(ownerShops);
            when(shopMapper.toResponseDTO(any(Shop.class))).thenReturn(shopResponseDTO);

            // When
            List<ShopResponseDTO> result = shopService.getShopsByOwnerId(ownerId);

            // Then
            assertThat(result).hasSize(1);
            verify(shopRepository).findByOwnerId(ownerId);
        }

        @Test
        @DisplayName("Should return empty list when owner has no shops")
        void shouldReturnEmptyListWhenOwnerHasNoShops() {
            // Given
            Long ownerId = 999L;
            when(shopRepository.findByOwnerId(ownerId)).thenReturn(Arrays.asList());

            // When
            List<ShopResponseDTO> result = shopService.getShopsByOwnerId(ownerId);

            // Then
            assertThat(result).isEmpty();
            verify(shopRepository).findByOwnerId(ownerId);
        }
    }

    @Nested
    @DisplayName("Get Shops By Category Tests")
    class GetShopsByCategoryTests {

        @Test
        @DisplayName("Should get shops by category id successfully")
        void shouldGetShopsByCategoryIdSuccessfully() {
            // Given
            Long categoryId = 1L;
            List<Shop> categoryShops = Arrays.asList(shop);

            when(shopRepository.findByCategoryId(categoryId)).thenReturn(categoryShops);
            when(shopMapper.toResponseDTO(any(Shop.class))).thenReturn(shopResponseDTO);

            // When
            List<ShopResponseDTO> result = shopService.getShopsByCategoryId(categoryId);

            // Then
            assertThat(result).hasSize(1);
            verify(shopRepository).findByCategoryId(categoryId);
        }

        @Test
        @DisplayName("Should return empty list when category has no shops")
        void shouldReturnEmptyListWhenCategoryHasNoShops() {
            // Given
            Long categoryId = 999L;
            when(shopRepository.findByCategoryId(categoryId)).thenReturn(Arrays.asList());

            // When
            List<ShopResponseDTO> result = shopService.getShopsByCategoryId(categoryId);

            // Then
            assertThat(result).isEmpty();
            verify(shopRepository).findByCategoryId(categoryId);
        }
    }

    @Nested
    @DisplayName("Get Shops By Status Tests")
    class GetShopsByStatusTests {

        @Test
        @DisplayName("Should get active shops successfully")
        void shouldGetActiveShopsSuccessfully() {
            // Given
            String status = "ACTIVE";
            List<Shop> activeShops = Arrays.asList(shop);

            when(shopRepository.findByStatus(status)).thenReturn(activeShops);
            when(shopMapper.toResponseDTO(any(Shop.class))).thenReturn(shopResponseDTO);

            // When
            List<ShopResponseDTO> result = shopService.getShopsByStatus(status);

            // Then
            assertThat(result).hasSize(1);
            verify(shopRepository).findByStatus(status);
        }

        @Test
        @DisplayName("Should get inactive shops successfully")
        void shouldGetInactiveShopsSuccessfully() {
            // Given
            String status = "INACTIVE";
            when(shopRepository.findByStatus(status)).thenReturn(Arrays.asList());

            // When
            List<ShopResponseDTO> result = shopService.getShopsByStatus(status);

            // Then
            assertThat(result).isEmpty();
            verify(shopRepository).findByStatus(status);
        }
    }

    @Nested
    @DisplayName("Search Shops Tests")
    class SearchShopsTests {

        @Test
        @DisplayName("Should search shops by name successfully")
        void shouldSearchShopsByNameSuccessfully() {
            // Given
            String name = "Pho";
            List<Shop> foundShops = Arrays.asList(shop);

            when(shopRepository.findByNameContainingIgnoreCase(name)).thenReturn(foundShops);
            when(shopMapper.toResponseDTO(any(Shop.class))).thenReturn(shopResponseDTO);

            // When
            List<ShopResponseDTO> result = shopService.searchShopsByName(name);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains("Pho");

            verify(shopRepository).findByNameContainingIgnoreCase(name);
        }

        @Test
        @DisplayName("Should return empty list when no shops match name")
        void shouldReturnEmptyListWhenNoShopsMatchName() {
            // Given
            String name = "NonExistent";
            when(shopRepository.findByNameContainingIgnoreCase(name)).thenReturn(Arrays.asList());

            // When
            List<ShopResponseDTO> result = shopService.searchShopsByName(name);

            // Then
            assertThat(result).isEmpty();
            verify(shopRepository).findByNameContainingIgnoreCase(name);
        }

        @Test
        @DisplayName("Should handle case-insensitive search")
        void shouldHandleCaseInsensitiveSearch() {
            // Given
            String name = "PHO";
            List<Shop> foundShops = Arrays.asList(shop);

            when(shopRepository.findByNameContainingIgnoreCase(name)).thenReturn(foundShops);
            when(shopMapper.toResponseDTO(any(Shop.class))).thenReturn(shopResponseDTO);

            // When
            List<ShopResponseDTO> result = shopService.searchShopsByName(name);

            // Then
            assertThat(result).hasSize(1);
            verify(shopRepository).findByNameContainingIgnoreCase(name);
        }
    }

    @Nested
    @DisplayName("Find Shops Within Radius Tests")
    class FindShopsWithinRadiusTests {

        @Test
        @DisplayName("Should find shops within radius successfully")
        void shouldFindShopsWithinRadiusSuccessfully() {
            // Given
            Double latitude = 10.8231;
            Double longitude = 106.6297;
            Double radius = 5.0;

            List<Shop> nearbyShops = Arrays.asList(shop);

            when(shopRepository.findShopsWithinRadius(latitude, longitude, radius)).thenReturn(nearbyShops);
            when(shopMapper.toResponseDTO(any(Shop.class))).thenReturn(shopResponseDTO);

            // When
            List<ShopResponseDTO> result = shopService.findShopsWithinRadius(latitude, longitude, radius);

            // Then
            assertThat(result).hasSize(1);
            verify(shopRepository).findShopsWithinRadius(latitude, longitude, radius);
        }

        @Test
        @DisplayName("Should return empty list when no shops within radius")
        void shouldReturnEmptyListWhenNoShopsWithinRadius() {
            // Given
            Double latitude = 0.0;
            Double longitude = 0.0;
            Double radius = 1.0;

            when(shopRepository.findShopsWithinRadius(latitude, longitude, radius)).thenReturn(Arrays.asList());

            // When
            List<ShopResponseDTO> result = shopService.findShopsWithinRadius(latitude, longitude, radius);

            // Then
            assertThat(result).isEmpty();
            verify(shopRepository).findShopsWithinRadius(latitude, longitude, radius);
        }

        @Test
        @DisplayName("Should handle large radius")
        void shouldHandleLargeRadius() {
            // Given
            Double latitude = 10.8231;
            Double longitude = 106.6297;
            Double radius = 100.0;

            List<Shop> nearbyShops = Arrays.asList(shop);

            when(shopRepository.findShopsWithinRadius(latitude, longitude, radius)).thenReturn(nearbyShops);
            when(shopMapper.toResponseDTO(any(Shop.class))).thenReturn(shopResponseDTO);

            // When
            List<ShopResponseDTO> result = shopService.findShopsWithinRadius(latitude, longitude, radius);

            // Then
            assertThat(result).hasSize(1);
            verify(shopRepository).findShopsWithinRadius(latitude, longitude, radius);
        }

        @Test
        @DisplayName("Should handle zero radius")
        void shouldHandleZeroRadius() {
            // Given
            Double latitude = 10.8231;
            Double longitude = 106.6297;
            Double radius = 0.0;

            when(shopRepository.findShopsWithinRadius(latitude, longitude, radius)).thenReturn(Arrays.asList());

            // When
            List<ShopResponseDTO> result = shopService.findShopsWithinRadius(latitude, longitude, radius);

            // Then
            assertThat(result).isEmpty();
            verify(shopRepository).findShopsWithinRadius(latitude, longitude, radius);
        }
    }
}
