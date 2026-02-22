package com.example.demo.service.impl;

import com.example.demo.dto.request.ReviewRequestDTO;
import com.example.demo.dto.response.ReviewResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ReviewMapper;
import com.example.demo.model.Review;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private ReviewRequestDTO reviewRequestDTO;
    private ReviewResponseDTO reviewResponseDTO;

    @BeforeEach
    void setUp() {
        review = new Review();
        review.setId(1L);
        review.setRating((short) 5);
        review.setContent("Excellent food and service!");

        reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setRating((short) 5);
        reviewRequestDTO.setContent("Excellent food and service!");
        reviewRequestDTO.setShopId(1L);
        reviewRequestDTO.setUserId(1L);

        reviewResponseDTO = new ReviewResponseDTO();
        reviewResponseDTO.setId(1L);
        reviewResponseDTO.setRating((short) 5);
        reviewResponseDTO.setContent("Excellent food and service!");
    }

    @Nested
    @DisplayName("Create Review Tests")
    class CreateReviewTests {

        @Test
        @DisplayName("Should create review successfully")
        void shouldCreateReviewSuccessfully() {
            // Given
            when(reviewMapper.toEntity(any(ReviewRequestDTO.class), any(UserRepository.class), any(ShopRepository.class)))
                    .thenReturn(review);
            when(reviewRepository.save(any(Review.class))).thenReturn(review);
            when(reviewMapper.toResponseDTO(any(Review.class))).thenReturn(reviewResponseDTO);

            // When
            ReviewResponseDTO result = reviewService.createReview(reviewRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRating()).isEqualTo((short) 5);

            verify(reviewMapper).toEntity(reviewRequestDTO, userRepository, shopRepository);
            verify(reviewRepository).save(review);
            verify(reviewMapper).toResponseDTO(review);
        }

        @Test
        @DisplayName("Should create review with minimum rating")
        void shouldCreateReviewWithMinimumRating() {
            // Given
            reviewRequestDTO.setRating((short) 1);
            review.setRating((short) 1);

            when(reviewMapper.toEntity(any(ReviewRequestDTO.class), any(UserRepository.class), any(ShopRepository.class)))
                    .thenReturn(review);
            when(reviewRepository.save(any(Review.class))).thenReturn(review);
            when(reviewMapper.toResponseDTO(any(Review.class))).thenReturn(reviewResponseDTO);

            // When
            ReviewResponseDTO result = reviewService.createReview(reviewRequestDTO);

            // Then
            assertThat(result).isNotNull();
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("Should create review without comment")
        void shouldCreateReviewWithoutComment() {
            // Given
            reviewRequestDTO.setContent(null);

            when(reviewMapper.toEntity(any(ReviewRequestDTO.class), any(UserRepository.class), any(ShopRepository.class)))
                    .thenReturn(review);
            when(reviewRepository.save(any(Review.class))).thenReturn(review);
            when(reviewMapper.toResponseDTO(any(Review.class))).thenReturn(reviewResponseDTO);

            // When
            ReviewResponseDTO result = reviewService.createReview(reviewRequestDTO);

            // Then
            assertThat(result).isNotNull();
            verify(reviewRepository).save(any(Review.class));
        }
    }

    @Nested
    @DisplayName("Update Review Tests")
    class UpdateReviewTests {

        @Test
        @DisplayName("Should update review successfully")
        void shouldUpdateReviewSuccessfully() {
            // Given
            Long reviewId = 1L;
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            doNothing().when(reviewMapper).updateEntityFromDTO(any(Review.class), any(ReviewRequestDTO.class),
                    any(UserRepository.class), any(ShopRepository.class));
            when(reviewRepository.save(any(Review.class))).thenReturn(review);
            when(reviewMapper.toResponseDTO(any(Review.class))).thenReturn(reviewResponseDTO);

            // When
            ReviewResponseDTO result = reviewService.updateReview(reviewId, reviewRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(reviewId);

            verify(reviewRepository).findById(reviewId);
            verify(reviewMapper).updateEntityFromDTO(review, reviewRequestDTO, userRepository, shopRepository);
            verify(reviewRepository).save(review);
        }

        @Test
        @DisplayName("Should throw exception when review not found")
        void shouldThrowExceptionWhenReviewNotFound() {
            // Given
            Long reviewId = 999L;
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> reviewService.updateReview(reviewId, reviewRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Review")
                    .hasMessageContaining("id");

            verify(reviewRepository).findById(reviewId);
            verify(reviewRepository, never()).save(any(Review.class));
        }

        @Test
        @DisplayName("Should update rating only")
        void shouldUpdateRatingOnly() {
            // Given
            Long reviewId = 1L;
            ReviewRequestDTO ratingUpdate = new ReviewRequestDTO();
            ratingUpdate.setRating((short) 4);

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            doNothing().when(reviewMapper).updateEntityFromDTO(any(Review.class), any(ReviewRequestDTO.class),
                    any(UserRepository.class), any(ShopRepository.class));
            when(reviewRepository.save(any(Review.class))).thenReturn(review);
            when(reviewMapper.toResponseDTO(any(Review.class))).thenReturn(reviewResponseDTO);

            // When
            ReviewResponseDTO result = reviewService.updateReview(reviewId, ratingUpdate);

            // Then
            assertThat(result).isNotNull();
            verify(reviewMapper).updateEntityFromDTO(review, ratingUpdate, userRepository, shopRepository);
        }
    }

    @Nested
    @DisplayName("Delete Review Tests")
    class DeleteReviewTests {

        @Test
        @DisplayName("Should delete review successfully")
        void shouldDeleteReviewSuccessfully() {
            // Given
            Long reviewId = 1L;
            doNothing().when(reviewRepository).deleteById(reviewId);

            // When
            reviewService.deleteReview(reviewId);

            // Then
            verify(reviewRepository).deleteById(reviewId);
        }
    }

    @Nested
    @DisplayName("Get Review Tests")
    class GetReviewTests {

        @Test
        @DisplayName("Should get review by id successfully")
        void shouldGetReviewByIdSuccessfully() {
            // Given
            Long reviewId = 1L;
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reviewMapper.toResponseDTO(any(Review.class))).thenReturn(reviewResponseDTO);

            // When
            Optional<ReviewResponseDTO> result = reviewService.getReviewById(reviewId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(reviewId);

            verify(reviewRepository).findById(reviewId);
            verify(reviewMapper).toResponseDTO(review);
        }

        @Test
        @DisplayName("Should return empty when review not found")
        void shouldReturnEmptyWhenReviewNotFound() {
            // Given
            Long reviewId = 999L;
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            // When
            Optional<ReviewResponseDTO> result = reviewService.getReviewById(reviewId);

            // Then
            assertThat(result).isEmpty();
            verify(reviewRepository).findById(reviewId);
            verify(reviewMapper, never()).toResponseDTO(any(Review.class));
        }

        @Test
        @DisplayName("Should get all reviews successfully")
        void shouldGetAllReviewsSuccessfully() {
            // Given
            Review review2 = new Review();
            review2.setId(2L);
            review2.setRating((short) 4);

            ReviewResponseDTO reviewResponseDTO2 = new ReviewResponseDTO();
            reviewResponseDTO2.setId(2L);
            reviewResponseDTO2.setRating((short) 4);

            List<Review> reviews = Arrays.asList(review, review2);

            when(reviewRepository.findAll()).thenReturn(reviews);
            when(reviewMapper.toResponseDTO(review)).thenReturn(reviewResponseDTO);
            when(reviewMapper.toResponseDTO(review2)).thenReturn(reviewResponseDTO2);

            // When
            List<ReviewResponseDTO> result = reviewService.getAllReviews();

            // Then
            assertThat(result).hasSize(2);
            verify(reviewRepository).findAll();
            verify(reviewMapper, times(2)).toResponseDTO(any(Review.class));
        }
    }

    @Nested
    @DisplayName("Get Reviews By Shop Tests")
    class GetReviewsByShopTests {

        @Test
        @DisplayName("Should get reviews by shop id successfully")
        void shouldGetReviewsByShopIdSuccessfully() {
            // Given
            Long shopId = 1L;
            List<Review> shopReviews = Arrays.asList(review);

            when(reviewRepository.findByShopIdOrderByCreatedAtDesc(shopId)).thenReturn(shopReviews);
            when(reviewMapper.toResponseDTO(any(Review.class))).thenReturn(reviewResponseDTO);

            // When
            List<ReviewResponseDTO> result = reviewService.getReviewsByShopId(shopId);

            // Then
            assertThat(result).hasSize(1);
            verify(reviewRepository).findByShopIdOrderByCreatedAtDesc(shopId);
        }

        @Test
        @DisplayName("Should return empty list when shop has no reviews")
        void shouldReturnEmptyListWhenShopHasNoReviews() {
            // Given
            Long shopId = 999L;
            when(reviewRepository.findByShopIdOrderByCreatedAtDesc(shopId)).thenReturn(Arrays.asList());

            // When
            List<ReviewResponseDTO> result = reviewService.getReviewsByShopId(shopId);

            // Then
            assertThat(result).isEmpty();
            verify(reviewRepository).findByShopIdOrderByCreatedAtDesc(shopId);
        }

        @Test
        @DisplayName("Should return reviews ordered by created date")
        void shouldReturnReviewsOrderedByCreatedDate() {
            // Given
            Long shopId = 1L;
            Review review2 = new Review();
            review2.setId(2L);
            review2.setRating((short) 4);

            List<Review> shopReviews = Arrays.asList(review2, review);

            when(reviewRepository.findByShopIdOrderByCreatedAtDesc(shopId)).thenReturn(shopReviews);
            when(reviewMapper.toResponseDTO(any(Review.class))).thenReturn(reviewResponseDTO);

            // When
            List<ReviewResponseDTO> result = reviewService.getReviewsByShopId(shopId);

            // Then
            assertThat(result).hasSize(2);
            verify(reviewRepository).findByShopIdOrderByCreatedAtDesc(shopId);
        }
    }

    @Nested
    @DisplayName("Get Reviews By User Tests")
    class GetReviewsByUserTests {

        @Test
        @DisplayName("Should get reviews by user id successfully")
        void shouldGetReviewsByUserIdSuccessfully() {
            // Given
            Long userId = 1L;
            List<Review> userReviews = Arrays.asList(review);

            when(reviewRepository.findByUserId(userId)).thenReturn(userReviews);
            when(reviewMapper.toResponseDTO(any(Review.class))).thenReturn(reviewResponseDTO);

            // When
            List<ReviewResponseDTO> result = reviewService.getReviewsByUserId(userId);

            // Then
            assertThat(result).hasSize(1);
            verify(reviewRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should return empty list when user has no reviews")
        void shouldReturnEmptyListWhenUserHasNoReviews() {
            // Given
            Long userId = 999L;
            when(reviewRepository.findByUserId(userId)).thenReturn(Arrays.asList());

            // When
            List<ReviewResponseDTO> result = reviewService.getReviewsByUserId(userId);

            // Then
            assertThat(result).isEmpty();
            verify(reviewRepository).findByUserId(userId);
        }
    }

    @Nested
    @DisplayName("Get Average Rating Tests")
    class GetAverageRatingTests {

        @Test
        @DisplayName("Should get average rating by shop id successfully")
        void shouldGetAverageRatingByShopIdSuccessfully() {
            // Given
            Long shopId = 1L;
            Double averageRating = 4.5;

            when(reviewRepository.findAverageRatingByShopId(shopId)).thenReturn(averageRating);

            // When
            Double result = reviewService.getAverageRatingByShopId(shopId);

            // Then
            assertThat(result).isEqualTo(4.5);
            verify(reviewRepository).findAverageRatingByShopId(shopId);
        }

        @Test
        @DisplayName("Should return null when shop has no reviews")
        void shouldReturnNullWhenShopHasNoReviews() {
            // Given
            Long shopId = 999L;
            when(reviewRepository.findAverageRatingByShopId(shopId)).thenReturn(null);

            // When
            Double result = reviewService.getAverageRatingByShopId(shopId);

            // Then
            assertThat(result).isNull();
            verify(reviewRepository).findAverageRatingByShopId(shopId);
        }

        @Test
        @DisplayName("Should handle perfect rating")
        void shouldHandlePerfectRating() {
            // Given
            Long shopId = 1L;
            when(reviewRepository.findAverageRatingByShopId(shopId)).thenReturn(5.0);

            // When
            Double result = reviewService.getAverageRatingByShopId(shopId);

            // Then
            assertThat(result).isEqualTo(5.0);
            verify(reviewRepository).findAverageRatingByShopId(shopId);
        }

        @Test
        @DisplayName("Should handle low rating")
        void shouldHandleLowRating() {
            // Given
            Long shopId = 1L;
            when(reviewRepository.findAverageRatingByShopId(shopId)).thenReturn(1.5);

            // When
            Double result = reviewService.getAverageRatingByShopId(shopId);

            // Then
            assertThat(result).isEqualTo(1.5);
            verify(reviewRepository).findAverageRatingByShopId(shopId);
        }
    }

    @Nested
    @DisplayName("Count Reviews Tests")
    class CountReviewsTests {

        @Test
        @DisplayName("Should count reviews by shop id successfully")
        void shouldCountReviewsByShopIdSuccessfully() {
            // Given
            Long shopId = 1L;
            Long count = 10L;

            when(reviewRepository.countReviewsByShopId(shopId)).thenReturn(count);

            // When
            Long result = reviewService.countReviewsByShopId(shopId);

            // Then
            assertThat(result).isEqualTo(10L);
            verify(reviewRepository).countReviewsByShopId(shopId);
        }

        @Test
        @DisplayName("Should return zero when shop has no reviews")
        void shouldReturnZeroWhenShopHasNoReviews() {
            // Given
            Long shopId = 999L;
            when(reviewRepository.countReviewsByShopId(shopId)).thenReturn(0L);

            // When
            Long result = reviewService.countReviewsByShopId(shopId);

            // Then
            assertThat(result).isEqualTo(0L);
            verify(reviewRepository).countReviewsByShopId(shopId);
        }

        @Test
        @DisplayName("Should handle large review count")
        void shouldHandleLargeReviewCount() {
            // Given
            Long shopId = 1L;
            when(reviewRepository.countReviewsByShopId(shopId)).thenReturn(10000L);

            // When
            Long result = reviewService.countReviewsByShopId(shopId);

            // Then
            assertThat(result).isEqualTo(10000L);
            verify(reviewRepository).countReviewsByShopId(shopId);
        }
    }
}
