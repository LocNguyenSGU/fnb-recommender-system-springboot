package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponseDTO {
    
    private Long id;
    private Long shopId;
    private String shopName;
    private String name;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
