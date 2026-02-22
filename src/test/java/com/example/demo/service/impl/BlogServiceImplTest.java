package com.example.demo.service.impl;

import com.example.demo.dto.request.BlogRequestDTO;
import com.example.demo.dto.response.BlogResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.BlogMapper;
import com.example.demo.model.Blog;
import com.example.demo.repository.BlogRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlogService Unit Tests")
class BlogServiceImplTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BlogMapper blogMapper;

    @InjectMocks
    private BlogServiceImpl blogService;

    private Blog blog;
    private BlogRequestDTO blogRequestDTO;
    private BlogResponseDTO blogResponseDTO;

    @BeforeEach
    void setUp() {
        blog = new Blog();
        blog.setId(1L);
        blog.setTitle("Test Blog");
        blog.setContent("Test Content");
        blog.setStatus("PUBLISHED");
        blog.setLikesCount(0);

        blogRequestDTO = new BlogRequestDTO();
        blogRequestDTO.setTitle("Test Blog");
        blogRequestDTO.setContent("Test Content");
        blogRequestDTO.setAuthorId(1L);

        blogResponseDTO = new BlogResponseDTO();
        blogResponseDTO.setId(1L);
        blogResponseDTO.setTitle("Test Blog");
        blogResponseDTO.setContent("Test Content");
    }

    @Nested
    @DisplayName("Create Blog Tests")
    class CreateBlogTests {

        @Test
        @DisplayName("Should create blog successfully")
        void shouldCreateBlogSuccessfully() {
            // Given
            when(blogMapper.toEntity(any(BlogRequestDTO.class), any(UserRepository.class))).thenReturn(blog);
            when(blogRepository.save(any(Blog.class))).thenReturn(blog);
            when(blogMapper.toResponseDTO(any(Blog.class))).thenReturn(blogResponseDTO);

            // When
            BlogResponseDTO result = blogService.createBlog(blogRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Blog");

            verify(blogMapper).toEntity(blogRequestDTO, userRepository);
            verify(blogRepository).save(blog);
            verify(blogMapper).toResponseDTO(blog);
        }

        @Test
        @DisplayName("Should handle blog with minimal data")
        void shouldHandleBlogWithMinimalData() {
            // Given
            BlogRequestDTO minimalRequest = new BlogRequestDTO();
            minimalRequest.setTitle("Title Only");

            when(blogMapper.toEntity(any(BlogRequestDTO.class), any(UserRepository.class))).thenReturn(blog);
            when(blogRepository.save(any(Blog.class))).thenReturn(blog);
            when(blogMapper.toResponseDTO(any(Blog.class))).thenReturn(blogResponseDTO);

            // When
            BlogResponseDTO result = blogService.createBlog(minimalRequest);

            // Then
            assertThat(result).isNotNull();
            verify(blogRepository).save(any(Blog.class));
        }
    }

    @Nested
    @DisplayName("Update Blog Tests")
    class UpdateBlogTests {

        @Test
        @DisplayName("Should update blog successfully")
        void shouldUpdateBlogSuccessfully() {
            // Given
            Long blogId = 1L;
            when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));
            doNothing().when(blogMapper).updateEntityFromDTO(any(Blog.class), any(BlogRequestDTO.class), any(UserRepository.class));
            when(blogRepository.save(any(Blog.class))).thenReturn(blog);
            when(blogMapper.toResponseDTO(any(Blog.class))).thenReturn(blogResponseDTO);

            // When
            BlogResponseDTO result = blogService.updateBlog(blogId, blogRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(blogId);

            verify(blogRepository).findById(blogId);
            verify(blogMapper).updateEntityFromDTO(blog, blogRequestDTO, userRepository);
            verify(blogRepository).save(blog);
        }

        @Test
        @DisplayName("Should throw exception when blog not found")
        void shouldThrowExceptionWhenBlogNotFound() {
            // Given
            Long blogId = 999L;
            when(blogRepository.findById(blogId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> blogService.updateBlog(blogId, blogRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Blog")
                    .hasMessageContaining("id");

            verify(blogRepository).findById(blogId);
            verify(blogRepository, never()).save(any(Blog.class));
        }
    }

    @Nested
    @DisplayName("Delete Blog Tests")
    class DeleteBlogTests {

        @Test
        @DisplayName("Should delete blog successfully")
        void shouldDeleteBlogSuccessfully() {
            // Given
            Long blogId = 1L;
            doNothing().when(blogRepository).deleteById(blogId);

            // When
            blogService.deleteBlog(blogId);

            // Then
            verify(blogRepository).deleteById(blogId);
        }
    }

    @Nested
    @DisplayName("Get Blog Tests")
    class GetBlogTests {

        @Test
        @DisplayName("Should get blog by id successfully")
        void shouldGetBlogByIdSuccessfully() {
            // Given
            Long blogId = 1L;
            when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));
            when(blogMapper.toResponseDTO(any(Blog.class))).thenReturn(blogResponseDTO);

            // When
            Optional<BlogResponseDTO> result = blogService.getBlogById(blogId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(blogId);

            verify(blogRepository).findById(blogId);
            verify(blogMapper).toResponseDTO(blog);
        }

        @Test
        @DisplayName("Should return empty when blog not found")
        void shouldReturnEmptyWhenBlogNotFound() {
            // Given
            Long blogId = 999L;
            when(blogRepository.findById(blogId)).thenReturn(Optional.empty());

            // When
            Optional<BlogResponseDTO> result = blogService.getBlogById(blogId);

            // Then
            assertThat(result).isEmpty();
            verify(blogRepository).findById(blogId);
            verify(blogMapper, never()).toResponseDTO(any(Blog.class));
        }

        @Test
        @DisplayName("Should get all blogs successfully")
        void shouldGetAllBlogsSuccessfully() {
            // Given
            Blog blog2 = new Blog();
            blog2.setId(2L);
            blog2.setTitle("Blog 2");

            BlogResponseDTO blogResponseDTO2 = new BlogResponseDTO();
            blogResponseDTO2.setId(2L);
            blogResponseDTO2.setTitle("Blog 2");

            List<Blog> blogs = Arrays.asList(blog, blog2);

            when(blogRepository.findAll()).thenReturn(blogs);
            when(blogMapper.toResponseDTO(blog)).thenReturn(blogResponseDTO);
            when(blogMapper.toResponseDTO(blog2)).thenReturn(blogResponseDTO2);

            // When
            List<BlogResponseDTO> result = blogService.getAllBlogs();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Test Blog");
            assertThat(result.get(1).getTitle()).isEqualTo("Blog 2");

            verify(blogRepository).findAll();
            verify(blogMapper, times(2)).toResponseDTO(any(Blog.class));
        }

        @Test
        @DisplayName("Should return empty list when no blogs exist")
        void shouldReturnEmptyListWhenNoBlogsExist() {
            // Given
            when(blogRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<BlogResponseDTO> result = blogService.getAllBlogs();

            // Then
            assertThat(result).isEmpty();
            verify(blogRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Get Blogs By Author Tests")
    class GetBlogsByAuthorTests {

        @Test
        @DisplayName("Should get blogs by author id successfully")
        void shouldGetBlogsByAuthorIdSuccessfully() {
            // Given
            Long authorId = 1L;
            List<Blog> authorBlogs = Arrays.asList(blog);

            when(blogRepository.findByAuthorId(authorId)).thenReturn(authorBlogs);
            when(blogMapper.toResponseDTO(any(Blog.class))).thenReturn(blogResponseDTO);

            // When
            List<BlogResponseDTO> result = blogService.getBlogsByAuthorId(authorId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Test Blog");

            verify(blogRepository).findByAuthorId(authorId);
            verify(blogMapper).toResponseDTO(blog);
        }

        @Test
        @DisplayName("Should return empty list when author has no blogs")
        void shouldReturnEmptyListWhenAuthorHasNoBlogs() {
            // Given
            Long authorId = 999L;
            when(blogRepository.findByAuthorId(authorId)).thenReturn(Arrays.asList());

            // When
            List<BlogResponseDTO> result = blogService.getBlogsByAuthorId(authorId);

            // Then
            assertThat(result).isEmpty();
            verify(blogRepository).findByAuthorId(authorId);
        }
    }

    @Nested
    @DisplayName("Get Blogs By Status Tests")
    class GetBlogsByStatusTests {

        @Test
        @DisplayName("Should get published blogs successfully")
        void shouldGetPublishedBlogsSuccessfully() {
            // Given
            String status = "PUBLISHED";
            List<Blog> publishedBlogs = Arrays.asList(blog);

            when(blogRepository.findByStatusOrderByCreatedAtDesc(status)).thenReturn(publishedBlogs);
            when(blogMapper.toResponseDTO(any(Blog.class))).thenReturn(blogResponseDTO);

            // When
            List<BlogResponseDTO> result = blogService.getBlogsByStatus(status);

            // Then
            assertThat(result).hasSize(1);
            verify(blogRepository).findByStatusOrderByCreatedAtDesc(status);
        }

        @Test
        @DisplayName("Should get draft blogs successfully")
        void shouldGetDraftBlogsSuccessfully() {
            // Given
            String status = "DRAFT";
            when(blogRepository.findByStatusOrderByCreatedAtDesc(status)).thenReturn(Arrays.asList());

            // When
            List<BlogResponseDTO> result = blogService.getBlogsByStatus(status);

            // Then
            assertThat(result).isEmpty();
            verify(blogRepository).findByStatusOrderByCreatedAtDesc(status);
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // Given
            when(blogRepository.findByStatusOrderByCreatedAtDesc(null)).thenReturn(Arrays.asList());

            // When
            List<BlogResponseDTO> result = blogService.getBlogsByStatus(null);

            // Then
            assertThat(result).isEmpty();
            verify(blogRepository).findByStatusOrderByCreatedAtDesc(null);
        }
    }

    @Nested
    @DisplayName("Search Blogs Tests")
    class SearchBlogsTests {

        @Test
        @DisplayName("Should search blogs by title successfully")
        void shouldSearchBlogsByTitleSuccessfully() {
            // Given
            String title = "Test";
            List<Blog> foundBlogs = Arrays.asList(blog);

            when(blogRepository.findByTitleContainingIgnoreCase(title)).thenReturn(foundBlogs);
            when(blogMapper.toResponseDTO(any(Blog.class))).thenReturn(blogResponseDTO);

            // When
            List<BlogResponseDTO> result = blogService.searchBlogsByTitle(title);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).contains("Test");

            verify(blogRepository).findByTitleContainingIgnoreCase(title);
        }

        @Test
        @DisplayName("Should return empty list when no blogs match title")
        void shouldReturnEmptyListWhenNoBlogsMatchTitle() {
            // Given
            String title = "NonExistent";
            when(blogRepository.findByTitleContainingIgnoreCase(title)).thenReturn(Arrays.asList());

            // When
            List<BlogResponseDTO> result = blogService.searchBlogsByTitle(title);

            // Then
            assertThat(result).isEmpty();
            verify(blogRepository).findByTitleContainingIgnoreCase(title);
        }

        @Test
        @DisplayName("Should handle case-insensitive search")
        void shouldHandleCaseInsensitiveSearch() {
            // Given
            String title = "TEST";
            List<Blog> foundBlogs = Arrays.asList(blog);

            when(blogRepository.findByTitleContainingIgnoreCase(title)).thenReturn(foundBlogs);
            when(blogMapper.toResponseDTO(any(Blog.class))).thenReturn(blogResponseDTO);

            // When
            List<BlogResponseDTO> result = blogService.searchBlogsByTitle(title);

            // Then
            assertThat(result).hasSize(1);
            verify(blogRepository).findByTitleContainingIgnoreCase(title);
        }
    }

    @Nested
    @DisplayName("Get Top Liked Blogs Tests")
    class GetTopLikedBlogsTests {

        @Test
        @DisplayName("Should get top liked blogs successfully")
        void shouldGetTopLikedBlogsSuccessfully() {
            // Given
            blog.setLikesCount(100);
            List<Blog> topBlogs = Arrays.asList(blog);

            when(blogRepository.findTopLikedBlogs()).thenReturn(topBlogs);
            when(blogMapper.toResponseDTO(any(Blog.class))).thenReturn(blogResponseDTO);

            // When
            List<BlogResponseDTO> result = blogService.getTopLikedBlogs();

            // Then
            assertThat(result).hasSize(1);
            verify(blogRepository).findTopLikedBlogs();
        }

        @Test
        @DisplayName("Should return empty list when no blogs exist")
        void shouldReturnEmptyListWhenNoBlogsExist() {
            // Given
            when(blogRepository.findTopLikedBlogs()).thenReturn(Arrays.asList());

            // When
            List<BlogResponseDTO> result = blogService.getTopLikedBlogs();

            // Then
            assertThat(result).isEmpty();
            verify(blogRepository).findTopLikedBlogs();
        }
    }

    @Nested
    @DisplayName("Increment Likes Count Tests")
    class IncrementLikesCountTests {

        @Test
        @DisplayName("Should increment likes count successfully")
        void shouldIncrementLikesCountSuccessfully() {
            // Given
            Long blogId = 1L;
            blog.setLikesCount(5);

            when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));
            when(blogRepository.save(any(Blog.class))).thenReturn(blog);

            // When
            blogService.incrementLikesCount(blogId);

            // Then
            assertThat(blog.getLikesCount()).isEqualTo(6);
            verify(blogRepository).findById(blogId);
            verify(blogRepository).save(blog);
        }

        @Test
        @DisplayName("Should throw exception when blog not found")
        void shouldThrowExceptionWhenBlogNotFound() {
            // Given
            Long blogId = 999L;
            when(blogRepository.findById(blogId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> blogService.incrementLikesCount(blogId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Blog");

            verify(blogRepository).findById(blogId);
            verify(blogRepository, never()).save(any(Blog.class));
        }

        @Test
        @DisplayName("Should handle zero likes count")
        void shouldHandleZeroLikesCount() {
            // Given
            Long blogId = 1L;
            blog.setLikesCount(0);

            when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));
            when(blogRepository.save(any(Blog.class))).thenReturn(blog);

            // When
            blogService.incrementLikesCount(blogId);

            // Then
            assertThat(blog.getLikesCount()).isEqualTo(1);
            verify(blogRepository).save(blog);
        }

        @Test
        @DisplayName("Should handle large likes count")
        void shouldHandleLargeLikesCount() {
            // Given
            Long blogId = 1L;
            blog.setLikesCount(999999);

            when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));
            when(blogRepository.save(any(Blog.class))).thenReturn(blog);

            // When
            blogService.incrementLikesCount(blogId);

            // Then
            assertThat(blog.getLikesCount()).isEqualTo(1000000);
            verify(blogRepository).save(blog);
        }
    }
}
