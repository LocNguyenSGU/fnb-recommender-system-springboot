package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponseDTO {
    
    private Long id;
    private Long ownerId;
    private String ownerName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String status;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
