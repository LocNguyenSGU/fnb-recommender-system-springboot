package com.example.demo.service.integration;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.model.MenuItem;
import com.example.demo.repository.MenuItemRepository;
import com.example.demo.service.*;
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
@DisplayName("MenuItemService Integration Tests")
class MenuItemServiceIntegrationTest {

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuService menuService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    private MenuResponseDTO testMenu;
    private ShopResponseDTO testShop;

    @BeforeEach
    void setUp() {
        menuItemRepository.deleteAll();

        // Create test owner
        UserRequestDTO ownerRequest = new UserRequestDTO();
        ownerRequest.setUsername("menuowner");
        ownerRequest.setEmail("menuowner@test.com");
        ownerRequest.setPassword("password");
        ownerRequest.setFullName("Menu Owner");
        UserResponseDTO owner = userService.createUser(ownerRequest);

        // Create test category
        CategoryRequestDTO categoryRequest = new CategoryRequestDTO();
        categoryRequest.setName("Vietnamese");
        categoryRequest.setDescription("Vietnamese cuisine");
        CategoryResponseDTO category = categoryService.createCategory(categoryRequest);

        // Create test shop
        ShopRequestDTO shopRequest = new ShopRequestDTO();
        shopRequest.setName("Test Restaurant");
        shopRequest.setAddress("123 Test St");
        shopRequest.setStatus("ACTIVE");
        shopRequest.setOwnerId(owner.getId());
        shopRequest.setCategoryId(category.getId());
        shopRequest.setLatitude(new java.math.BigDecimal("10.8231"));
        shopRequest.setLongitude(new java.math.BigDecimal("106.6297"));
        testShop = shopService.createShop(shopRequest);

        // Create test menu
        MenuRequestDTO menuRequest = new MenuRequestDTO();
        menuRequest.setName("Main Menu");
        menuRequest.setShopId(testShop.getId());
        testMenu = menuService.createMenu(menuRequest);
    }

    @Nested
    @DisplayName("Create MenuItem Tests")
    class CreateMenuItemTests {

        @Test
        @DisplayName("Should create menu item and persist to database")
        void shouldCreateMenuItemAndPersist() {
            // Given
            MenuItemRequestDTO request = new MenuItemRequestDTO();
            request.setName("Pho Bo");
            request.setDescription("Beef noodle soup");
            request.setPrice(new java.math.BigDecimal("50000.0"));
            request.setIsAvailable(true);
            request.setIsHot(false);
            request.setIsSignature(true);
            request.setMenuId(testMenu.getId());

            // When
            MenuItemResponseDTO response = menuItemService.createMenuItem(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotNull();
            assertThat(response.getName()).isEqualTo("Pho Bo");
            assertThat(response.getPrice()).isEqualTo(new java.math.BigDecimal("50000.0"));
            assertThat(response.getIsAvailable()).isTrue();
            assertThat(response.getIsSignature()).isTrue();

            // Verify in database
            Optional<MenuItem> savedItem = menuItemRepository.findById(response.getId());
            assertThat(savedItem).isPresent();
            assertThat(savedItem.get().getName()).isEqualTo("Pho Bo");
            assertThat(savedItem.get().getMenu().getId()).isEqualTo(testMenu.getId());
        }

        @Test
        @DisplayName("Should create multiple menu items")
        void shouldCreateMultipleMenuItems() {
            // Given
            MenuItemRequestDTO request1 = new MenuItemRequestDTO();
            request1.setName("Item 1");
            request1.setPrice(new java.math.BigDecimal("30000.0"));
            request1.setIsAvailable(true);
            request1.setMenuId(testMenu.getId());

            MenuItemRequestDTO request2 = new MenuItemRequestDTO();
            request2.setName("Item 2");
            request2.setPrice(new java.math.BigDecimal("40000.0"));
            request2.setIsAvailable(true);
            request2.setMenuId(testMenu.getId());

            // When
            MenuItemResponseDTO response1 = menuItemService.createMenuItem(request1);
            MenuItemResponseDTO response2 = menuItemService.createMenuItem(request2);

            // Then
            assertThat(response1.getId()).isNotEqualTo(response2.getId());
            assertThat(menuItemRepository.count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Update MenuItem Tests")
    class UpdateMenuItemTests {

        @Test
        @DisplayName("Should update menu item and persist changes")
        void shouldUpdateMenuItemAndPersist() {
            // Given
            MenuItemRequestDTO createRequest = new MenuItemRequestDTO();
            createRequest.setName("Original Item");
            createRequest.setPrice(new java.math.BigDecimal("50000.0"));
            createRequest.setIsAvailable(true);
            createRequest.setIsHot(false);
            createRequest.setMenuId(testMenu.getId());
            MenuItemResponseDTO created = menuItemService.createMenuItem(createRequest);

            MenuItemRequestDTO updateRequest = new MenuItemRequestDTO();
            updateRequest.setName("Updated Item");
            updateRequest.setPrice(new java.math.BigDecimal("60000.0"));
            updateRequest.setIsAvailable(false);
            updateRequest.setIsHot(true);
            updateRequest.setMenuId(testMenu.getId());

            // When
            MenuItemResponseDTO updated = menuItemService.updateMenuItem(created.getId(), updateRequest);

            // Then
            assertThat(updated.getName()).isEqualTo("Updated Item");
            assertThat(updated.getPrice()).isEqualTo(new java.math.BigDecimal("60000.0"));
            assertThat(updated.getIsAvailable()).isFalse();
            assertThat(updated.getIsHot()).isTrue();

            // Verify in database
            Optional<MenuItem> savedItem = menuItemRepository.findById(created.getId());
            assertThat(savedItem).isPresent();
            assertThat(savedItem.get().getName()).isEqualTo("Updated Item");
            assertThat(savedItem.get().getPrice()).isEqualTo(new java.math.BigDecimal("60000.0"));
        }

        @Test
        @DisplayName("Should update signature and hot flags")
        void shouldUpdateSignatureAndHotFlags() {
            // Given
            MenuItemRequestDTO createRequest = new MenuItemRequestDTO();
            createRequest.setName("Regular Item");
            createRequest.setPrice(new java.math.BigDecimal("35000.0"));
            createRequest.setIsAvailable(true);
            createRequest.setIsHot(false);
            createRequest.setIsSignature(false);
            createRequest.setMenuId(testMenu.getId());
            MenuItemResponseDTO created = menuItemService.createMenuItem(createRequest);

            MenuItemRequestDTO updateRequest = new MenuItemRequestDTO();
            updateRequest.setName("Regular Item");
            updateRequest.setPrice(new java.math.BigDecimal("35000.0"));
            updateRequest.setIsAvailable(true);
            updateRequest.setIsHot(true);
            updateRequest.setIsSignature(true);
            updateRequest.setMenuId(testMenu.getId());

            // When
            MenuItemResponseDTO updated = menuItemService.updateMenuItem(created.getId(), updateRequest);

            // Then
            assertThat(updated.getIsHot()).isTrue();
            assertThat(updated.getIsSignature()).isTrue();
        }
    }

    @Nested
    @DisplayName("Get MenuItem Tests")
    class GetMenuItemTests {

        @Test
        @DisplayName("Should retrieve menu item by ID")
        void shouldRetrieveMenuItemById() {
            // Given
            MenuItemRequestDTO request = new MenuItemRequestDTO();
            request.setName("Banh Mi");
            request.setDescription("Vietnamese sandwich");
            request.setPrice(new java.math.BigDecimal("25000.0"));
            request.setIsAvailable(true);
            request.setMenuId(testMenu.getId());
            MenuItemResponseDTO created = menuItemService.createMenuItem(request);

            // When
            MenuItemResponseDTO retrieved = menuItemService.getMenuItemById(created.getId()).orElseThrow();

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getId()).isEqualTo(created.getId());
            assertThat(retrieved.getName()).isEqualTo("Banh Mi");
        }

        @Test
        @DisplayName("Should retrieve all menu items")
        void shouldRetrieveAllMenuItems() {
            // Given
            MenuItemRequestDTO request1 = new MenuItemRequestDTO();
            request1.setName("Item 1");
            request1.setPrice(new java.math.BigDecimal("30000.0"));
            request1.setIsAvailable(true);
            request1.setMenuId(testMenu.getId());

            MenuItemRequestDTO request2 = new MenuItemRequestDTO();
            request2.setName("Item 2");
            request2.setPrice(new java.math.BigDecimal("40000.0"));
            request2.setIsAvailable(true);
            request2.setMenuId(testMenu.getId());

            menuItemService.createMenuItem(request1);
            menuItemService.createMenuItem(request2);

            // When
            List<MenuItemResponseDTO> items = menuItemService.getAllMenuItems();

            // Then
            assertThat(items).hasSize(2);
        }

        @Test
        @DisplayName("Should retrieve menu items by menu ID")
        void shouldRetrieveMenuItemsByMenuId() {
            // Given
            MenuItemRequestDTO request1 = new MenuItemRequestDTO();
            request1.setName("Menu Item 1");
            request1.setPrice(new java.math.BigDecimal("30000.0"));
            request1.setIsAvailable(true);
            request1.setMenuId(testMenu.getId());

            MenuItemRequestDTO request2 = new MenuItemRequestDTO();
            request2.setName("Menu Item 2");
            request2.setPrice(new java.math.BigDecimal("40000.0"));
            request2.setIsAvailable(true);
            request2.setMenuId(testMenu.getId());

            menuItemService.createMenuItem(request1);
            menuItemService.createMenuItem(request2);

            // When
            List<MenuItemResponseDTO> menuItems = menuItemService.getMenuItemsByMenuId(testMenu.getId());

            // Then
            assertThat(menuItems).hasSize(2);
            assertThat(menuItems)
                    .allMatch(item -> item.getMenuId().equals(testMenu.getId()));
        }
    }

    @Nested
    @DisplayName("Delete MenuItem Tests")
    class DeleteMenuItemTests {

        @Test
        @DisplayName("Should delete menu item from database")
        void shouldDeleteMenuItemFromDatabase() {
            // Given
            MenuItemRequestDTO request = new MenuItemRequestDTO();
            request.setName("To Delete");
            request.setPrice(new java.math.BigDecimal("20000.0"));
            request.setIsAvailable(true);
            request.setMenuId(testMenu.getId());
            MenuItemResponseDTO created = menuItemService.createMenuItem(request);

            // When
            menuItemService.deleteMenuItem(created.getId());

            // Then
            Optional<MenuItem> deleted = menuItemRepository.findById(created.getId());
            assertThat(deleted).isNotPresent();
        }
    }

    @Nested
    @DisplayName("Hot and Signature Items Tests")
    class HotAndSignatureItemsTests {

        @Test
        @DisplayName("Should retrieve hot items")
        void shouldRetrieveHotItems() {
            // Given
            MenuItemRequestDTO hotItem = new MenuItemRequestDTO();
            hotItem.setName("Hot Item");
            hotItem.setPrice(new java.math.BigDecimal("45000.0"));
            hotItem.setIsAvailable(true);
            hotItem.setIsHot(true);
            hotItem.setMenuId(testMenu.getId());

            MenuItemRequestDTO regularItem = new MenuItemRequestDTO();
            regularItem.setName("Regular Item");
            regularItem.setPrice(new java.math.BigDecimal("35000.0"));
            regularItem.setIsAvailable(true);
            regularItem.setIsHot(false);
            regularItem.setMenuId(testMenu.getId());

            menuItemService.createMenuItem(hotItem);
            menuItemService.createMenuItem(regularItem);

            // When
            List<MenuItemResponseDTO> hotItems = menuItemService.getHotMenuItems();

            // Then
            assertThat(hotItems).hasSize(1);
            assertThat(hotItems.get(0).getName()).isEqualTo("Hot Item");
            assertThat(hotItems.get(0).getIsHot()).isTrue();
        }

        @Test
        @DisplayName("Should retrieve signature items")
        void shouldRetrieveSignatureItems() {
            // Given
            MenuItemRequestDTO signatureItem = new MenuItemRequestDTO();
            signatureItem.setName("Signature Dish");
            signatureItem.setPrice(new java.math.BigDecimal("80000.0"));
            signatureItem.setIsAvailable(true);
            signatureItem.setIsSignature(true);
            signatureItem.setMenuId(testMenu.getId());

            MenuItemRequestDTO regularItem = new MenuItemRequestDTO();
            regularItem.setName("Regular Dish");
            regularItem.setPrice(new java.math.BigDecimal("40000.0"));
            regularItem.setIsAvailable(true);
            regularItem.setIsSignature(false);
            regularItem.setMenuId(testMenu.getId());

            menuItemService.createMenuItem(signatureItem);
            menuItemService.createMenuItem(regularItem);

            // When
            List<MenuItemResponseDTO> signatureItems = menuItemService.getSignatureMenuItems();

            // Then
            assertThat(signatureItems).hasSize(1);
            assertThat(signatureItems.get(0).getName()).isEqualTo("Signature Dish");
            assertThat(signatureItems.get(0).getIsSignature()).isTrue();
        }
    }

    @Nested
    @DisplayName("View Count Tests")
    class ViewCountTests {

        @Test
        @DisplayName("Should increment view count")
        void shouldIncrementViewCount() {
            // Given
            MenuItemRequestDTO request = new MenuItemRequestDTO();
            request.setName("Popular Item");
            request.setPrice(new java.math.BigDecimal("45000.0"));
            request.setIsAvailable(true);
            request.setMenuId(testMenu.getId());
            MenuItemResponseDTO created = menuItemService.createMenuItem(request);

            // When
            menuItemService.incrementViewCount(created.getId());
            menuItemService.incrementViewCount(created.getId());
            menuItemService.incrementViewCount(created.getId());

            // Then
            MenuItemResponseDTO retrieved = menuItemService.getMenuItemById(created.getId()).orElseThrow();
            assertThat(retrieved.getViewCount()).isEqualTo(3);

            // Verify in database
            Optional<MenuItem> savedItem = menuItemRepository.findById(created.getId());
            assertThat(savedItem).isPresent();
            assertThat(savedItem.get().getViewCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should start with zero view count")
        void shouldStartWithZeroViewCount() {
            // Given
            MenuItemRequestDTO request = new MenuItemRequestDTO();
            request.setName("New Item");
            request.setPrice(new java.math.BigDecimal("30000.0"));
            request.setIsAvailable(true);
            request.setMenuId(testMenu.getId());

            // When
            MenuItemResponseDTO created = menuItemService.createMenuItem(request);

            // Then
            assertThat(created.getViewCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should search menu items by name")
        void shouldSearchMenuItemsByName() {
            // Given
            MenuItemRequestDTO request1 = new MenuItemRequestDTO();
            request1.setName("Pho Bo");
            request1.setPrice(new java.math.BigDecimal("50000.0"));
            request1.setIsAvailable(true);
            request1.setMenuId(testMenu.getId());

            MenuItemRequestDTO request2 = new MenuItemRequestDTO();
            request2.setName("Pho Ga");
            request2.setPrice(new java.math.BigDecimal("45000.0"));
            request2.setIsAvailable(true);
            request2.setMenuId(testMenu.getId());

            menuItemService.createMenuItem(request1);
            menuItemService.createMenuItem(request2);

            // When
            List<MenuItemResponseDTO> found = menuItemService.searchMenuItemsByName("Pho");

            // Then
            assertThat(found).hasSizeGreaterThanOrEqualTo(2);
            assertThat(found)
                    .extracting(MenuItemResponseDTO::getName)
                    .allMatch(name -> name.contains("Pho"));
        }

        @Test
        @DisplayName("Should handle case-insensitive search")
        void shouldHandleCaseInsensitiveSearch() {
            // Given
            MenuItemRequestDTO request = new MenuItemRequestDTO();
            request.setName("Banh Mi");
            request.setPrice(new java.math.BigDecimal("25000.0"));
            request.setIsAvailable(true);
            request.setMenuId(testMenu.getId());
            menuItemService.createMenuItem(request);

            // When
            List<MenuItemResponseDTO> foundLower = menuItemService.searchMenuItemsByName("banh");
            List<MenuItemResponseDTO> foundUpper = menuItemService.searchMenuItemsByName("BANH");

            // Then
            assertThat(foundLower).isNotEmpty();
            assertThat(foundUpper).isNotEmpty();
            assertThat(foundLower).hasSize(foundUpper.size());
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should maintain menu-menuitem relationship")
        void shouldMaintainMenuMenuItemRelationship() {
            // Given
            MenuItemRequestDTO request = new MenuItemRequestDTO();
            request.setName("Related Item");
            request.setPrice(new java.math.BigDecimal("35000.0"));
            request.setIsAvailable(true);
            request.setMenuId(testMenu.getId());

            // When
            MenuItemResponseDTO created = menuItemService.createMenuItem(request);

            // Then
            Optional<MenuItem> savedItem = menuItemRepository.findById(created.getId());
            assertThat(savedItem).isPresent();
            assertThat(savedItem.get().getMenu()).isNotNull();
            assertThat(savedItem.get().getMenu().getId()).isEqualTo(testMenu.getId());
            assertThat(savedItem.get().getMenu().getName()).isEqualTo(testMenu.getName());
        }

        @Test
        @DisplayName("Should filter menu items by different menus")
        void shouldFilterMenuItemsByDifferentMenus() {
            // Given - Create second menu
            MenuRequestDTO menu2Request = new MenuRequestDTO();
            menu2Request.setName("Second Menu");
            menu2Request.setShopId(testShop.getId());
            MenuResponseDTO menu2 = menuService.createMenu(menu2Request);

            // Create items for both menus
            MenuItemRequestDTO item1Request = new MenuItemRequestDTO();
            item1Request.setName("Menu 1 Item");
            item1Request.setPrice(new java.math.BigDecimal("30000.0"));
            item1Request.setIsAvailable(true);
            item1Request.setMenuId(testMenu.getId());

            MenuItemRequestDTO item2Request = new MenuItemRequestDTO();
            item2Request.setName("Menu 2 Item");
            item2Request.setPrice(new java.math.BigDecimal("40000.0"));
            item2Request.setIsAvailable(true);
            item2Request.setMenuId(menu2.getId());

            menuItemService.createMenuItem(item1Request);
            menuItemService.createMenuItem(item2Request);

            // When
            List<MenuItemResponseDTO> menu1Items = menuItemService.getMenuItemsByMenuId(testMenu.getId());
            List<MenuItemResponseDTO> menu2Items = menuItemService.getMenuItemsByMenuId(menu2.getId());

            // Then
            assertThat(menu1Items).hasSize(1);
            assertThat(menu2Items).hasSize(1);
            assertThat(menu1Items.get(0).getName()).isEqualTo("Menu 1 Item");
            assertThat(menu2Items.get(0).getName()).isEqualTo("Menu 2 Item");
        }
    }
}
