package com.example.demo.service.impl;

import com.example.demo.dto.request.MenuItemRequestDTO;
import com.example.demo.dto.response.MenuItemResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.MenuItemMapper;
import com.example.demo.model.MenuItem;
import com.example.demo.repository.MenuItemRepository;
import com.example.demo.repository.MenuRepository;

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
@DisplayName("MenuItemService Unit Tests")
class MenuItemServiceImplTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuItemMapper menuItemMapper;

    @InjectMocks
    private MenuItemServiceImpl menuItemService;

    private MenuItem menuItem;
    private MenuItemRequestDTO menuItemRequestDTO;
    private MenuItemResponseDTO menuItemResponseDTO;

    @BeforeEach
    void setUp() {
        menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setName("Pho Bo");
        menuItem.setPrice(BigDecimal.valueOf(50000.0));
        menuItem.setIsAvailable(true);
        menuItem.setViewCount(0);
        menuItem.setIsHot(false);
        menuItem.setIsSignature(false);

        menuItemRequestDTO = new MenuItemRequestDTO();
        menuItemRequestDTO.setName("Pho Bo");
        menuItemRequestDTO.setPrice(BigDecimal.valueOf(50000.0));
        menuItemRequestDTO.setMenuId(1L);

        menuItemResponseDTO = new MenuItemResponseDTO();
        menuItemResponseDTO.setId(1L);
        menuItemResponseDTO.setName("Pho Bo");
        menuItemResponseDTO.setPrice(BigDecimal.valueOf(50000.0));
    }

    @Nested
    @DisplayName("Create MenuItem Tests")
    class CreateMenuItemTests {

        @Test
        @DisplayName("Should create menu item successfully")
        void shouldCreateMenuItemSuccessfully() {
            // Given
            when(menuItemMapper.toEntity(any(MenuItemRequestDTO.class), any(MenuRepository.class))).thenReturn(menuItem);
            when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);
            when(menuItemMapper.toResponseDTO(any(MenuItem.class))).thenReturn(menuItemResponseDTO);

            // When
            MenuItemResponseDTO result = menuItemService.createMenuItem(menuItemRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Pho Bo");

            verify(menuItemMapper).toEntity(menuItemRequestDTO, menuRepository);
            verify(menuItemRepository).save(menuItem);
            verify(menuItemMapper).toResponseDTO(menuItem);
        }
    }

    @Nested
    @DisplayName("Update MenuItem Tests")
    class UpdateMenuItemTests {

        @Test
        @DisplayName("Should update menu item successfully")
        void shouldUpdateMenuItemSuccessfully() {
            // Given
            Long menuItemId = 1L;
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
            doNothing().when(menuItemMapper).updateEntityFromDTO(any(MenuItem.class), any(MenuItemRequestDTO.class), any(MenuRepository.class));
            when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);
            when(menuItemMapper.toResponseDTO(any(MenuItem.class))).thenReturn(menuItemResponseDTO);

            // When
            MenuItemResponseDTO result = menuItemService.updateMenuItem(menuItemId, menuItemRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(menuItemId);

            verify(menuItemRepository).findById(menuItemId);
            verify(menuItemMapper).updateEntityFromDTO(menuItem, menuItemRequestDTO, menuRepository);
            verify(menuItemRepository).save(menuItem);
        }

        @Test
        @DisplayName("Should throw exception when menu item not found")
        void shouldThrowExceptionWhenMenuItemNotFound() {
            // Given
            Long menuItemId = 999L;
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> menuItemService.updateMenuItem(menuItemId, menuItemRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("MenuItem")
                    .hasMessageContaining("id");

            verify(menuItemRepository).findById(menuItemId);
            verify(menuItemRepository, never()).save(any(MenuItem.class));
        }
    }

    @Nested
    @DisplayName("Delete MenuItem Tests")
    class DeleteMenuItemTests {

        @Test
        @DisplayName("Should delete menu item successfully")
        void shouldDeleteMenuItemSuccessfully() {
            // Given
            Long menuItemId = 1L;
            doNothing().when(menuItemRepository).deleteById(menuItemId);

            // When
            menuItemService.deleteMenuItem(menuItemId);

            // Then
            verify(menuItemRepository).deleteById(menuItemId);
        }
    }

    @Nested
    @DisplayName("Get MenuItem Tests")
    class GetMenuItemTests {

        @Test
        @DisplayName("Should get menu item by id successfully")
        void shouldGetMenuItemByIdSuccessfully() {
            // Given
            Long menuItemId = 1L;
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
            when(menuItemMapper.toResponseDTO(any(MenuItem.class))).thenReturn(menuItemResponseDTO);

            // When
            Optional<MenuItemResponseDTO> result = menuItemService.getMenuItemById(menuItemId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(menuItemId);

            verify(menuItemRepository).findById(menuItemId);
            verify(menuItemMapper).toResponseDTO(menuItem);
        }

        @Test
        @DisplayName("Should return empty when menu item not found")
        void shouldReturnEmptyWhenMenuItemNotFound() {
            // Given
            Long menuItemId = 999L;
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.empty());

            // When
            Optional<MenuItemResponseDTO> result = menuItemService.getMenuItemById(menuItemId);

            // Then
            assertThat(result).isEmpty();
            verify(menuItemRepository).findById(menuItemId);
            verify(menuItemMapper, never()).toResponseDTO(any(MenuItem.class));
        }

        @Test
        @DisplayName("Should get all menu items successfully")
        void shouldGetAllMenuItemsSuccessfully() {
            // Given
            MenuItem menuItem2 = new MenuItem();
            menuItem2.setId(2L);
            menuItem2.setName("Bun Cha");

            MenuItemResponseDTO menuItemResponseDTO2 = new MenuItemResponseDTO();
            menuItemResponseDTO2.setId(2L);
            menuItemResponseDTO2.setName("Bun Cha");

            List<MenuItem> menuItems = Arrays.asList(menuItem, menuItem2);

            when(menuItemRepository.findAll()).thenReturn(menuItems);
            when(menuItemMapper.toResponseDTO(menuItem)).thenReturn(menuItemResponseDTO);
            when(menuItemMapper.toResponseDTO(menuItem2)).thenReturn(menuItemResponseDTO2);

            // When
            List<MenuItemResponseDTO> result = menuItemService.getAllMenuItems();

            // Then
            assertThat(result).hasSize(2);
            verify(menuItemRepository).findAll();
            verify(menuItemMapper, times(2)).toResponseDTO(any(MenuItem.class));
        }
    }

    @Nested
    @DisplayName("Get MenuItems By Menu Tests")
    class GetMenuItemsByMenuTests {

        @Test
        @DisplayName("Should get menu items by menu id successfully")
        void shouldGetMenuItemsByMenuIdSuccessfully() {
            // Given
            Long menuId = 1L;
            List<MenuItem> menuItems = Arrays.asList(menuItem);

            when(menuItemRepository.findByMenuId(menuId)).thenReturn(menuItems);
            when(menuItemMapper.toResponseDTO(any(MenuItem.class))).thenReturn(menuItemResponseDTO);

            // When
            List<MenuItemResponseDTO> result = menuItemService.getMenuItemsByMenuId(menuId);

            // Then
            assertThat(result).hasSize(1);
            verify(menuItemRepository).findByMenuId(menuId);
        }

        @Test
        @DisplayName("Should get available menu items by menu id successfully")
        void shouldGetAvailableMenuItemsByMenuIdSuccessfully() {
            // Given
            Long menuId = 1L;
            menuItem.setIsAvailable(true);
            List<MenuItem> availableItems = Arrays.asList(menuItem);

            when(menuItemRepository.findAvailableItemsByMenuId(menuId)).thenReturn(availableItems);
            when(menuItemMapper.toResponseDTO(any(MenuItem.class))).thenReturn(menuItemResponseDTO);

            // When
            List<MenuItemResponseDTO> result = menuItemService.getAvailableMenuItemsByMenuId(menuId);

            // Then
            assertThat(result).hasSize(1);
            verify(menuItemRepository).findAvailableItemsByMenuId(menuId);
        }
    }

    @Nested
    @DisplayName("Get Special MenuItems Tests")
    class GetSpecialMenuItemsTests {

        @Test
        @DisplayName("Should get hot menu items successfully")
        void shouldGetHotMenuItemsSuccessfully() {
            // Given
            menuItem.setIsHot(true);
            List<MenuItem> hotItems = Arrays.asList(menuItem);

            when(menuItemRepository.findByIsHot(true)).thenReturn(hotItems);
            when(menuItemMapper.toResponseDTO(any(MenuItem.class))).thenReturn(menuItemResponseDTO);

            // When
            List<MenuItemResponseDTO> result = menuItemService.getHotMenuItems();

            // Then
            assertThat(result).hasSize(1);
            verify(menuItemRepository).findByIsHot(true);
        }

        @Test
        @DisplayName("Should get signature menu items successfully")
        void shouldGetSignatureMenuItemsSuccessfully() {
            // Given
            menuItem.setIsSignature(true);
            List<MenuItem> signatureItems = Arrays.asList(menuItem);

            when(menuItemRepository.findByIsSignature(true)).thenReturn(signatureItems);
            when(menuItemMapper.toResponseDTO(any(MenuItem.class))).thenReturn(menuItemResponseDTO);

            // When
            List<MenuItemResponseDTO> result = menuItemService.getSignatureMenuItems();

            // Then
            assertThat(result).hasSize(1);
            verify(menuItemRepository).findByIsSignature(true);
        }
    }

    @Nested
    @DisplayName("Search MenuItems Tests")
    class SearchMenuItemsTests {

        @Test
        @DisplayName("Should search menu items by name successfully")
        void shouldSearchMenuItemsByNameSuccessfully() {
            // Given
            String name = "Pho";
            List<MenuItem> foundItems = Arrays.asList(menuItem);

            when(menuItemRepository.findByNameContainingIgnoreCase(name)).thenReturn(foundItems);
            when(menuItemMapper.toResponseDTO(any(MenuItem.class))).thenReturn(menuItemResponseDTO);

            // When
            List<MenuItemResponseDTO> result = menuItemService.searchMenuItemsByName(name);

            // Then
            assertThat(result).hasSize(1);
            verify(menuItemRepository).findByNameContainingIgnoreCase(name);
        }

        @Test
        @DisplayName("Should return empty list when no items match name")
        void shouldReturnEmptyListWhenNoItemsMatchName() {
            // Given
            String name = "NonExistent";
            when(menuItemRepository.findByNameContainingIgnoreCase(name)).thenReturn(Arrays.asList());

            // When
            List<MenuItemResponseDTO> result = menuItemService.searchMenuItemsByName(name);

            // Then
            assertThat(result).isEmpty();
            verify(menuItemRepository).findByNameContainingIgnoreCase(name);
        }
    }

    @Nested
    @DisplayName("Get Top Viewed MenuItems Tests")
    class GetTopViewedMenuItemsTests {

        @Test
        @DisplayName("Should get top viewed menu items successfully")
        void shouldGetTopViewedMenuItemsSuccessfully() {
            // Given
            menuItem.setViewCount(100);
            List<MenuItem> topItems = Arrays.asList(menuItem);

            when(menuItemRepository.findTopViewedItems()).thenReturn(topItems);
            when(menuItemMapper.toResponseDTO(any(MenuItem.class))).thenReturn(menuItemResponseDTO);

            // When
            List<MenuItemResponseDTO> result = menuItemService.getTopViewedMenuItems();

            // Then
            assertThat(result).hasSize(1);
            verify(menuItemRepository).findTopViewedItems();
        }
    }

    @Nested
    @DisplayName("Increment View Count Tests")
    class IncrementViewCountTests {

        @Test
        @DisplayName("Should increment view count successfully")
        void shouldIncrementViewCountSuccessfully() {
            // Given
            Long menuItemId = 1L;
            menuItem.setViewCount(5);

            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
            when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

            // When
            menuItemService.incrementViewCount(menuItemId);

            // Then
            assertThat(menuItem.getViewCount()).isEqualTo(6);
            verify(menuItemRepository).findById(menuItemId);
            verify(menuItemRepository).save(menuItem);
        }

        @Test
        @DisplayName("Should throw exception when menu item not found")
        void shouldThrowExceptionWhenMenuItemNotFound() {
            // Given
            Long menuItemId = 999L;
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> menuItemService.incrementViewCount(menuItemId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("MenuItem");

            verify(menuItemRepository).findById(menuItemId);
            verify(menuItemRepository, never()).save(any(MenuItem.class));
        }

        @Test
        @DisplayName("Should handle zero view count")
        void shouldHandleZeroViewCount() {
            // Given
            Long menuItemId = 1L;
            menuItem.setViewCount(0);

            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
            when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

            // When
            menuItemService.incrementViewCount(menuItemId);

            // Then
            assertThat(menuItem.getViewCount()).isEqualTo(1);
            verify(menuItemRepository).save(menuItem);
        }

        @Test
        @DisplayName("Should handle large view count")
        void shouldHandleLargeViewCount() {
            // Given
            Long menuItemId = 1L;
            menuItem.setViewCount(999999);

            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
            when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

            // When
            menuItemService.incrementViewCount(menuItemId);

            // Then
            assertThat(menuItem.getViewCount()).isEqualTo(1000000);
            verify(menuItemRepository).save(menuItem);
        }
    }
}
