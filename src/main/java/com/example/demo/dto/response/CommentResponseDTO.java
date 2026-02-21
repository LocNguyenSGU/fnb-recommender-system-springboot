package com.example.demo.dto.response;

import com.example.demo.model.Reply;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {
    
    private Long id;
    private Long blogId;
    private Long userId;
    private String userName;
    private String content;
    private List<Reply> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
