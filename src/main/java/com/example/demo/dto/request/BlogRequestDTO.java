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
public class BlogRequestDTO {
    
    @NotNull(message = "Author ID không được để trống")
    private Long authorId;
    
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    private String title;
    
    @NotBlank(message = "Nội dung không được để trống")
    private String content;
    
    private List<String> images;
    
    @Size(max = 20, message = "Trạng thái không được quá 20 ký tự")
    private String status;
}
