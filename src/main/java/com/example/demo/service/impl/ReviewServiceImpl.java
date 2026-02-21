package com.example.demo.service.impl;

import com.example.demo.dto.request.ReviewRequestDTO;
import com.example.demo.dto.response.ReviewResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Review;
import com.example.demo.model.Shop;
import com.example.demo.model.User;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    
    @Override
    public ReviewResponseDTO createReview(ReviewRequestDTO reviewRequestDTO) {
        Review review = mapToEntity(reviewRequestDTO);
        Review savedReview = reviewRepository.save(review);
        return mapToResponseDTO(savedReview);
    }
    
    @Override
    public ReviewResponseDTO updateReview(Long id, ReviewRequestDTO reviewRequestDTO) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        
        updateEntityFromDTO(review, reviewRequestDTO);
        Review updatedReview = reviewRepository.save(review);
        return mapToResponseDTO(updatedReview);
    }
    
    @Override
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewResponseDTO> getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByShopId(Long shopId) {
        return reviewRepository.findByShopIdOrderByCreatedAtDesc(shopId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByShopId(Long shopId) {
        return reviewRepository.findAverageRatingByShopId(shopId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countReviewsByShopId(Long shopId) {
        return reviewRepository.countReviewsByShopId(shopId);
    }
    
    // Mapper methods
    private Review mapToEntity(ReviewRequestDTO dto) {
        Review review = new Review();
        
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));
        review.setUser(user);
        
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + dto.getShopId()));
        review.setShop(shop);
        
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        
        return review;
    }
    
    private void updateEntityFromDTO(Review review, ReviewRequestDTO dto) {
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));
            review.setUser(user);
        }
        
        if (dto.getShopId() != null) {
            Shop shop = shopRepository.findById(dto.getShopId())
                    .orElseThrow(() -> new RuntimeException("Shop not found with id: " + dto.getShopId()));
            review.setShop(shop);
        }
        
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
    }
    
    private ReviewResponseDTO mapToResponseDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        
        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
            dto.setUserName(review.getUser().getFullName());
        }
        
        if (review.getShop() != null) {
            dto.setShopId(review.getShop().getId());
            dto.setShopName(review.getShop().getName());
        }
        
        dto.setRating(review.getRating());
        dto.setContent(review.getContent());
        dto.setReplies(review.getReplies());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        
        return dto;
    }
}
