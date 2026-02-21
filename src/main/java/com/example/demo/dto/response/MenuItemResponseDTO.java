package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponseDTO {
    
    private Long id;
    private Long menuId;
    private String menuName;
    private String name;
    private String description;
    private BigDecimal price;
    private List<String> images;
    private Boolean isAvailable;
    private Boolean isHot;
    private Boolean isSignature;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
