package com.example.demo.service.impl;

import com.example.demo.dto.request.MenuRequestDTO;
import com.example.demo.dto.response.MenuResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.MenuMapper;
import com.example.demo.model.Menu;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.ShopRepository;
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
@DisplayName("MenuService Unit Tests")
class MenuServiceImplTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private MenuMapper menuMapper;

    @InjectMocks
    private MenuServiceImpl menuService;

    private Menu menu;
    private MenuRequestDTO menuRequestDTO;
    private MenuResponseDTO menuResponseDTO;

    @BeforeEach
    void setUp() {
        menu = new Menu();
        menu.setId(1L);
        menu.setName("Main Menu");

        menuRequestDTO = new MenuRequestDTO();
        menuRequestDTO.setName("Main Menu");
        menuRequestDTO.setShopId(1L);

        menuResponseDTO = new MenuResponseDTO();
        menuResponseDTO.setId(1L);
        menuResponseDTO.setName("Main Menu");
    }

    @Nested
    @DisplayName("Create Menu Tests")
    class CreateMenuTests {

        @Test
        @DisplayName("Should create menu successfully")
        void shouldCreateMenuSuccessfully() {
            // Given
            when(menuMapper.toEntity(any(MenuRequestDTO.class), any(ShopRepository.class))).thenReturn(menu);
            when(menuRepository.save(any(Menu.class))).thenReturn(menu);
            when(menuMapper.toResponseDTO(any(Menu.class))).thenReturn(menuResponseDTO);

            // When
            MenuResponseDTO result = menuService.createMenu(menuRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Main Menu");

            verify(menuMapper).toEntity(menuRequestDTO, shopRepository);
            verify(menuRepository).save(menu);
            verify(menuMapper).toResponseDTO(menu);
        }

        @Test
        @DisplayName("Should create menu with minimal data")
        void shouldCreateMenuWithMinimalData() {
            // Given
            MenuRequestDTO minimalRequest = new MenuRequestDTO();
            minimalRequest.setName("Breakfast Menu");
            minimalRequest.setShopId(1L);

            when(menuMapper.toEntity(any(MenuRequestDTO.class), any(ShopRepository.class))).thenReturn(menu);
            when(menuRepository.save(any(Menu.class))).thenReturn(menu);
            when(menuMapper.toResponseDTO(any(Menu.class))).thenReturn(menuResponseDTO);

            // When
            MenuResponseDTO result = menuService.createMenu(minimalRequest);

            // Then
            assertThat(result).isNotNull();
            verify(menuRepository).save(any(Menu.class));
        }
    }

    @Nested
    @DisplayName("Update Menu Tests")
    class UpdateMenuTests {

        @Test
        @DisplayName("Should update menu successfully")
        void shouldUpdateMenuSuccessfully() {
            // Given
            Long menuId = 1L;
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
            doNothing().when(menuMapper).updateEntityFromDTO(any(Menu.class), any(MenuRequestDTO.class), any(ShopRepository.class));
            when(menuRepository.save(any(Menu.class))).thenReturn(menu);
            when(menuMapper.toResponseDTO(any(Menu.class))).thenReturn(menuResponseDTO);

            // When
            MenuResponseDTO result = menuService.updateMenu(menuId, menuRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(menuId);

            verify(menuRepository).findById(menuId);
            verify(menuMapper).updateEntityFromDTO(menu, menuRequestDTO, shopRepository);
            verify(menuRepository).save(menu);
        }

        @Test
        @DisplayName("Should throw exception when menu not found")
        void shouldThrowExceptionWhenMenuNotFound() {
            // Given
            Long menuId = 999L;
            when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> menuService.updateMenu(menuId, menuRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Menu")
                    .hasMessageContaining("id");

            verify(menuRepository).findById(menuId);
            verify(menuRepository, never()).save(any(Menu.class));
        }

        @Test
        @DisplayName("Should update menu name only")
        void shouldUpdateMenuNameOnly() {
            // Given
            Long menuId = 1L;
            MenuRequestDTO nameUpdate = new MenuRequestDTO();
            nameUpdate.setName("Updated Menu");

            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
            doNothing().when(menuMapper).updateEntityFromDTO(any(Menu.class), any(MenuRequestDTO.class), any(ShopRepository.class));
            when(menuRepository.save(any(Menu.class))).thenReturn(menu);
            when(menuMapper.toResponseDTO(any(Menu.class))).thenReturn(menuResponseDTO);

            // When
            MenuResponseDTO result = menuService.updateMenu(menuId, nameUpdate);

            // Then
            assertThat(result).isNotNull();
            verify(menuMapper).updateEntityFromDTO(menu, nameUpdate, shopRepository);
        }
    }

    @Nested
    @DisplayName("Delete Menu Tests")
    class DeleteMenuTests {

        @Test
        @DisplayName("Should delete menu successfully")
        void shouldDeleteMenuSuccessfully() {
            // Given
            Long menuId = 1L;
            doNothing().when(menuRepository).deleteById(menuId);

            // When
            menuService.deleteMenu(menuId);

            // Then
            verify(menuRepository).deleteById(menuId);
        }

        @Test
        @DisplayName("Should not throw exception when deleting non-existent menu")
        void shouldNotThrowExceptionWhenDeletingNonExistentMenu() {
            // Given
            Long menuId = 999L;
            doNothing().when(menuRepository).deleteById(menuId);

            // When
            menuService.deleteMenu(menuId);

            // Then
            verify(menuRepository).deleteById(menuId);
        }
    }

    @Nested
    @DisplayName("Get Menu Tests")
    class GetMenuTests {

        @Test
        @DisplayName("Should get menu by id successfully")
        void shouldGetMenuByIdSuccessfully() {
            // Given
            Long menuId = 1L;
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
            when(menuMapper.toResponseDTO(any(Menu.class))).thenReturn(menuResponseDTO);

            // When
            Optional<MenuResponseDTO> result = menuService.getMenuById(menuId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(menuId);

            verify(menuRepository).findById(menuId);
            verify(menuMapper).toResponseDTO(menu);
        }

        @Test
        @DisplayName("Should return empty when menu not found by id")
        void shouldReturnEmptyWhenMenuNotFoundById() {
            // Given
            Long menuId = 999L;
            when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

            // When
            Optional<MenuResponseDTO> result = menuService.getMenuById(menuId);

            // Then
            assertThat(result).isEmpty();
            verify(menuRepository).findById(menuId);
            verify(menuMapper, never()).toResponseDTO(any(Menu.class));
        }

        @Test
        @DisplayName("Should get all menus successfully")
        void shouldGetAllMenusSuccessfully() {
            // Given
            Menu menu2 = new Menu();
            menu2.setId(2L);
            menu2.setName("Lunch Menu");

            MenuResponseDTO menuResponseDTO2 = new MenuResponseDTO();
            menuResponseDTO2.setId(2L);
            menuResponseDTO2.setName("Lunch Menu");

            List<Menu> menus = Arrays.asList(menu, menu2);

            when(menuRepository.findAll()).thenReturn(menus);
            when(menuMapper.toResponseDTO(menu)).thenReturn(menuResponseDTO);
            when(menuMapper.toResponseDTO(menu2)).thenReturn(menuResponseDTO2);

            // When
            List<MenuResponseDTO> result = menuService.getAllMenus();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Main Menu");
            assertThat(result.get(1).getName()).isEqualTo("Lunch Menu");

            verify(menuRepository).findAll();
            verify(menuMapper, times(2)).toResponseDTO(any(Menu.class));
        }

        @Test
        @DisplayName("Should return empty list when no menus exist")
        void shouldReturnEmptyListWhenNoMenusExist() {
            // Given
            when(menuRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<MenuResponseDTO> result = menuService.getAllMenus();

            // Then
            assertThat(result).isEmpty();
            verify(menuRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Get Menus By Shop Tests")
    class GetMenusByShopTests {

        @Test
        @DisplayName("Should get menus by shop id successfully")
        void shouldGetMenusByShopIdSuccessfully() {
            // Given
            Long shopId = 1L;
            List<Menu> shopMenus = Arrays.asList(menu);

            when(menuRepository.findByShopId(shopId)).thenReturn(shopMenus);
            when(menuMapper.toResponseDTO(any(Menu.class))).thenReturn(menuResponseDTO);

            // When
            List<MenuResponseDTO> result = menuService.getMenusByShopId(shopId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Main Menu");

            verify(menuRepository).findByShopId(shopId);
            verify(menuMapper).toResponseDTO(menu);
        }

        @Test
        @DisplayName("Should return empty list when shop has no menus")
        void shouldReturnEmptyListWhenShopHasNoMenus() {
            // Given
            Long shopId = 999L;
            when(menuRepository.findByShopId(shopId)).thenReturn(Arrays.asList());

            // When
            List<MenuResponseDTO> result = menuService.getMenusByShopId(shopId);

            // Then
            assertThat(result).isEmpty();
            verify(menuRepository).findByShopId(shopId);
        }

        @Test
        @DisplayName("Should handle shop with multiple menus")
        void shouldHandleShopWithMultipleMenus() {
            // Given
            Long shopId = 1L;
            Menu menu2 = new Menu();
            menu2.setId(2L);
            menu2.setName("Dinner Menu");

            Menu menu3 = new Menu();
            menu3.setId(3L);
            menu3.setName("Drinks Menu");

            List<Menu> shopMenus = Arrays.asList(menu, menu2, menu3);

            when(menuRepository.findByShopId(shopId)).thenReturn(shopMenus);
            when(menuMapper.toResponseDTO(any(Menu.class))).thenReturn(menuResponseDTO);

            // When
            List<MenuResponseDTO> result = menuService.getMenusByShopId(shopId);

            // Then
            assertThat(result).hasSize(3);
            verify(menuRepository).findByShopId(shopId);
            verify(menuMapper, times(3)).toResponseDTO(any(Menu.class));
        }
    }

    @Nested
    @DisplayName("Search Menus Tests")
    class SearchMenusTests {

        @Test
        @DisplayName("Should search menus by name successfully")
        void shouldSearchMenusByNameSuccessfully() {
            // Given
            String name = "Main";
            List<Menu> foundMenus = Arrays.asList(menu);

            when(menuRepository.findByNameContainingIgnoreCase(name)).thenReturn(foundMenus);
            when(menuMapper.toResponseDTO(any(Menu.class))).thenReturn(menuResponseDTO);

            // When
            List<MenuResponseDTO> result = menuService.searchMenusByName(name);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains("Main");

            verify(menuRepository).findByNameContainingIgnoreCase(name);
        }

        @Test
        @DisplayName("Should return empty list when no menus match name")
        void shouldReturnEmptyListWhenNoMenusMatchName() {
            // Given
            String name = "NonExistent";
            when(menuRepository.findByNameContainingIgnoreCase(name)).thenReturn(Arrays.asList());

            // When
            List<MenuResponseDTO> result = menuService.searchMenusByName(name);

            // Then
            assertThat(result).isEmpty();
            verify(menuRepository).findByNameContainingIgnoreCase(name);
        }

        @Test
        @DisplayName("Should handle case-insensitive search")
        void shouldHandleCaseInsensitiveSearch() {
            // Given
            String name = "MAIN";
            List<Menu> foundMenus = Arrays.asList(menu);

            when(menuRepository.findByNameContainingIgnoreCase(name)).thenReturn(foundMenus);
            when(menuMapper.toResponseDTO(any(Menu.class))).thenReturn(menuResponseDTO);

            // When
            List<MenuResponseDTO> result = menuService.searchMenusByName(name);

            // Then
            assertThat(result).hasSize(1);
            verify(menuRepository).findByNameContainingIgnoreCase(name);
        }

        @Test
        @DisplayName("Should handle partial name search")
        void shouldHandlePartialNameSearch() {
            // Given
            String name = "Mai";
            List<Menu> foundMenus = Arrays.asList(menu);

            when(menuRepository.findByNameContainingIgnoreCase(name)).thenReturn(foundMenus);
            when(menuMapper.toResponseDTO(any(Menu.class))).thenReturn(menuResponseDTO);

            // When
            List<MenuResponseDTO> result = menuService.searchMenusByName(name);

            // Then
            assertThat(result).hasSize(1);
            verify(menuRepository).findByNameContainingIgnoreCase(name);
        }

        @Test
        @DisplayName("Should handle empty search string")
        void shouldHandleEmptySearchString() {
            // Given
            String name = "";
            when(menuRepository.findByNameContainingIgnoreCase(name)).thenReturn(Arrays.asList(menu));
            when(menuMapper.toResponseDTO(any(Menu.class))).thenReturn(menuResponseDTO);

            // When
            List<MenuResponseDTO> result = menuService.searchMenusByName(name);

            // Then
            assertThat(result).hasSize(1);
            verify(menuRepository).findByNameContainingIgnoreCase(name);
        }
    }
}
