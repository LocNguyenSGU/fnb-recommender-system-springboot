package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponseDTO {
    
    private Long id;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private List<String> images;
    private Integer likesCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
