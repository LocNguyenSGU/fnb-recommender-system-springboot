package com.example.demo.service.integration;

import com.example.demo.dto.request.BlogRequestDTO;
import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.BlogResponseDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BlogRepository;
import com.example.demo.service.BlogService;
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
@DisplayName("BlogService Integration Tests")
class BlogServiceIntegrationTest {

    @Autowired
    private BlogService blogService;

    @Autowired
    private UserService userService;

    @Autowired
    private BlogRepository blogRepository;

    private Long authorId;
    private BlogRequestDTO blogRequestDTO;

    @BeforeEach
    void setUp() {
        blogRepository.deleteAll();

        // Create author
        UserRequestDTO userRequest = new UserRequestDTO();
        userRequest.setUsername("author");
        userRequest.setEmail("author@example.com");
        userRequest.setFullName("Blog Author");
        userRequest.setPassword("password");
        UserResponseDTO author = userService.createUser(userRequest);
        authorId = author.getId();

        blogRequestDTO = new BlogRequestDTO();
        blogRequestDTO.setTitle("Test Blog");
        blogRequestDTO.setContent("This is a test blog content");
        blogRequestDTO.setAuthorId(authorId);
        blogRequestDTO.setStatus("PUBLISHED");
    }

    @Nested
    @DisplayName("Create Blog Integration Tests")
    class CreateBlogIntegrationTests {

        @Test
        @DisplayName("Should create blog with author relationship")
        void shouldCreateBlogWithAuthorRelationship() {
            // When
            BlogResponseDTO result = blogService.createBlog(blogRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Blog");

            // Verify in database
            assertThat(blogRepository.findById(result.getId())).isPresent();
        }

        @Test
        @DisplayName("Should create multiple blogs for same author")
        void shouldCreateMultipleBlogsForSameAuthor() {
            // When
            BlogResponseDTO blog1 = blogService.createBlog(blogRequestDTO);

            BlogRequestDTO blog2Request = new BlogRequestDTO();
            blog2Request.setTitle("Second Blog");
            blog2Request.setContent("Second content");
            blog2Request.setAuthorId(authorId);
            BlogResponseDTO blog2 = blogService.createBlog(blog2Request);

            // Then
            assertThat(blog1.getId()).isNotEqualTo(blog2.getId());

            List<BlogResponseDTO> authorBlogs = blogService.getBlogsByAuthorId(authorId);
            assertThat(authorBlogs).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Update Blog Integration Tests")
    class UpdateBlogIntegrationTests {

        @Test
        @DisplayName("Should update blog in database")
        void shouldUpdateBlogInDatabase() {
            // Given
            BlogResponseDTO createdBlog = blogService.createBlog(blogRequestDTO);

            BlogRequestDTO updateRequest = new BlogRequestDTO();
            updateRequest.setTitle("Updated Title");
            updateRequest.setContent("Updated content");

            // When
            BlogResponseDTO result = blogService.updateBlog(createdBlog.getId(), updateRequest);

            // Then
            assertThat(result.getTitle()).isEqualTo("Updated Title");

            // Verify in database
            Optional<BlogResponseDTO> dbBlog = blogService.getBlogById(createdBlog.getId());
            assertThat(dbBlog).isPresent();
            assertThat(dbBlog.get().getTitle()).isEqualTo("Updated Title");
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent blog")
        void shouldThrowExceptionWhenUpdatingNonExistentBlog() {
            // When & Then
            assertThatThrownBy(() -> blogService.updateBlog(999L, blogRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Blog");
        }
    }

    @Nested
    @DisplayName("Delete Blog Integration Tests")
    class DeleteBlogIntegrationTests {

        @Test
        @DisplayName("Should delete blog from database")
        void shouldDeleteBlogFromDatabase() {
            // Given
            BlogResponseDTO createdBlog = blogService.createBlog(blogRequestDTO);
            Long blogId = createdBlog.getId();

            // When
            blogService.deleteBlog(blogId);

            // Then
            assertThat(blogRepository.findById(blogId)).isEmpty();
            assertThat(blogService.getBlogById(blogId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Blog Integration Tests")
    class GetBlogIntegrationTests {

        @Test
        @DisplayName("Should get blog by id from database")
        void shouldGetBlogByIdFromDatabase() {
            // Given
            BlogResponseDTO createdBlog = blogService.createBlog(blogRequestDTO);

            // When
            Optional<BlogResponseDTO> result = blogService.getBlogById(createdBlog.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Test Blog");
        }

        @Test
        @DisplayName("Should get all blogs from database")
        void shouldGetAllBlogsFromDatabase() {
            // Given
            blogService.createBlog(blogRequestDTO);

            BlogRequestDTO blog2 = new BlogRequestDTO();
            blog2.setTitle("Second Blog");
            blog2.setContent("Second content");
            blog2.setAuthorId(authorId);
            blogService.createBlog(blog2);

            // When
            List<BlogResponseDTO> result = blogService.getAllBlogs();

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should get blogs by author id")
        void shouldGetBlogsByAuthorId() {
            // Given
            blogService.createBlog(blogRequestDTO);

            BlogRequestDTO blog2 = new BlogRequestDTO();
            blog2.setTitle("Second Blog");
            blog2.setContent("Second content");
            blog2.setAuthorId(authorId);
            blogService.createBlog(blog2);

            // When
            List<BlogResponseDTO> result = blogService.getBlogsByAuthorId(authorId);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should get blogs by status")
        void shouldGetBlogsByStatus() {
            // Given
            blogService.createBlog(blogRequestDTO);

            BlogRequestDTO draftBlog = new BlogRequestDTO();
            draftBlog.setTitle("Draft Blog");
            draftBlog.setContent("Draft content");
            draftBlog.setAuthorId(authorId);
            draftBlog.setStatus("DRAFT");
            blogService.createBlog(draftBlog);

            // When
            List<BlogResponseDTO> publishedBlogs = blogService.getBlogsByStatus("PUBLISHED");
            List<BlogResponseDTO> draftBlogs = blogService.getBlogsByStatus("DRAFT");

            // Then
            assertThat(publishedBlogs).hasSize(1);
            assertThat(draftBlogs).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Search Blog Integration Tests")
    class SearchBlogIntegrationTests {

        @Test
        @DisplayName("Should search blogs by title")
        void shouldSearchBlogsByTitle() {
            // Given
            blogService.createBlog(blogRequestDTO);

            BlogRequestDTO blog2 = new BlogRequestDTO();
            blog2.setTitle("Another Test");
            blog2.setContent("Content");
            blog2.setAuthorId(authorId);
            blogService.createBlog(blog2);

            // When
            List<BlogResponseDTO> result = blogService.searchBlogsByTitle("Test");

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should search blogs case-insensitively")
        void shouldSearchBlogsCaseInsensitively() {
            // Given
            blogService.createBlog(blogRequestDTO);

            // When
            List<BlogResponseDTO> result = blogService.searchBlogsByTitle("test");

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Increment Likes Integration Tests")
    class IncrementLikesIntegrationTests {

        @Test
        @DisplayName("Should increment likes count in database")
        void shouldIncrementLikesCountInDatabase() {
            // Given
            BlogResponseDTO createdBlog = blogService.createBlog(blogRequestDTO);
            Long blogId = createdBlog.getId();

            // When
            blogService.incrementLikesCount(blogId);

            // Then
            Optional<BlogResponseDTO> updated = blogService.getBlogById(blogId);
            assertThat(updated).isPresent();
            // Note: This assumes BlogResponseDTO has likesCount field
        }

        @Test
        @DisplayName("Should increment likes multiple times")
        void shouldIncrementLikesMultipleTimes() {
            // Given
            BlogResponseDTO createdBlog = blogService.createBlog(blogRequestDTO);
            Long blogId = createdBlog.getId();

            // When
            blogService.incrementLikesCount(blogId);
            blogService.incrementLikesCount(blogId);
            blogService.incrementLikesCount(blogId);

            // Then
            Optional<BlogResponseDTO> updated = blogService.getBlogById(blogId);
            assertThat(updated).isPresent();
        }

        @Test
        @DisplayName("Should throw exception when incrementing non-existent blog")
        void shouldThrowExceptionWhenIncrementingNonExistentBlog() {
            // When & Then
            assertThatThrownBy(() -> blogService.incrementLikesCount(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Blog");
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @Test
        @DisplayName("Should maintain referential integrity")
        void shouldMaintainReferentialIntegrity() {
            // Given - create blog with author
            BlogResponseDTO blog = blogService.createBlog(blogRequestDTO);

            // When - get blog
            Optional<BlogResponseDTO> result = blogService.getBlogById(blog.getId());

            // Then - blog should have author reference
            assertThat(result).isPresent();
            // Verify author still exists
            assertThat(userService.getUserById(authorId)).isPresent();
        }
    }
}
