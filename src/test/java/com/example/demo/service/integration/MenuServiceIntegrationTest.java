package com.example.demo.service.integration;

import com.example.demo.dto.request.CategoryRequestDTO;
import com.example.demo.dto.request.MenuRequestDTO;
import com.example.demo.dto.request.ShopRequestDTO;
import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.CategoryResponseDTO;
import com.example.demo.dto.response.MenuResponseDTO;
import com.example.demo.dto.response.ShopResponseDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.model.Menu;
import com.example.demo.repository.MenuRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.MenuService;
import com.example.demo.service.ShopService;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("MenuService Integration Tests")
class MenuServiceIntegrationTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    private ShopResponseDTO testShop;
    private UserResponseDTO testOwner;
    private CategoryResponseDTO testCategory;

    @BeforeEach
    void setUp() {
        menuRepository.deleteAll();

        // Create test owner
        UserRequestDTO ownerRequest = new UserRequestDTO();
        ownerRequest.setUsername("shopowner");
        ownerRequest.setEmail("owner@test.com");
        ownerRequest.setPassword("password");
        ownerRequest.setFullName("Shop Owner");
        testOwner = userService.createUser(ownerRequest);

        // Create test category
        CategoryRequestDTO categoryRequest = new CategoryRequestDTO();
        categoryRequest.setName("Vietnamese");
        categoryRequest.setDescription("Vietnamese cuisine");
        testCategory = categoryService.createCategory(categoryRequest);

        // Create test shop
        ShopRequestDTO shopRequest = new ShopRequestDTO();
        shopRequest.setName("Test Restaurant");
        shopRequest.setAddress("123 Test St");
        shopRequest.setStatus("ACTIVE");
        shopRequest.setOwnerId(testOwner.getId());
        shopRequest.setCategoryId(testCategory.getId());
        shopRequest.setLatitude(new java.math.BigDecimal("10.8231"));
        shopRequest.setLongitude(new java.math.BigDecimal("106.6297"));
        testShop = shopService.createShop(shopRequest);
    }

    @Nested
    @DisplayName("Create Menu Tests")
    class CreateMenuTests {

        @Test
        @DisplayName("Should create menu and persist to database")
        void shouldCreateMenuAndPersist() {
            // Given
            MenuRequestDTO request = new MenuRequestDTO();
            request.setName("Lunch Menu");
            request.setShopId(testShop.getId());

            // When
            MenuResponseDTO response = menuService.createMenu(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotNull();
            assertThat(response.getName()).isEqualTo("Lunch Menu");

            // Verify in database
            Optional<Menu> savedMenu = menuRepository.findById(response.getId());
            assertThat(savedMenu).isPresent();
            assertThat(savedMenu.get().getName()).isEqualTo("Lunch Menu");
            assertThat(savedMenu.get().getShop().getId()).isEqualTo(testShop.getId());
        }

        @Test
        @DisplayName("Should create multiple menus for same shop")
        void shouldCreateMultipleMenusForSameShop() {
            // Given
            MenuRequestDTO request1 = new MenuRequestDTO();
            request1.setName("Breakfast Menu");
            request1.setShopId(testShop.getId());

            MenuRequestDTO request2 = new MenuRequestDTO();
            request2.setName("Dinner Menu");
            request2.setShopId(testShop.getId());

            // When
            MenuResponseDTO response1 = menuService.createMenu(request1);
            MenuResponseDTO response2 = menuService.createMenu(request2);

            // Then
            assertThat(response1.getId()).isNotEqualTo(response2.getId());

            List<MenuResponseDTO> shopMenus = menuService.getMenusByShopId(testShop.getId());
            assertThat(shopMenus).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Update Menu Tests")
    class UpdateMenuTests {

        @Test
        @DisplayName("Should update menu and persist changes")
        void shouldUpdateMenuAndPersist() {
            // Given
            MenuRequestDTO createRequest = new MenuRequestDTO();
            createRequest.setName("Original Menu");
            createRequest.setShopId(testShop.getId());
            MenuResponseDTO created = menuService.createMenu(createRequest);

            MenuRequestDTO updateRequest = new MenuRequestDTO();
            updateRequest.setName("Updated Menu");
            updateRequest.setShopId(testShop.getId());

            // When
            MenuResponseDTO updated = menuService.updateMenu(created.getId(), updateRequest);

            // Then
            assertThat(updated.getName()).isEqualTo("Updated Menu");

            // Verify in database
            Optional<Menu> savedMenu = menuRepository.findById(created.getId());
            assertThat(savedMenu).isPresent();
            assertThat(savedMenu.get().getName()).isEqualTo("Updated Menu");
        }
    }

    @Nested
    @DisplayName("Get Menu Tests")
    class GetMenuTests {

        @Test
        @DisplayName("Should retrieve menu by ID")
        void shouldRetrieveMenuById() {
            // Given
            MenuRequestDTO request = new MenuRequestDTO();
            request.setName("Weekday Menu");
            request.setShopId(testShop.getId());
            MenuResponseDTO created = menuService.createMenu(request);

            // When
            MenuResponseDTO retrieved = menuService.getMenuById(created.getId()).orElseThrow();

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getId()).isEqualTo(created.getId());
            assertThat(retrieved.getName()).isEqualTo("Weekday Menu");
        }

        @Test
        @DisplayName("Should retrieve all menus")
        void shouldRetrieveAllMenus() {
            // Given
            MenuRequestDTO request1 = new MenuRequestDTO();
            request1.setName("Menu 1");
            request1.setShopId(testShop.getId());

            MenuRequestDTO request2 = new MenuRequestDTO();
            request2.setName("Menu 2");
            request2.setShopId(testShop.getId());

            menuService.createMenu(request1);
            menuService.createMenu(request2);

            // When
            List<MenuResponseDTO> menus = menuService.getAllMenus();

            // Then
            assertThat(menus).hasSize(2);
            assertThat(menus)
                    .extracting(MenuResponseDTO::getName)
                    .containsExactlyInAnyOrder("Menu 1", "Menu 2");
        }

        @Test
        @DisplayName("Should retrieve menus by shop ID")
        void shouldRetrieveMenusByShopId() {
            // Given
            MenuRequestDTO request1 = new MenuRequestDTO();
            request1.setName("Shop Menu 1");
            request1.setShopId(testShop.getId());

            MenuRequestDTO request2 = new MenuRequestDTO();
            request2.setName("Shop Menu 2");
            request2.setShopId(testShop.getId());

            menuService.createMenu(request1);
            menuService.createMenu(request2);

            // When
            List<MenuResponseDTO> shopMenus = menuService.getMenusByShopId(testShop.getId());

            // Then
            assertThat(shopMenus).hasSize(2);
            assertThat(shopMenus)
                    .allMatch(menu -> menu.getShopId().equals(testShop.getId()));
        }
    }

    @Nested
    @DisplayName("Delete Menu Tests")
    class DeleteMenuTests {

        @Test
        @DisplayName("Should delete menu from database")
        void shouldDeleteMenuFromDatabase() {
            // Given
            MenuRequestDTO request = new MenuRequestDTO();
            request.setName("To Delete Menu");
            request.setShopId(testShop.getId());
            MenuResponseDTO created = menuService.createMenu(request);

            // When
            menuService.deleteMenu(created.getId());

            // Then
            Optional<Menu> deleted = menuRepository.findById(created.getId());
            assertThat(deleted).isNotPresent();
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should search menus by name - exact match")
        void shouldSearchMenusByNameExactMatch() {
            // Given
            MenuRequestDTO request = new MenuRequestDTO();
            request.setName("Special Menu");
            request.setShopId(testShop.getId());
            menuService.createMenu(request);

            // When
            List<MenuResponseDTO> found = menuService.searchMenusByName("Special Menu");

            // Then
            assertThat(found).hasSize(1);
            assertThat(found.get(0).getName()).isEqualTo("Special Menu");
        }

        @Test
        @DisplayName("Should search menus by name - partial match")
        void shouldSearchMenusByNamePartialMatch() {
            // Given
            MenuRequestDTO request1 = new MenuRequestDTO();
            request1.setName("Vegetarian Special");
            request1.setShopId(testShop.getId());

            MenuRequestDTO request2 = new MenuRequestDTO();
            request2.setName("Vegan Special");
            request2.setShopId(testShop.getId());

            menuService.createMenu(request1);
            menuService.createMenu(request2);

            // When
            List<MenuResponseDTO> found = menuService.searchMenusByName("Special");

            // Then
            assertThat(found).hasSizeGreaterThanOrEqualTo(2);
            assertThat(found)
                    .extracting(MenuResponseDTO::getName)
                    .allMatch(name -> name.contains("Special"));
        }

        @Test
        @DisplayName("Should handle case-insensitive search")
        void shouldHandleCaseInsensitiveSearch() {
            // Given
            MenuRequestDTO request = new MenuRequestDTO();
            request.setName("Premium Menu");
            request.setShopId(testShop.getId());
            menuService.createMenu(request);

            // When
            List<MenuResponseDTO> foundLower = menuService.searchMenusByName("premium");
            List<MenuResponseDTO> foundUpper = menuService.searchMenusByName("PREMIUM");

            // Then
            assertThat(foundLower).isNotEmpty();
            assertThat(foundUpper).isNotEmpty();
            assertThat(foundLower).hasSize(foundUpper.size());
        }

        @Test
        @DisplayName("Should return empty list when no match found")
        void shouldReturnEmptyListWhenNoMatchFound() {
            // Given
            MenuRequestDTO request = new MenuRequestDTO();
            request.setName("Regular Menu");
            request.setShopId(testShop.getId());
            menuService.createMenu(request);

            // When
            List<MenuResponseDTO> found = menuService.searchMenusByName("NonExistent");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should maintain shop-menu relationship")
        void shouldMaintainShopMenuRelationship() {
            // Given
            MenuRequestDTO request = new MenuRequestDTO();
            request.setName("Relationship Menu");
            request.setShopId(testShop.getId());

            // When
            MenuResponseDTO created = menuService.createMenu(request);

            // Then
            Optional<Menu> savedMenu = menuRepository.findById(created.getId());
            assertThat(savedMenu).isPresent();
            assertThat(savedMenu.get().getShop()).isNotNull();
            assertThat(savedMenu.get().getShop().getId()).isEqualTo(testShop.getId());
            assertThat(savedMenu.get().getShop().getName()).isEqualTo(testShop.getName());
        }

        @Test
        @DisplayName("Should filter menus by different shops")
        void shouldFilterMenusByDifferentShops() {
            // Given - Create second shop
            UserRequestDTO owner2Request = new UserRequestDTO();
            owner2Request.setUsername("owner2");
            owner2Request.setEmail("owner2@test.com");
            owner2Request.setPassword("password");
            owner2Request.setFullName("Owner 2");
            UserResponseDTO owner2 = userService.createUser(owner2Request);

            ShopRequestDTO shop2Request = new ShopRequestDTO();
            shop2Request.setName("Second Restaurant");
            shop2Request.setAddress("456 Test St");
            shop2Request.setStatus("ACTIVE");
            shop2Request.setOwnerId(owner2.getId());
            shop2Request.setCategoryId(testCategory.getId());
            shop2Request.setLatitude(new java.math.BigDecimal("10.8231"));
            shop2Request.setLongitude(new java.math.BigDecimal("106.6297"));
            ShopResponseDTO shop2 = shopService.createShop(shop2Request);

            // Create menus for both shops
            MenuRequestDTO menu1Request = new MenuRequestDTO();
            menu1Request.setName("Shop 1 Menu");
            menu1Request.setShopId(testShop.getId());

            MenuRequestDTO menu2Request = new MenuRequestDTO();
            menu2Request.setName("Shop 2 Menu");
            menu2Request.setShopId(shop2.getId());

            menuService.createMenu(menu1Request);
            menuService.createMenu(menu2Request);

            // When
            List<MenuResponseDTO> shop1Menus = menuService.getMenusByShopId(testShop.getId());
            List<MenuResponseDTO> shop2Menus = menuService.getMenusByShopId(shop2.getId());

            // Then
            assertThat(shop1Menus).hasSize(1);
            assertThat(shop2Menus).hasSize(1);
            assertThat(shop1Menus.get(0).getName()).isEqualTo("Shop 1 Menu");
            assertThat(shop2Menus.get(0).getName()).isEqualTo("Shop 2 Menu");
        }
    }
}
