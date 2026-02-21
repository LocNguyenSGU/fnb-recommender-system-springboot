package com.example.demo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequestDTO {
    
    @NotNull(message = "Menu ID không được để trống")
    private Long menuId;
    
    @NotBlank(message = "Tên món không được để trống")
    @Size(max = 100, message = "Tên món không được quá 100 ký tự")
    private String name;
    
    @Size(max = 1000, message = "Mô tả không được quá 1000 ký tự")
    private String description;
    
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;
    
    private List<String> images;
    
    private Boolean isAvailable;
    
    private Boolean isHot;
    
    private Boolean isSignature;
}
