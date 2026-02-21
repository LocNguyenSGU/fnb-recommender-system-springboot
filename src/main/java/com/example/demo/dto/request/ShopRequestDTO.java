package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopRequestDTO {
    
    @NotNull(message = "Owner ID không được để trống")
    private Long ownerId;
    
    @NotNull(message = "Category ID không được để trống")
    private Long categoryId;
    
    @NotBlank(message = "Tên shop không được để trống")
    @Size(max = 255, message = "Tên shop không được quá 255 ký tự")
    private String name;
    
    @Size(max = 1000, message = "Địa chỉ không được quá 1000 ký tự")
    private String address;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private LocalTime openTime;
    
    private LocalTime closeTime;
    
    @Size(max = 20, message = "Trạng thái không được quá 20 ký tự")
    private String status;
    
    private List<String> images;
}
