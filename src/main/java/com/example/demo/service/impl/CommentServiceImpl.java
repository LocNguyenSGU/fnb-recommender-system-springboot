package com.example.demo.service.impl;

import com.example.demo.dto.request.CommentRequestDTO;
import com.example.demo.dto.response.CommentResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Blog;
import com.example.demo.model.Comment;
import com.example.demo.model.User;
import com.example.demo.repository.BlogRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    
    @Override
    public CommentResponseDTO createComment(CommentRequestDTO commentRequestDTO) {
        Comment comment = mapToEntity(commentRequestDTO);
        Comment savedComment = commentRepository.save(comment);
        return mapToResponseDTO(savedComment);
    }
    
    @Override
    public CommentResponseDTO updateComment(Long id, CommentRequestDTO commentRequestDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        updateEntityFromDTO(comment, commentRequestDTO);
        Comment updatedComment = commentRepository.save(comment);
        return mapToResponseDTO(updatedComment);
    }
    
    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CommentResponseDTO> getCommentById(Long id) {
        return commentRepository.findById(id)
                .map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getAllComments() {
        return commentRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByBlogId(Long blogId) {
        return commentRepository.findByBlogIdOrderByCreatedAtDesc(blogId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByUserId(Long userId) {
        return commentRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    // Mapper methods
    private Comment mapToEntity(CommentRequestDTO dto) {
        Comment comment = new Comment();
        
        Blog blog = blogRepository.findById(dto.getBlogId())
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + dto.getBlogId()));
        comment.setBlog(blog);
        
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));
        comment.setUser(user);
        
        comment.setContent(dto.getContent());
        
        return comment;
    }
    
    private void updateEntityFromDTO(Comment comment, CommentRequestDTO dto) {
        if (dto.getBlogId() != null) {
            Blog blog = blogRepository.findById(dto.getBlogId())
                    .orElseThrow(() -> new RuntimeException("Blog not found with id: " + dto.getBlogId()));
            comment.setBlog(blog);
        }
        
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));
            comment.setUser(user);
        }
        
        comment.setContent(dto.getContent());
    }
    
    private CommentResponseDTO mapToResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        
        if (comment.getBlog() != null) {
            dto.setBlogId(comment.getBlog().getId());
        }
        
        if (comment.getUser() != null) {
            dto.setUserId(comment.getUser().getId());
            dto.setUserName(comment.getUser().getFullName());
        }
        
        dto.setContent(comment.getContent());
        dto.setReplies(comment.getReplies());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        
        return dto;
    }
}
