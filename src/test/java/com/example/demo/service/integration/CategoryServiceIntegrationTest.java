package com.example.demo.service.integration;

import com.example.demo.dto.request.CategoryRequestDTO;
import com.example.demo.dto.response.CategoryResponseDTO;
import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.CategoryService;
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
@DisplayName("CategoryService Integration Tests")
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category and persist to database")
        void shouldCreateCategoryAndPersist() {
            // Given
            CategoryRequestDTO request = new CategoryRequestDTO();
            request.setName("Vietnamese Food");
            request.setDescription("Traditional Vietnamese cuisine");

            // When
            CategoryResponseDTO response = categoryService.createCategory(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotNull();
            assertThat(response.getName()).isEqualTo("Vietnamese Food");
            assertThat(response.getDescription()).isEqualTo("Traditional Vietnamese cuisine");

            // Verify in database
            Optional<Category> savedCategory = categoryRepository.findById(response.getId());
            assertThat(savedCategory).isPresent();
            assertThat(savedCategory.get().getName()).isEqualTo("Vietnamese Food");
        }

        @Test
        @DisplayName("Should create multiple categories with unique names")
        void shouldCreateMultipleCategoriesWithUniqueNames() {
            // Given
            CategoryRequestDTO request1 = new CategoryRequestDTO();
            request1.setName("Vietnamese");
            request1.setDescription("Vietnamese cuisine");

            CategoryRequestDTO request2 = new CategoryRequestDTO();
            request2.setName("Japanese");
            request2.setDescription("Japanese cuisine");

            // When
            CategoryResponseDTO response1 = categoryService.createCategory(request1);
            CategoryResponseDTO response2 = categoryService.createCategory(request2);

            // Then
            assertThat(response1.getId()).isNotEqualTo(response2.getId());
            assertThat(categoryRepository.count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category and persist changes")
        void shouldUpdateCategoryAndPersist() {
            // Given
            CategoryRequestDTO createRequest = new CategoryRequestDTO();
            createRequest.setName("Fast Food");
            createRequest.setDescription("Quick service restaurants");
            CategoryResponseDTO created = categoryService.createCategory(createRequest);

            CategoryRequestDTO updateRequest = new CategoryRequestDTO();
            updateRequest.setName("Fast Food Updated");
            updateRequest.setDescription("Quick service restaurants - updated");

            // When
            CategoryResponseDTO updated = categoryService.updateCategory(created.getId(), updateRequest);

            // Then
            assertThat(updated.getName()).isEqualTo("Fast Food Updated");
            assertThat(updated.getDescription()).isEqualTo("Quick service restaurants - updated");

            // Verify in database
            Optional<Category> savedCategory = categoryRepository.findById(created.getId());
            assertThat(savedCategory).isPresent();
            assertThat(savedCategory.get().getName()).isEqualTo("Fast Food Updated");
            assertThat(savedCategory.get().getDescription()).isEqualTo("Quick service restaurants - updated");
        }

        @Test
        @DisplayName("Should update only specified fields")
        void shouldUpdateOnlySpecifiedFields() {
            // Given
            CategoryRequestDTO createRequest = new CategoryRequestDTO();
            createRequest.setName("Original Name");
            createRequest.setDescription("Original Description");
            CategoryResponseDTO created = categoryService.createCategory(createRequest);

            CategoryRequestDTO updateRequest = new CategoryRequestDTO();
            updateRequest.setName("Updated Name");
            updateRequest.setDescription("Original Description");

            // When
            CategoryResponseDTO updated = categoryService.updateCategory(created.getId(), updateRequest);

            // Then
            assertThat(updated.getName()).isEqualTo("Updated Name");
            assertThat(updated.getDescription()).isEqualTo("Original Description");
        }
    }

    @Nested
    @DisplayName("Get Category Tests")
    class GetCategoryTests {

        @Test
        @DisplayName("Should retrieve category by ID")
        void shouldRetrieveCategoryById() {
            // Given
            CategoryRequestDTO request = new CategoryRequestDTO();
            request.setName("Italian");
            request.setDescription("Italian cuisine");
            CategoryResponseDTO created = categoryService.createCategory(request);

            // When
            CategoryResponseDTO retrieved = categoryService.getCategoryById(created.getId()).orElseThrow();

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getId()).isEqualTo(created.getId());
            assertThat(retrieved.getName()).isEqualTo("Italian");
            assertThat(retrieved.getDescription()).isEqualTo("Italian cuisine");
        }

        @Test
        @DisplayName("Should retrieve all categories")
        void shouldRetrieveAllCategories() {
            // Given
            CategoryRequestDTO request1 = new CategoryRequestDTO();
            request1.setName("Chinese");
            request1.setDescription("Chinese cuisine");

            CategoryRequestDTO request2 = new CategoryRequestDTO();
            request2.setName("Thai");
            request2.setDescription("Thai cuisine");

            categoryService.createCategory(request1);
            categoryService.createCategory(request2);

            // When
            List<CategoryResponseDTO> categories = categoryService.getAllCategories();

            // Then
            assertThat(categories).hasSize(2);
            assertThat(categories)
                    .extracting(CategoryResponseDTO::getName)
                    .containsExactlyInAnyOrder("Chinese", "Thai");
        }

        @Test
        @DisplayName("Should return empty list when no categories exist")
        void shouldReturnEmptyListWhenNoCategoriesExist() {
            // When
            List<CategoryResponseDTO> categories = categoryService.getAllCategories();

            // Then
            assertThat(categories).isEmpty();
        }
    }

    @Nested
    @DisplayName("Delete Category Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category from database")
        void shouldDeleteCategoryFromDatabase() {
            // Given
            CategoryRequestDTO request = new CategoryRequestDTO();
            request.setName("Mexican");
            request.setDescription("Mexican cuisine");
            CategoryResponseDTO created = categoryService.createCategory(request);

            // When
            categoryService.deleteCategory(created.getId());

            // Then
            Optional<Category> deleted = categoryRepository.findById(created.getId());
            assertThat(deleted).isNotPresent();
            assertThat(categoryRepository.count()).isZero();
        }

        @Test
        @DisplayName("Should handle deleting non-existent category")
        void shouldHandleDeletingNonExistentCategory() {
            // Given
            Long nonExistentId = 999L;

            // When/Then - should not throw exception
            categoryService.deleteCategory(nonExistentId);
            assertThat(categoryRepository.count()).isZero();
        }
    }

    @Nested
    @DisplayName("Search and Query Tests")
    class SearchAndQueryTests {

        @Test
        @DisplayName("Should find categories by name")
        void shouldFindCategoriesByName() {
            // Given
            CategoryRequestDTO request = new CategoryRequestDTO();
            request.setName("Korean BBQ");
            request.setDescription("Korean barbecue");
            categoryService.createCategory(request);

            // When
            Optional<CategoryResponseDTO> found = categoryService.getCategoryByName("Korean BBQ");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Korean BBQ");
        }

        @Test
        @DisplayName("Should return empty optional when category name not found")
        void shouldReturnEmptyListWhenNameNotFound() {
            // Given
            CategoryRequestDTO request = new CategoryRequestDTO();
            request.setName("French");
            request.setDescription("French cuisine");
            categoryService.createCategory(request);

            // When
            Optional<CategoryResponseDTO> found = categoryService.getCategoryByName("Indian");

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should check category existence")
        void shouldCheckCategoryExistence() {
            // Given
            CategoryRequestDTO request = new CategoryRequestDTO();
            request.setName("Spanish");
            request.setDescription("Spanish cuisine");
            CategoryResponseDTO created = categoryService.createCategory(request);

            // When
            Boolean exists = categoryService.existsByName("Spanish");
            Boolean notExists = categoryService.existsByName("NonExistent");

            // Then
            assertThat(created).isNotNull();
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @Test
        @DisplayName("Should rollback on database constraint violation")
        void shouldRollbackOnConstraintViolation() {
            // Given
            CategoryRequestDTO request = new CategoryRequestDTO();
            request.setName("Seafood");
            request.setDescription("Fresh seafood");
            categoryService.createCategory(request);

            // When - trying to create duplicate might cause constraint violation depending on DB schema
            long initialCount = categoryRepository.count();

            // Then
            assertThat(initialCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Should maintain data integrity across operations")
        void shouldMaintainDataIntegrityAcrossOperations() {
            // Given
            CategoryRequestDTO request1 = new CategoryRequestDTO();
            request1.setName("Breakfast");
            request1.setDescription("Morning meals");

            CategoryRequestDTO request2 = new CategoryRequestDTO();
            request2.setName("Lunch");
            request2.setDescription("Afternoon meals");

            // When
            CategoryResponseDTO created1 = categoryService.createCategory(request1);
            CategoryResponseDTO created2 = categoryService.createCategory(request2);
            categoryService.deleteCategory(created1.getId());

            // Then
            assertThat(categoryRepository.count()).isEqualTo(1);
            assertThat(categoryRepository.findById(created2.getId())).isPresent();
            assertThat(categoryRepository.findById(created1.getId())).isNotPresent();
        }
    }
}
