package com.example.demo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {
    
    @NotNull(message = "User ID không được để trống")
    private Long userId;
    
    @NotNull(message = "Shop ID không được để trống")
    private Long shopId;
    
    @NotNull(message = "Đánh giá không được để trống")
    @Min(value = 1, message = "Đánh giá phải từ 1-5")
    @Max(value = 5, message = "Đánh giá phải từ 1-5")
    private Short rating;
    
    @Size(max = 2000, message = "Nội dung không được quá 2000 ký tự")
    private String content;
}
