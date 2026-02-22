package com.example.demo.service.integration;

import com.example.demo.dto.request.BlogRequestDTO;
import com.example.demo.dto.request.CommentRequestDTO;
import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.BlogResponseDTO;
import com.example.demo.dto.response.CommentResponseDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.model.Comment;
import com.example.demo.repository.CommentRepository;
import com.example.demo.service.BlogService;
import com.example.demo.service.CommentService;
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
@DisplayName("CommentService Integration Tests")
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BlogService blogService;

    @Autowired
    private UserService userService;

    private BlogResponseDTO testBlog;
    private UserResponseDTO testAuthor;
    private UserResponseDTO testCommenter;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();

        // Create test author
        UserRequestDTO authorRequest = new UserRequestDTO();
        authorRequest.setUsername("blogauthor");
        authorRequest.setEmail("author@test.com");
        authorRequest.setPassword("password");
        authorRequest.setFullName("Blog Author");
        testAuthor = userService.createUser(authorRequest);

        // Create test commenter
        UserRequestDTO commenterRequest = new UserRequestDTO();
        commenterRequest.setUsername("commenter");
        commenterRequest.setEmail("commenter@test.com");
        commenterRequest.setPassword("password");
        commenterRequest.setFullName("Commenter User");
        testCommenter = userService.createUser(commenterRequest);

        // Create test blog
        BlogRequestDTO blogRequest = new BlogRequestDTO();
        blogRequest.setTitle("Test Blog");
        blogRequest.setContent("Test blog content");
        blogRequest.setStatus("PUBLISHED");
        blogRequest.setAuthorId(testAuthor.getId());
        testBlog = blogService.createBlog(blogRequest);
    }

    @Nested
    @DisplayName("Create Comment Tests")
    class CreateCommentTests {

        @Test
        @DisplayName("Should create comment and persist to database")
        void shouldCreateCommentAndPersist() {
            // Given
            CommentRequestDTO request = new CommentRequestDTO();
            request.setContent("Great article!");
            request.setBlogId(testBlog.getId());
            request.setUserId(testCommenter.getId());

            // When
            CommentResponseDTO response = commentService.createComment(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotNull();
            assertThat(response.getContent()).isEqualTo("Great article!");

            // Verify in database
            Optional<Comment> savedComment = commentRepository.findById(response.getId());
            assertThat(savedComment).isPresent();
            assertThat(savedComment.get().getContent()).isEqualTo("Great article!");
            assertThat(savedComment.get().getBlog().getId()).isEqualTo(testBlog.getId());
            assertThat(savedComment.get().getUser().getId()).isEqualTo(testCommenter.getId());
        }

        @Test
        @DisplayName("Should create multiple comments on same blog")
        void shouldCreateMultipleCommentsOnSameBlog() {
            // Given
            CommentRequestDTO request1 = new CommentRequestDTO();
            request1.setContent("First comment");
            request1.setBlogId(testBlog.getId());
            request1.setUserId(testCommenter.getId());

            CommentRequestDTO request2 = new CommentRequestDTO();
            request2.setContent("Second comment");
            request2.setBlogId(testBlog.getId());
            request2.setUserId(testCommenter.getId());

            // When
            CommentResponseDTO response1 = commentService.createComment(request1);
            CommentResponseDTO response2 = commentService.createComment(request2);

            // Then
            assertThat(response1.getId()).isNotEqualTo(response2.getId());

            List<CommentResponseDTO> blogComments = commentService.getCommentsByBlogId(testBlog.getId());
            assertThat(blogComments).hasSize(2);
        }

        @Test
        @DisplayName("Should create comment with long content")
        void shouldCreateCommentWithLongContent() {
            // Given
            String longContent = "Lorem ipsum ".repeat(100);
            CommentRequestDTO request = new CommentRequestDTO();
            request.setContent(longContent);
            request.setBlogId(testBlog.getId());
            request.setUserId(testCommenter.getId());

            // When
            CommentResponseDTO response = commentService.createComment(request);

            // Then
            assertThat(response.getContent()).hasSize(longContent.length());
        }
    }

    @Nested
    @DisplayName("Update Comment Tests")
    class UpdateCommentTests {

        @Test
        @DisplayName("Should update comment and persist changes")
        void shouldUpdateCommentAndPersist() {
            // Given
            CommentRequestDTO createRequest = new CommentRequestDTO();
            createRequest.setContent("Original comment");
            createRequest.setBlogId(testBlog.getId());
            createRequest.setUserId(testCommenter.getId());
            CommentResponseDTO created = commentService.createComment(createRequest);

            CommentRequestDTO updateRequest = new CommentRequestDTO();
            updateRequest.setContent("Updated comment");
            updateRequest.setBlogId(testBlog.getId());
            updateRequest.setUserId(testCommenter.getId());

            // When
            CommentResponseDTO updated = commentService.updateComment(created.getId(), updateRequest);

            // Then
            assertThat(updated.getContent()).isEqualTo("Updated comment");

            // Verify in database
            Optional<Comment> savedComment = commentRepository.findById(created.getId());
            assertThat(savedComment).isPresent();
            assertThat(savedComment.get().getContent()).isEqualTo("Updated comment");
        }

        @Test
        @DisplayName("Should update and preserve relationships")
        void shouldUpdateAndPreserveRelationships() {
            // Given
            CommentRequestDTO createRequest = new CommentRequestDTO();
            createRequest.setContent("Original");
            createRequest.setBlogId(testBlog.getId());
            createRequest.setUserId(testCommenter.getId());
            CommentResponseDTO created = commentService.createComment(createRequest);

            CommentRequestDTO updateRequest = new CommentRequestDTO();
            updateRequest.setContent("Updated");
            updateRequest.setBlogId(testBlog.getId());
            updateRequest.setUserId(testCommenter.getId());

            // When
            CommentResponseDTO updated = commentService.updateComment(created.getId(), updateRequest);

            // Then
            Optional<Comment> savedComment = commentRepository.findById(updated.getId());
            assertThat(savedComment).isPresent();
            assertThat(savedComment.get().getBlog().getId()).isEqualTo(testBlog.getId());
            assertThat(savedComment.get().getUser().getId()).isEqualTo(testCommenter.getId());
        }
    }

    @Nested
    @DisplayName("Get Comment Tests")
    class GetCommentTests {

        @Test
        @DisplayName("Should retrieve comment by ID")
        void shouldRetrieveCommentById() {
            // Given
            CommentRequestDTO request = new CommentRequestDTO();
            request.setContent("Test comment");
            request.setBlogId(testBlog.getId());
            request.setUserId(testCommenter.getId());
            CommentResponseDTO created = commentService.createComment(request);

            // When
            CommentResponseDTO retrieved = commentService.getCommentById(created.getId()).orElseThrow();

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getId()).isEqualTo(created.getId());
            assertThat(retrieved.getContent()).isEqualTo("Test comment");
        }

        @Test
        @DisplayName("Should retrieve all comments")
        void shouldRetrieveAllComments() {
            // Given
            CommentRequestDTO request1 = new CommentRequestDTO();
            request1.setContent("Comment 1");
            request1.setBlogId(testBlog.getId());
            request1.setUserId(testCommenter.getId());

            CommentRequestDTO request2 = new CommentRequestDTO();
            request2.setContent("Comment 2");
            request2.setBlogId(testBlog.getId());
            request2.setUserId(testCommenter.getId());

            commentService.createComment(request1);
            commentService.createComment(request2);

            // When
            List<CommentResponseDTO> comments = commentService.getAllComments();

            // Then
            assertThat(comments).hasSize(2);
        }

        @Test
        @DisplayName("Should retrieve comments by blog ID")
        void shouldRetrieveCommentsByBlogId() {
            // Given
            CommentRequestDTO request1 = new CommentRequestDTO();
            request1.setContent("Blog comment 1");
            request1.setBlogId(testBlog.getId());
            request1.setUserId(testCommenter.getId());

            CommentRequestDTO request2 = new CommentRequestDTO();
            request2.setContent("Blog comment 2");
            request2.setBlogId(testBlog.getId());
            request2.setUserId(testCommenter.getId());

            commentService.createComment(request1);
            commentService.createComment(request2);

            // When
            List<CommentResponseDTO> blogComments = commentService.getCommentsByBlogId(testBlog.getId());

            // Then
            assertThat(blogComments).hasSize(2);
            assertThat(blogComments)
                    .allMatch(comment -> comment.getBlogId().equals(testBlog.getId()));
        }

        @Test
        @DisplayName("Should retrieve comments by user ID")
        void shouldRetrieveCommentsByUserId() {
            // Given
            CommentRequestDTO request1 = new CommentRequestDTO();
            request1.setContent("User comment 1");
            request1.setBlogId(testBlog.getId());
            request1.setUserId(testCommenter.getId());

            CommentRequestDTO request2 = new CommentRequestDTO();
            request2.setContent("User comment 2");
            request2.setBlogId(testBlog.getId());
            request2.setUserId(testCommenter.getId());

            commentService.createComment(request1);
            commentService.createComment(request2);

            // When
            List<CommentResponseDTO> userComments = commentService.getCommentsByUserId(testCommenter.getId());

            // Then
            assertThat(userComments).hasSize(2);
            assertThat(userComments)
                    .allMatch(comment -> comment.getUserId().equals(testCommenter.getId()));
        }
    }

    @Nested
    @DisplayName("Delete Comment Tests")
    class DeleteCommentTests {

        @Test
        @DisplayName("Should delete comment from database")
        void shouldDeleteCommentFromDatabase() {
            // Given
            CommentRequestDTO request = new CommentRequestDTO();
            request.setContent("To be deleted");
            request.setBlogId(testBlog.getId());
            request.setUserId(testCommenter.getId());
            CommentResponseDTO created = commentService.createComment(request);

            // When
            commentService.deleteComment(created.getId());

            // Then
            Optional<Comment> deleted = commentRepository.findById(created.getId());
            assertThat(deleted).isNotPresent();
        }

        @Test
        @DisplayName("Should delete multiple comments")
        void shouldDeleteMultipleComments() {
            // Given
            CommentRequestDTO request1 = new CommentRequestDTO();
            request1.setContent("Comment 1");
            request1.setBlogId(testBlog.getId());
            request1.setUserId(testCommenter.getId());

            CommentRequestDTO request2 = new CommentRequestDTO();
            request2.setContent("Comment 2");
            request2.setBlogId(testBlog.getId());
            request2.setUserId(testCommenter.getId());

            CommentResponseDTO created1 = commentService.createComment(request1);
            CommentResponseDTO created2 = commentService.createComment(request2);

            // When
            commentService.deleteComment(created1.getId());

            // Then
            assertThat(commentRepository.findById(created1.getId())).isNotPresent();
            assertThat(commentRepository.findById(created2.getId())).isPresent();
            assertThat(commentRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should maintain blog-comment relationship")
        void shouldMaintainBlogCommentRelationship() {
            // Given
            CommentRequestDTO request = new CommentRequestDTO();
            request.setContent("Related comment");
            request.setBlogId(testBlog.getId());
            request.setUserId(testCommenter.getId());

            // When
            CommentResponseDTO created = commentService.createComment(request);

            // Then
            Optional<Comment> savedComment = commentRepository.findById(created.getId());
            assertThat(savedComment).isPresent();
            assertThat(savedComment.get().getBlog()).isNotNull();
            assertThat(savedComment.get().getBlog().getId()).isEqualTo(testBlog.getId());
            assertThat(savedComment.get().getBlog().getTitle()).isEqualTo(testBlog.getTitle());
        }

        @Test
        @DisplayName("Should maintain user-comment relationship")
        void shouldMaintainUserCommentRelationship() {
            // Given
            CommentRequestDTO request = new CommentRequestDTO();
            request.setContent("User's comment");
            request.setBlogId(testBlog.getId());
            request.setUserId(testCommenter.getId());

            // When
            CommentResponseDTO created = commentService.createComment(request);

            // Then
            Optional<Comment> savedComment = commentRepository.findById(created.getId());
            assertThat(savedComment).isPresent();
            assertThat(savedComment.get().getUser()).isNotNull();
            assertThat(savedComment.get().getUser().getId()).isEqualTo(testCommenter.getId());
            assertThat(savedComment.get().getUser().getUsername()).isEqualTo(testCommenter.getUsername());
        }

        @Test
        @DisplayName("Should filter comments by different blogs")
        void shouldFilterCommentsByDifferentBlogs() {
            // Given - Create second blog
            BlogRequestDTO blog2Request = new BlogRequestDTO();
            blog2Request.setTitle("Second Blog");
            blog2Request.setContent("Second blog content");
            blog2Request.setStatus("PUBLISHED");
            blog2Request.setAuthorId(testAuthor.getId());
            BlogResponseDTO blog2 = blogService.createBlog(blog2Request);

            // Create comments for both blogs
            CommentRequestDTO comment1Request = new CommentRequestDTO();
            comment1Request.setContent("Blog 1 comment");
            comment1Request.setBlogId(testBlog.getId());
            comment1Request.setUserId(testCommenter.getId());

            CommentRequestDTO comment2Request = new CommentRequestDTO();
            comment2Request.setContent("Blog 2 comment");
            comment2Request.setBlogId(blog2.getId());
            comment2Request.setUserId(testCommenter.getId());

            commentService.createComment(comment1Request);
            commentService.createComment(comment2Request);

            // When
            List<CommentResponseDTO> blog1Comments = commentService.getCommentsByBlogId(testBlog.getId());
            List<CommentResponseDTO> blog2Comments = commentService.getCommentsByBlogId(blog2.getId());

            // Then
            assertThat(blog1Comments).hasSize(1);
            assertThat(blog2Comments).hasSize(1);
            assertThat(blog1Comments.get(0).getContent()).isEqualTo("Blog 1 comment");
            assertThat(blog2Comments.get(0).getContent()).isEqualTo("Blog 2 comment");
        }

        @Test
        @DisplayName("Should filter comments by different users")
        void shouldFilterCommentsByDifferentUsers() {
            // Given - Create second commenter
            UserRequestDTO user2Request = new UserRequestDTO();
            user2Request.setUsername("commenter2");
            user2Request.setEmail("commenter2@test.com");
            user2Request.setPassword("password");
            user2Request.setFullName("Second Commenter");
            UserResponseDTO user2 = userService.createUser(user2Request);

            // Create comments from different users
            CommentRequestDTO comment1Request = new CommentRequestDTO();
            comment1Request.setContent("User 1 comment");
            comment1Request.setBlogId(testBlog.getId());
            comment1Request.setUserId(testCommenter.getId());

            CommentRequestDTO comment2Request = new CommentRequestDTO();
            comment2Request.setContent("User 2 comment");
            comment2Request.setBlogId(testBlog.getId());
            comment2Request.setUserId(user2.getId());

            commentService.createComment(comment1Request);
            commentService.createComment(comment2Request);

            // When
            List<CommentResponseDTO> user1Comments = commentService.getCommentsByUserId(testCommenter.getId());
            List<CommentResponseDTO> user2Comments = commentService.getCommentsByUserId(user2.getId());

            // Then
            assertThat(user1Comments).hasSize(1);
            assertThat(user2Comments).hasSize(1);
            assertThat(user1Comments.get(0).getContent()).isEqualTo("User 1 comment");
            assertThat(user2Comments.get(0).getContent()).isEqualTo("User 2 comment");
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @Test
        @DisplayName("Should maintain referential integrity")
        void shouldMaintainReferentialIntegrity() {
            // Given
            CommentRequestDTO request = new CommentRequestDTO();
            request.setContent("Test integrity");
            request.setBlogId(testBlog.getId());
            request.setUserId(testCommenter.getId());

            // When
            CommentResponseDTO created = commentService.createComment(request);

            // Then
            Optional<Comment> savedComment = commentRepository.findById(created.getId());
            assertThat(savedComment).isPresent();
            assertThat(savedComment.get().getBlog()).isNotNull();
            assertThat(savedComment.get().getUser()).isNotNull();
        }

        @Test
        @DisplayName("Should handle cascading operations")
        void shouldHandleCascadingOperations() {
            // Given
            CommentRequestDTO request1 = new CommentRequestDTO();
            request1.setContent("Comment 1");
            request1.setBlogId(testBlog.getId());
            request1.setUserId(testCommenter.getId());

            CommentRequestDTO request2 = new CommentRequestDTO();
            request2.setContent("Comment 2");
            request2.setBlogId(testBlog.getId());
            request2.setUserId(testCommenter.getId());

            CommentResponseDTO comment1 = commentService.createComment(request1);
            CommentResponseDTO comment2 = commentService.createComment(request2);

            // When
            commentService.deleteComment(comment1.getId());

            // Then
            assertThat(commentRepository.count()).isEqualTo(1);
            assertThat(commentRepository.findById(comment2.getId())).isPresent();
        }
    }
}
