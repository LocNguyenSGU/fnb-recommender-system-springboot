package com.example.demo.service.impl;

import com.example.demo.dto.request.CommentRequestDTO;
import com.example.demo.dto.response.CommentResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.model.Comment;
import com.example.demo.repository.BlogRepository;
import com.example.demo.repository.CommentRepository;
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
@DisplayName("CommentService Unit Tests")
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment comment;
    private CommentRequestDTO commentRequestDTO;
    private CommentResponseDTO commentResponseDTO;

    @BeforeEach
    void setUp() {
        comment = new Comment();
        comment.setId(1L);
        comment.setContent("Great blog post!");

        commentRequestDTO = new CommentRequestDTO();
        commentRequestDTO.setContent("Great blog post!");
        commentRequestDTO.setBlogId(1L);
        commentRequestDTO.setUserId(1L);

        commentResponseDTO = new CommentResponseDTO();
        commentResponseDTO.setId(1L);
        commentResponseDTO.setContent("Great blog post!");
    }

    @Nested
    @DisplayName("Create Comment Tests")
    class CreateCommentTests {

        @Test
        @DisplayName("Should create comment successfully")
        void shouldCreateCommentSuccessfully() {
            // Given
            when(commentMapper.toEntity(any(CommentRequestDTO.class), any(BlogRepository.class), any(UserRepository.class)))
                    .thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toResponseDTO(any(Comment.class))).thenReturn(commentResponseDTO);

            // When
            CommentResponseDTO result = commentService.createComment(commentRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEqualTo("Great blog post!");

            verify(commentMapper).toEntity(commentRequestDTO, blogRepository, userRepository);
            verify(commentRepository).save(comment);
            verify(commentMapper).toResponseDTO(comment);
        }

        @Test
        @DisplayName("Should create comment with long content")
        void shouldCreateCommentWithLongContent() {
            // Given
            String longContent = "A".repeat(500);
            commentRequestDTO.setContent(longContent);

            when(commentMapper.toEntity(any(CommentRequestDTO.class), any(BlogRepository.class), any(UserRepository.class)))
                    .thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toResponseDTO(any(Comment.class))).thenReturn(commentResponseDTO);

            // When
            CommentResponseDTO result = commentService.createComment(commentRequestDTO);

            // Then
            assertThat(result).isNotNull();
            verify(commentRepository).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("Update Comment Tests")
    class UpdateCommentTests {

        @Test
        @DisplayName("Should update comment successfully")
        void shouldUpdateCommentSuccessfully() {
            // Given
            Long commentId = 1L;
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            doNothing().when(commentMapper).updateEntityFromDTO(any(Comment.class), any(CommentRequestDTO.class),
                    any(BlogRepository.class), any(UserRepository.class));
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toResponseDTO(any(Comment.class))).thenReturn(commentResponseDTO);

            // When
            CommentResponseDTO result = commentService.updateComment(commentId, commentRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(commentId);

            verify(commentRepository).findById(commentId);
            verify(commentMapper).updateEntityFromDTO(comment, commentRequestDTO, blogRepository, userRepository);
            verify(commentRepository).save(comment);
        }

        @Test
        @DisplayName("Should throw exception when comment not found")
        void shouldThrowExceptionWhenCommentNotFound() {
            // Given
            Long commentId = 999L;
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.updateComment(commentId, commentRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Comment")
                    .hasMessageContaining("id");

            verify(commentRepository).findById(commentId);
            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("Delete Comment Tests")
    class DeleteCommentTests {

        @Test
        @DisplayName("Should delete comment successfully")
        void shouldDeleteCommentSuccessfully() {
            // Given
            Long commentId = 1L;
            doNothing().when(commentRepository).deleteById(commentId);

            // When
            commentService.deleteComment(commentId);

            // Then
            verify(commentRepository).deleteById(commentId);
        }
    }

    @Nested
    @DisplayName("Get Comment Tests")
    class GetCommentTests {

        @Test
        @DisplayName("Should get comment by id successfully")
        void shouldGetCommentByIdSuccessfully() {
            // Given
            Long commentId = 1L;
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(commentMapper.toResponseDTO(any(Comment.class))).thenReturn(commentResponseDTO);

            // When
            Optional<CommentResponseDTO> result = commentService.getCommentById(commentId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(commentId);

            verify(commentRepository).findById(commentId);
            verify(commentMapper).toResponseDTO(comment);
        }

        @Test
        @DisplayName("Should return empty when comment not found")
        void shouldReturnEmptyWhenCommentNotFound() {
            // Given
            Long commentId = 999L;
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // When
            Optional<CommentResponseDTO> result = commentService.getCommentById(commentId);

            // Then
            assertThat(result).isEmpty();
            verify(commentRepository).findById(commentId);
            verify(commentMapper, never()).toResponseDTO(any(Comment.class));
        }

        @Test
        @DisplayName("Should get all comments successfully")
        void shouldGetAllCommentsSuccessfully() {
            // Given
            Comment comment2 = new Comment();
            comment2.setId(2L);
            comment2.setContent("Another comment");

            CommentResponseDTO commentResponseDTO2 = new CommentResponseDTO();
            commentResponseDTO2.setId(2L);
            commentResponseDTO2.setContent("Another comment");

            List<Comment> comments = Arrays.asList(comment, comment2);

            when(commentRepository.findAll()).thenReturn(comments);
            when(commentMapper.toResponseDTO(comment)).thenReturn(commentResponseDTO);
            when(commentMapper.toResponseDTO(comment2)).thenReturn(commentResponseDTO2);

            // When
            List<CommentResponseDTO> result = commentService.getAllComments();

            // Then
            assertThat(result).hasSize(2);
            verify(commentRepository).findAll();
            verify(commentMapper, times(2)).toResponseDTO(any(Comment.class));
        }

        @Test
        @DisplayName("Should return empty list when no comments exist")
        void shouldReturnEmptyListWhenNoCommentsExist() {
            // Given
            when(commentRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<CommentResponseDTO> result = commentService.getAllComments();

            // Then
            assertThat(result).isEmpty();
            verify(commentRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Get Comments By Blog Tests")
    class GetCommentsByBlogTests {

        @Test
        @DisplayName("Should get comments by blog id successfully")
        void shouldGetCommentsByBlogIdSuccessfully() {
            // Given
            Long blogId = 1L;
            List<Comment> blogComments = Arrays.asList(comment);

            when(commentRepository.findByBlogIdOrderByCreatedAtDesc(blogId)).thenReturn(blogComments);
            when(commentMapper.toResponseDTO(any(Comment.class))).thenReturn(commentResponseDTO);

            // When
            List<CommentResponseDTO> result = commentService.getCommentsByBlogId(blogId);

            // Then
            assertThat(result).hasSize(1);
            verify(commentRepository).findByBlogIdOrderByCreatedAtDesc(blogId);
        }

        @Test
        @DisplayName("Should return empty list when blog has no comments")
        void shouldReturnEmptyListWhenBlogHasNoComments() {
            // Given
            Long blogId = 999L;
            when(commentRepository.findByBlogIdOrderByCreatedAtDesc(blogId)).thenReturn(Arrays.asList());

            // When
            List<CommentResponseDTO> result = commentService.getCommentsByBlogId(blogId);

            // Then
            assertThat(result).isEmpty();
            verify(commentRepository).findByBlogIdOrderByCreatedAtDesc(blogId);
        }

        @Test
        @DisplayName("Should return comments ordered by created date")
        void shouldReturnCommentsOrderedByCreatedDate() {
            // Given
            Long blogId = 1L;
            Comment comment2 = new Comment();
            comment2.setId(2L);
            comment2.setContent("Newer comment");

            List<Comment> blogComments = Arrays.asList(comment2, comment);

            when(commentRepository.findByBlogIdOrderByCreatedAtDesc(blogId)).thenReturn(blogComments);
            when(commentMapper.toResponseDTO(any(Comment.class))).thenReturn(commentResponseDTO);

            // When
            List<CommentResponseDTO> result = commentService.getCommentsByBlogId(blogId);

            // Then
            assertThat(result).hasSize(2);
            verify(commentRepository).findByBlogIdOrderByCreatedAtDesc(blogId);
        }
    }

    @Nested
    @DisplayName("Get Comments By User Tests")
    class GetCommentsByUserTests {

        @Test
        @DisplayName("Should get comments by user id successfully")
        void shouldGetCommentsByUserIdSuccessfully() {
            // Given
            Long userId = 1L;
            List<Comment> userComments = Arrays.asList(comment);

            when(commentRepository.findByUserId(userId)).thenReturn(userComments);
            when(commentMapper.toResponseDTO(any(Comment.class))).thenReturn(commentResponseDTO);

            // When
            List<CommentResponseDTO> result = commentService.getCommentsByUserId(userId);

            // Then
            assertThat(result).hasSize(1);
            verify(commentRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should return empty list when user has no comments")
        void shouldReturnEmptyListWhenUserHasNoComments() {
            // Given
            Long userId = 999L;
            when(commentRepository.findByUserId(userId)).thenReturn(Arrays.asList());

            // When
            List<CommentResponseDTO> result = commentService.getCommentsByUserId(userId);

            // Then
            assertThat(result).isEmpty();
            verify(commentRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should handle user with multiple comments")
        void shouldHandleUserWithMultipleComments() {
            // Given
            Long userId = 1L;
            Comment comment2 = new Comment();
            comment2.setId(2L);
            comment2.setContent("Second comment");

            Comment comment3 = new Comment();
            comment3.setId(3L);
            comment3.setContent("Third comment");

            List<Comment> userComments = Arrays.asList(comment, comment2, comment3);

            when(commentRepository.findByUserId(userId)).thenReturn(userComments);
            when(commentMapper.toResponseDTO(any(Comment.class))).thenReturn(commentResponseDTO);

            // When
            List<CommentResponseDTO> result = commentService.getCommentsByUserId(userId);

            // Then
            assertThat(result).hasSize(3);
            verify(commentRepository).findByUserId(userId);
            verify(commentMapper, times(3)).toResponseDTO(any(Comment.class));
        }
    }
}
