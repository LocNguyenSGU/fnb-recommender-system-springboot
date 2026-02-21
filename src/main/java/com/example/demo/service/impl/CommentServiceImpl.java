package com.example.demo.service.impl;

import com.example.demo.dto.request.CommentRequestDTO;
import com.example.demo.dto.response.CommentResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CommentMapper;
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
    private final CommentMapper commentMapper;
    
    @Override
    public CommentResponseDTO createComment(CommentRequestDTO commentRequestDTO) {
        Comment comment = commentMapper.toEntity(commentRequestDTO, blogRepository, userRepository);
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponseDTO(savedComment);
    }
    
    @Override
    public CommentResponseDTO updateComment(Long id, CommentRequestDTO commentRequestDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        
        commentMapper.updateEntityFromDTO(comment, commentRequestDTO, blogRepository, userRepository);
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toResponseDTO(updatedComment);
    }
    
    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CommentResponseDTO> getCommentById(Long id) {
        return commentRepository.findById(id)
                .map(commentMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getAllComments() {
        return commentRepository.findAll().stream()
                .map(commentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByBlogId(Long blogId) {
        return commentRepository.findByBlogIdOrderByCreatedAtDesc(blogId).stream()
                .map(commentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByUserId(Long userId) {
        return commentRepository.findByUserId(userId).stream()
                .map(commentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
