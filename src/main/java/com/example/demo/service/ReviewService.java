package com.example.demo.service;

import com.example.demo.dto.request.ReviewRequestDTO;
import com.example.demo.dto.response.ReviewResponseDTO;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    
    ReviewResponseDTO createReview(ReviewRequestDTO reviewRequestDTO);
    
    ReviewResponseDTO updateReview(Long id, ReviewRequestDTO reviewRequestDTO);
    
    void deleteReview(Long id);
    
    Optional<ReviewResponseDTO> getReviewById(Long id);
    
    List<ReviewResponseDTO> getAllReviews();
    
    List<ReviewResponseDTO> getReviewsByShopId(Long shopId);
    
    List<ReviewResponseDTO> getReviewsByUserId(Long userId);
    
    Double getAverageRatingByShopId(Long shopId);
    
    Long countReviewsByShopId(Long shopId);
}
