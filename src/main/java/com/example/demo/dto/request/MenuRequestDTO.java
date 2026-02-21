package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequestDTO {
    
    @NotNull(message = "Shop ID không được để trống")
    private Long shopId;
    
    @NotBlank(message = "Tên menu không được để trống")
    @Size(max = 100, message = "Tên menu không được quá 100 ký tự")
    private String name;
    
    private List<String> images;
}
