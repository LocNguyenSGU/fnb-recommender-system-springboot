package com.example.demo.service.impl;

import com.example.demo.dto.request.ReviewRequestDTO;
import com.example.demo.dto.response.ReviewResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ReviewMapper;
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
    private final ReviewMapper reviewMapper;
    
    @Override
    public ReviewResponseDTO createReview(ReviewRequestDTO reviewRequestDTO) {
        Review review = reviewMapper.toEntity(reviewRequestDTO, userRepository, shopRepository);
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponseDTO(savedReview);
    }
    
    @Override
    public ReviewResponseDTO updateReview(Long id, ReviewRequestDTO reviewRequestDTO) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        
        reviewMapper.updateEntityFromDTO(review, reviewRequestDTO, userRepository, shopRepository);
        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toResponseDTO(updatedReview);
    }
    
    @Override
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewResponseDTO> getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(reviewMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByShopId(Long shopId) {
        return reviewRepository.findByShopIdOrderByCreatedAtDesc(shopId).stream()
                .map(reviewMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(reviewMapper::toResponseDTO)
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
}
