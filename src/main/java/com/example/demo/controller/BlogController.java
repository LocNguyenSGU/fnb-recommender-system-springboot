package com.example.demo.controller;

import com.example.demo.dto.request.BlogRequestDTO;
import com.example.demo.dto.response.BlogResponseDTO;
import com.example.demo.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {
    
    private final BlogService blogService;
    
    @GetMapping
    public ResponseEntity<List<BlogResponseDTO>> getAllBlogs() {
        return ResponseEntity.ok(blogService.getAllBlogs());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BlogResponseDTO> getBlogById(@PathVariable Long id) {
        return blogService.getBlogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<BlogResponseDTO>> getBlogsByAuthorId(@PathVariable Long authorId) {
        return ResponseEntity.ok(blogService.getBlogsByAuthorId(authorId));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BlogResponseDTO>> getBlogsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(blogService.getBlogsByStatus(status));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<BlogResponseDTO>> searchBlogsByTitle(@RequestParam String title) {
        return ResponseEntity.ok(blogService.searchBlogsByTitle(title));
    }
    
    @GetMapping("/top-liked")
    public ResponseEntity<List<BlogResponseDTO>> getTopLikedBlogs() {
        return ResponseEntity.ok(blogService.getTopLikedBlogs());
    }
    
    @PostMapping
    public ResponseEntity<BlogResponseDTO> createBlog(@Valid @RequestBody BlogRequestDTO blogRequestDTO) {
        BlogResponseDTO created = blogService.createBlog(blogRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BlogResponseDTO> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogRequestDTO blogRequestDTO) {
        BlogResponseDTO updated = blogService.updateBlog(id, blogRequestDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> incrementLikesCount(@PathVariable Long id) {
        blogService.incrementLikesCount(id);
        return ResponseEntity.ok().build();
    }
}
