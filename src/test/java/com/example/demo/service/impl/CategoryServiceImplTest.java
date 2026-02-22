package com.example.demo.service.impl;

import com.example.demo.dto.request.CategoryRequestDTO;
import com.example.demo.dto.response.CategoryResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
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
@DisplayName("CategoryService Unit Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequestDTO categoryRequestDTO;
    private CategoryResponseDTO categoryResponseDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Vietnamese Food");
        category.setDescription("Traditional Vietnamese cuisine");

        categoryRequestDTO = new CategoryRequestDTO();
        categoryRequestDTO.setName("Vietnamese Food");
        categoryRequestDTO.setDescription("Traditional Vietnamese cuisine");

        categoryResponseDTO = new CategoryResponseDTO();
        categoryResponseDTO.setId(1L);
        categoryResponseDTO.setName("Vietnamese Food");
        categoryResponseDTO.setDescription("Traditional Vietnamese cuisine");
    }

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category successfully")
        void shouldCreateCategorySuccessfully() {
            // Given
            when(categoryMapper.toEntity(any(CategoryRequestDTO.class))).thenReturn(category);
            when(categoryRepository.save(any(Category.class))).thenReturn(category);
            when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(categoryResponseDTO);

            // When
            CategoryResponseDTO result = categoryService.createCategory(categoryRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Vietnamese Food");

            verify(categoryMapper).toEntity(categoryRequestDTO);
            verify(categoryRepository).save(category);
            verify(categoryMapper).toResponseDTO(category);
        }

        @Test
        @DisplayName("Should create category with minimal data")
        void shouldCreateCategoryWithMinimalData() {
            // Given
            CategoryRequestDTO minimalRequest = new CategoryRequestDTO();
            minimalRequest.setName("Fast Food");

            when(categoryMapper.toEntity(any(CategoryRequestDTO.class))).thenReturn(category);
            when(categoryRepository.save(any(Category.class))).thenReturn(category);
            when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(categoryResponseDTO);

            // When
            CategoryResponseDTO result = categoryService.createCategory(minimalRequest);

            // Then
            assertThat(result).isNotNull();
            verify(categoryRepository).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully")
        void shouldUpdateCategorySuccessfully() {
            // Given
            Long categoryId = 1L;
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            doNothing().when(categoryMapper).updateEntityFromDTO(any(Category.class), any(CategoryRequestDTO.class));
            when(categoryRepository.save(any(Category.class))).thenReturn(category);
            when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(categoryResponseDTO);

            // When
            CategoryResponseDTO result = categoryService.updateCategory(categoryId, categoryRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(categoryId);

            verify(categoryRepository).findById(categoryId);
            verify(categoryMapper).updateEntityFromDTO(category, categoryRequestDTO);
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // Given
            Long categoryId = 999L;
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> categoryService.updateCategory(categoryId, categoryRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category")
                    .hasMessageContaining("id");

            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("Delete Category Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category successfully")
        void shouldDeleteCategorySuccessfully() {
            // Given
            Long categoryId = 1L;
            doNothing().when(categoryRepository).deleteById(categoryId);

            // When
            categoryService.deleteCategory(categoryId);

            // Then
            verify(categoryRepository).deleteById(categoryId);
        }
    }

    @Nested
    @DisplayName("Get Category Tests")
    class GetCategoryTests {

        @Test
        @DisplayName("Should get category by id successfully")
        void shouldGetCategoryByIdSuccessfully() {
            // Given
            Long categoryId = 1L;
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(categoryResponseDTO);

            // When
            Optional<CategoryResponseDTO> result = categoryService.getCategoryById(categoryId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(categoryId);

            verify(categoryRepository).findById(categoryId);
            verify(categoryMapper).toResponseDTO(category);
        }

        @Test
        @DisplayName("Should return empty when category not found by id")
        void shouldReturnEmptyWhenCategoryNotFoundById() {
            // Given
            Long categoryId = 999L;
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            // When
            Optional<CategoryResponseDTO> result = categoryService.getCategoryById(categoryId);

            // Then
            assertThat(result).isEmpty();
            verify(categoryRepository).findById(categoryId);
            verify(categoryMapper, never()).toResponseDTO(any(Category.class));
        }

        @Test
        @DisplayName("Should get category by name successfully")
        void shouldGetCategoryByNameSuccessfully() {
            // Given
            String name = "Vietnamese Food";
            when(categoryRepository.findByName(name)).thenReturn(Optional.of(category));
            when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(categoryResponseDTO);

            // When
            Optional<CategoryResponseDTO> result = categoryService.getCategoryByName(name);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo(name);

            verify(categoryRepository).findByName(name);
            verify(categoryMapper).toResponseDTO(category);
        }

        @Test
        @DisplayName("Should return empty when category not found by name")
        void shouldReturnEmptyWhenCategoryNotFoundByName() {
            // Given
            String name = "NonExistent";
            when(categoryRepository.findByName(name)).thenReturn(Optional.empty());

            // When
            Optional<CategoryResponseDTO> result = categoryService.getCategoryByName(name);

            // Then
            assertThat(result).isEmpty();
            verify(categoryRepository).findByName(name);
        }

        @Test
        @DisplayName("Should get all categories successfully")
        void shouldGetAllCategoriesSuccessfully() {
            // Given
            Category category2 = new Category();
            category2.setId(2L);
            category2.setName("Japanese Food");

            CategoryResponseDTO categoryResponseDTO2 = new CategoryResponseDTO();
            categoryResponseDTO2.setId(2L);
            categoryResponseDTO2.setName("Japanese Food");

            List<Category> categories = Arrays.asList(category, category2);

            when(categoryRepository.findAll()).thenReturn(categories);
            when(categoryMapper.toResponseDTO(category)).thenReturn(categoryResponseDTO);
            when(categoryMapper.toResponseDTO(category2)).thenReturn(categoryResponseDTO2);

            // When
            List<CategoryResponseDTO> result = categoryService.getAllCategories();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Vietnamese Food");
            assertThat(result.get(1).getName()).isEqualTo("Japanese Food");

            verify(categoryRepository).findAll();
            verify(categoryMapper, times(2)).toResponseDTO(any(Category.class));
        }

        @Test
        @DisplayName("Should return empty list when no categories exist")
        void shouldReturnEmptyListWhenNoCategoriesExist() {
            // Given
            when(categoryRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<CategoryResponseDTO> result = categoryService.getAllCategories();

            // Then
            assertThat(result).isEmpty();
            verify(categoryRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Exists Tests")
    class ExistsTests {

        @Test
        @DisplayName("Should return true when category name exists")
        void shouldReturnTrueWhenCategoryNameExists() {
            // Given
            String name = "Vietnamese Food";
            when(categoryRepository.existsByName(name)).thenReturn(true);

            // When
            boolean result = categoryService.existsByName(name);

            // Then
            assertThat(result).isTrue();
            verify(categoryRepository).existsByName(name);
        }

        @Test
        @DisplayName("Should return false when category name does not exist")
        void shouldReturnFalseWhenCategoryNameDoesNotExist() {
            // Given
            String name = "NonExistent";
            when(categoryRepository.existsByName(name)).thenReturn(false);

            // When
            boolean result = categoryService.existsByName(name);

            // Then
            assertThat(result).isFalse();
            verify(categoryRepository).existsByName(name);
        }

        @Test
        @DisplayName("Should handle null name check")
        void shouldHandleNullNameCheck() {
            // Given
            when(categoryRepository.existsByName(null)).thenReturn(false);

            // When
            boolean result = categoryService.existsByName(null);

            // Then
            assertThat(result).isFalse();
            verify(categoryRepository).existsByName(null);
        }

        @Test
        @DisplayName("Should handle empty name check")
        void shouldHandleEmptyNameCheck() {
            // Given
            String emptyName = "";
            when(categoryRepository.existsByName(emptyName)).thenReturn(false);

            // When
            boolean result = categoryService.existsByName(emptyName);

            // Then
            assertThat(result).isFalse();
            verify(categoryRepository).existsByName(emptyName);
        }
    }
}
