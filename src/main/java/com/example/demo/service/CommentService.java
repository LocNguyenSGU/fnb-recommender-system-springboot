package com.example.demo.service;

import com.example.demo.dto.request.CommentRequestDTO;
import com.example.demo.dto.response.CommentResponseDTO;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    
    CommentResponseDTO createComment(CommentRequestDTO commentRequestDTO);
    
    CommentResponseDTO updateComment(Long id, CommentRequestDTO commentRequestDTO);
    
    void deleteComment(Long id);
    
    Optional<CommentResponseDTO> getCommentById(Long id);
    
    List<CommentResponseDTO> getAllComments();
    
    List<CommentResponseDTO> getCommentsByBlogId(Long blogId);
    
    List<CommentResponseDTO> getCommentsByUserId(Long userId);
}
