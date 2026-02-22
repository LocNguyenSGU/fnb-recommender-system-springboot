package com.example.demo.service.integration;

import com.example.demo.dto.request.CategoryRequestDTO;
import com.example.demo.dto.request.ReviewRequestDTO;
import com.example.demo.dto.request.ShopRequestDTO;
import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.CategoryResponseDTO;
import com.example.demo.dto.response.ReviewResponseDTO;
import com.example.demo.dto.response.ShopResponseDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.model.Review;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ReviewService;
import com.example.demo.service.ShopService;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ReviewService Integration Tests")
class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    private ShopResponseDTO testShop;
    private UserResponseDTO testReviewer;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();

        // Create test owner
        UserRequestDTO ownerRequest = new UserRequestDTO();
        ownerRequest.setUsername("shopowner");
        ownerRequest.setEmail("owner@test.com");
        ownerRequest.setPassword("password");
        ownerRequest.setFullName("Shop Owner");
        UserResponseDTO owner = userService.createUser(ownerRequest);

        // Create test reviewer
        UserRequestDTO reviewerRequest = new UserRequestDTO();
        reviewerRequest.setUsername("reviewer");
        reviewerRequest.setEmail("reviewer@test.com");
        reviewerRequest.setPassword("password");
        reviewerRequest.setFullName("Reviewer User");
        testReviewer = userService.createUser(reviewerRequest);

        // Create test category
        CategoryRequestDTO categoryRequest = new CategoryRequestDTO();
        categoryRequest.setName("Vietnamese");
        categoryRequest.setDescription("Vietnamese cuisine");
        CategoryResponseDTO category = categoryService.createCategory(categoryRequest);

        // Create test shop
        ShopRequestDTO shopRequest = new ShopRequestDTO();
        shopRequest.setName("Test Restaurant");
        shopRequest.setAddress("123 Test St");
        shopRequest.setStatus("ACTIVE");
        shopRequest.setOwnerId(owner.getId());
        shopRequest.setCategoryId(category.getId());
        shopRequest.setLatitude(BigDecimal.valueOf(10.8231));
        shopRequest.setLongitude(BigDecimal.valueOf(106.6297));
        testShop = shopService.createShop(shopRequest);
    }

    @Nested
    @DisplayName("Create Review Tests")
    class CreateReviewTests {

        @Test
        @DisplayName("Should create review and persist to database")
        void shouldCreateReviewAndPersist() {
            // Given
            ReviewRequestDTO request = new ReviewRequestDTO();
            request.setRating((short) 5);
            request.setContent("Excellent food and service!");
            request.setShopId(testShop.getId());
            request.setUserId(testReviewer.getId());

            // When
            ReviewResponseDTO response = reviewService.createReview(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotNull();
            assertThat(response.getRating()).isEqualTo((short) 5);
            assertThat(response.getContent()).isEqualTo("Excellent food and service!");

            // Verify in database
            Optional<Review> savedReview = reviewRepository.findById(response.getId());
            assertThat(savedReview).isPresent();
            assertThat(savedReview.get().getRating()).isEqualTo((short) 5);
            assertThat(savedReview.get().getShop().getId()).isEqualTo(testShop.getId());
            assertThat(savedReview.get().getUser().getId()).isEqualTo(testReviewer.getId());
        }

        @Test
        @DisplayName("Should create multiple reviews for same shop")
        void shouldCreateMultipleReviewsForSameShop() {
            // Given - Create second reviewer
            UserRequestDTO user2Request = new UserRequestDTO();
            user2Request.setUsername("reviewer2");
            user2Request.setEmail("reviewer2@test.com");
            user2Request.setPassword("password");
            user2Request.setFullName("Second Reviewer");
            UserResponseDTO user2 = userService.createUser(user2Request);

            ReviewRequestDTO request1 = new ReviewRequestDTO();
            request1.setRating((short) 5);
            request1.setContent("Great!");
            request1.setShopId(testShop.getId());
            request1.setUserId(testReviewer.getId());

            ReviewRequestDTO request2 = new ReviewRequestDTO();
            request2.setRating((short) 4);
            request2.setContent("Good!");
            request2.setShopId(testShop.getId());
            request2.setUserId(user2.getId());

            // When
            ReviewResponseDTO response1 = reviewService.createReview(request1);
            ReviewResponseDTO response2 = reviewService.createReview(request2);

            // Then
            assertThat(response1.getId()).isNotEqualTo(response2.getId());

            List<ReviewResponseDTO> shopReviews = reviewService.getReviewsByShopId(testShop.getId());
            assertThat(shopReviews).hasSize(2);
        }

        @Test
        @DisplayName("Should create reviews with different ratings")
        void shouldCreateReviewsWithDifferentRatings() {
            // Given
            UserRequestDTO user2Request = new UserRequestDTO();
            user2Request.setUsername("reviewer2");
            user2Request.setEmail("reviewer2@test.com");
            user2Request.setPassword("password");
            user2Request.setFullName("Second Reviewer");
            UserResponseDTO user2 = userService.createUser(user2Request);

            ReviewRequestDTO excellentReview = new ReviewRequestDTO();
            excellentReview.setRating((short) 5);
            excellentReview.setContent("Excellent!");
            excellentReview.setShopId(testShop.getId());
            excellentReview.setUserId(testReviewer.getId());

            ReviewRequestDTO poorReview = new ReviewRequestDTO();
            poorReview.setRating((short) 1);
            poorReview.setContent("Disappointing");
            poorReview.setShopId(testShop.getId());
            poorReview.setUserId(user2.getId());

            // When
            reviewService.createReview(excellentReview);
            reviewService.createReview(poorReview);

            // Then
            List<ReviewResponseDTO> reviews = reviewService.getReviewsByShopId(testShop.getId());
            assertThat(reviews)
                    .extracting(ReviewResponseDTO::getRating)
                    .containsExactlyInAnyOrder((short) 5, (short) 1);
        }
    }

    @Nested
    @DisplayName("Update Review Tests")
    class UpdateReviewTests {

        @Test
        @DisplayName("Should update review and persist changes")
        void shouldUpdateReviewAndPersist() {
            // Given
            ReviewRequestDTO createRequest = new ReviewRequestDTO();
            createRequest.setRating((short) 3);
            createRequest.setContent("Average");
            createRequest.setShopId(testShop.getId());
            createRequest.setUserId(testReviewer.getId());
            ReviewResponseDTO created = reviewService.createReview(createRequest);

            ReviewRequestDTO updateRequest = new ReviewRequestDTO();
            updateRequest.setRating((short) 5);
            updateRequest.setContent("Changed my mind - excellent!");
            updateRequest.setShopId(testShop.getId());
            updateRequest.setUserId(testReviewer.getId());

            // When
            ReviewResponseDTO updated = reviewService.updateReview(created.getId(), updateRequest);

            // Then
            assertThat(updated.getRating()).isEqualTo((short) 5);
            assertThat(updated.getContent()).isEqualTo("Changed my mind - excellent!");

            // Verify in database
            Optional<Review> savedReview = reviewRepository.findById(created.getId());
            assertThat(savedReview).isPresent();
            assertThat(savedReview.get().getRating()).isEqualTo((short) 5);
        }

        @Test
        @DisplayName("Should update rating without changing comment")
        void shouldUpdateRatingWithoutChangingComment() {
            // Given
            ReviewRequestDTO createRequest = new ReviewRequestDTO();
            createRequest.setRating((short) 3);
            createRequest.setContent("Original comment");
            createRequest.setShopId(testShop.getId());
            createRequest.setUserId(testReviewer.getId());
            ReviewResponseDTO created = reviewService.createReview(createRequest);

            ReviewRequestDTO updateRequest = new ReviewRequestDTO();
            updateRequest.setRating((short) 4);
            updateRequest.setContent("Original comment");
            updateRequest.setShopId(testShop.getId());
            updateRequest.setUserId(testReviewer.getId());

            // When
            ReviewResponseDTO updated = reviewService.updateReview(created.getId(), updateRequest);

            // Then
            assertThat(updated.getRating()).isEqualTo((short) 4);
            assertThat(updated.getContent()).isEqualTo("Original comment");
        }
    }

    @Nested
    @DisplayName("Get Review Tests")
    class GetReviewTests {

        @Test
        @DisplayName("Should retrieve review by ID")
        void shouldRetrieveReviewById() {
            // Given
            ReviewRequestDTO request = new ReviewRequestDTO();
            request.setRating((short) 4);
            request.setContent("Very good");
            request.setShopId(testShop.getId());
            request.setUserId(testReviewer.getId());
            ReviewResponseDTO created = reviewService.createReview(request);

            // When
            ReviewResponseDTO retrieved = reviewService.getReviewById(created.getId()).orElseThrow();

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getId()).isEqualTo(created.getId());
            assertThat(retrieved.getRating()).isEqualTo((short) 4);
        }

        @Test
        @DisplayName("Should retrieve all reviews")
        void shouldRetrieveAllReviews() {
            // Given
            UserRequestDTO user2Request = new UserRequestDTO();
            user2Request.setUsername("reviewer2");
            user2Request.setEmail("reviewer2@test.com");
            user2Request.setPassword("password");
            user2Request.setFullName("Second Reviewer");
            UserResponseDTO user2 = userService.createUser(user2Request);

            ReviewRequestDTO request1 = new ReviewRequestDTO();
            request1.setRating((short) 5);
            request1.setContent("Review 1");
            request1.setShopId(testShop.getId());
            request1.setUserId(testReviewer.getId());

            ReviewRequestDTO request2 = new ReviewRequestDTO();
            request2.setRating((short) 4);
            request2.setContent("Review 2");
            request2.setShopId(testShop.getId());
            request2.setUserId(user2.getId());

            reviewService.createReview(request1);
            reviewService.createReview(request2);

            // When
            List<ReviewResponseDTO> reviews = reviewService.getAllReviews();

            // Then
            assertThat(reviews).hasSize(2);
        }

        @Test
        @DisplayName("Should retrieve reviews by shop ID")
        void shouldRetrieveReviewsByShopId() {
            // Given
            ReviewRequestDTO request = new ReviewRequestDTO();
            request.setRating((short) 5);
            request.setContent("Shop review");
            request.setShopId(testShop.getId());
            request.setUserId(testReviewer.getId());

            reviewService.createReview(request);

            // When
            List<ReviewResponseDTO> shopReviews = reviewService.getReviewsByShopId(testShop.getId());

            // Then
            assertThat(shopReviews).hasSize(1);
            assertThat(shopReviews.get(0).getShopId()).isEqualTo(testShop.getId());
        }

        @Test
        @DisplayName("Should retrieve reviews by user ID")
        void shouldRetrieveReviewsByUserId() {
            // Given
            ReviewRequestDTO request = new ReviewRequestDTO();
            request.setRating((short) 5);
            request.setContent("User review");
            request.setShopId(testShop.getId());
            request.setUserId(testReviewer.getId());

            reviewService.createReview(request);

            // When
            List<ReviewResponseDTO> userReviews = reviewService.getReviewsByUserId(testReviewer.getId());

            // Then
            assertThat(userReviews).hasSize(1);
            assertThat(userReviews.get(0).getUserId()).isEqualTo(testReviewer.getId());
        }
    }

    @Nested
    @DisplayName("Delete Review Tests")
    class DeleteReviewTests {

        @Test
        @DisplayName("Should delete review from database")
        void shouldDeleteReviewFromDatabase() {
            // Given
            ReviewRequestDTO request = new ReviewRequestDTO();
            request.setRating((short) 3);
            request.setContent("To be deleted");
            request.setShopId(testShop.getId());
            request.setUserId(testReviewer.getId());
            ReviewResponseDTO created = reviewService.createReview(request);

            // When
            reviewService.deleteReview(created.getId());

            // Then
            Optional<Review> deleted = reviewRepository.findById(created.getId());
            assertThat(deleted).isNotPresent();
        }
    }

    @Nested
    @DisplayName("Average Rating Tests")
    class AverageRatingTests {

        @Test
        @DisplayName("Should calculate average rating correctly")
        void shouldCalculateAverageRatingCorrectly() {
            // Given - Create multiple reviewers
            UserRequestDTO user2Request = new UserRequestDTO();
            user2Request.setUsername("reviewer2");
            user2Request.setEmail("reviewer2@test.com");
            user2Request.setPassword("password");
            user2Request.setFullName("Reviewer 2");
            UserResponseDTO user2 = userService.createUser(user2Request);

            UserRequestDTO user3Request = new UserRequestDTO();
            user3Request.setUsername("reviewer3");
            user3Request.setEmail("reviewer3@test.com");
            user3Request.setPassword("password");
            user3Request.setFullName("Reviewer 3");
            UserResponseDTO user3 = userService.createUser(user3Request);

            // Create reviews: ratings 5, 4, 3 -> average 4.0
            ReviewRequestDTO review1 = new ReviewRequestDTO();
            review1.setRating((short) 5);
            review1.setContent("Excellent");
            review1.setShopId(testShop.getId());
            review1.setUserId(testReviewer.getId());

            ReviewRequestDTO review2 = new ReviewRequestDTO();
            review2.setRating((short) 4);
            review2.setContent("Good");
            review2.setShopId(testShop.getId());
            review2.setUserId(user2.getId());

            ReviewRequestDTO review3 = new ReviewRequestDTO();
            review3.setRating((short) 3);
            review3.setContent("Average");
            review3.setShopId(testShop.getId());
            review3.setUserId(user3.getId());

            reviewService.createReview(review1);
            reviewService.createReview(review2);
            reviewService.createReview(review3);

            // When
            Double averageRating = reviewService.getAverageRatingByShopId(testShop.getId());

            // Then
            assertThat(averageRating).isNotNull();
            assertThat(averageRating).isCloseTo(4.0, within(0.01));
        }

        @Test
        @DisplayName("Should return null or zero when no reviews exist")
        void shouldReturnNullOrZeroWhenNoReviewsExist() {
            // When
            Double averageRating = reviewService.getAverageRatingByShopId(testShop.getId());

            // Then
            assertThat(averageRating).isIn(null, 0.0);
        }

        @Test
        @DisplayName("Should handle single review")
        void shouldHandleSingleReview() {
            // Given
            ReviewRequestDTO request = new ReviewRequestDTO();
            request.setRating((short) 5);
            request.setContent("Only review");
            request.setShopId(testShop.getId());
            request.setUserId(testReviewer.getId());

            reviewService.createReview(request);

            // When
            Double averageRating = reviewService.getAverageRatingByShopId(testShop.getId());

            // Then
            assertThat(averageRating).isEqualTo(5.0);
        }
    }

    @Nested
    @DisplayName("Review Count Tests")
    class ReviewCountTests {

        @Test
        @DisplayName("Should count reviews for shop correctly")
        void shouldCountReviewsForShopCorrectly() {
            // Given
            UserRequestDTO user2Request = new UserRequestDTO();
            user2Request.setUsername("reviewer2");
            user2Request.setEmail("reviewer2@test.com");
            user2Request.setPassword("password");
            user2Request.setFullName("Reviewer 2");
            UserResponseDTO user2 = userService.createUser(user2Request);

            ReviewRequestDTO review1 = new ReviewRequestDTO();
            review1.setRating((short) 5);
            review1.setContent("Review 1");
            review1.setShopId(testShop.getId());
            review1.setUserId(testReviewer.getId());

            ReviewRequestDTO review2 = new ReviewRequestDTO();
            review2.setRating((short) 4);
            review2.setContent("Review 2");
            review2.setShopId(testShop.getId());
            review2.setUserId(user2.getId());

            reviewService.createReview(review1);
            reviewService.createReview(review2);

            // When
            Long count = reviewService.countReviewsByShopId(testShop.getId());

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return zero when no reviews exist")
        void shouldReturnZeroWhenNoReviewsExist() {
            // When
            Long count = reviewService.countReviewsByShopId(testShop.getId());

            // Then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should maintain shop-review relationship")
        void shouldMaintainShopReviewRelationship() {
            // Given
            ReviewRequestDTO request = new ReviewRequestDTO();
            request.setRating((short) 5);
            request.setContent("Related review");
            request.setShopId(testShop.getId());
            request.setUserId(testReviewer.getId());

            // When
            ReviewResponseDTO created = reviewService.createReview(request);

            // Then
            Optional<Review> savedReview = reviewRepository.findById(created.getId());
            assertThat(savedReview).isPresent();
            assertThat(savedReview.get().getShop()).isNotNull();
            assertThat(savedReview.get().getShop().getId()).isEqualTo(testShop.getId());
            assertThat(savedReview.get().getShop().getName()).isEqualTo(testShop.getName());
        }

        @Test
        @DisplayName("Should maintain user-review relationship")
        void shouldMaintainUserReviewRelationship() {
            // Given
            ReviewRequestDTO request = new ReviewRequestDTO();
            request.setRating((short) 5);
            request.setContent("User's review");
            request.setShopId(testShop.getId());
            request.setUserId(testReviewer.getId());

            // When
            ReviewResponseDTO created = reviewService.createReview(request);

            // Then
            Optional<Review> savedReview = reviewRepository.findById(created.getId());
            assertThat(savedReview).isPresent();
            assertThat(savedReview.get().getUser()).isNotNull();
            assertThat(savedReview.get().getUser().getId()).isEqualTo(testReviewer.getId());
        }

        @Test
        @DisplayName("Should filter reviews by different shops")
        void shouldFilterReviewsByDifferentShops() {
            // Given - Create second shop
            UserRequestDTO owner2Request = new UserRequestDTO();
            owner2Request.setUsername("owner2");
            owner2Request.setEmail("owner2@test.com");
            owner2Request.setPassword("password");
            owner2Request.setFullName("Owner 2");
            UserResponseDTO owner2 = userService.createUser(owner2Request);

            CategoryRequestDTO categoryRequest = new CategoryRequestDTO();
            categoryRequest.setName("Japanese");
            categoryRequest.setDescription("Japanese cuisine");
            CategoryResponseDTO category = categoryService.createCategory(categoryRequest);

            ShopRequestDTO shop2Request = new ShopRequestDTO();
            shop2Request.setName("Second Restaurant");
            shop2Request.setAddress("456 Test St");
            shop2Request.setStatus("ACTIVE");
            shop2Request.setOwnerId(owner2.getId());
            shop2Request.setCategoryId(category.getId());
            shop2Request.setLatitude(BigDecimal.valueOf(10.8231));
            shop2Request.setLongitude(BigDecimal.valueOf(106.6297));
            ShopResponseDTO shop2 = shopService.createShop(shop2Request);

            // Create reviews for both shops
            ReviewRequestDTO review1 = new ReviewRequestDTO();
            review1.setRating((short) 5);
            review1.setContent("Shop 1 review");
            review1.setShopId(testShop.getId());
            review1.setUserId(testReviewer.getId());

            ReviewRequestDTO review2 = new ReviewRequestDTO();
            review2.setRating((short) 4);
            review2.setContent("Shop 2 review");
            review2.setShopId(shop2.getId());
            review2.setUserId(testReviewer.getId());

            reviewService.createReview(review1);
            reviewService.createReview(review2);

            // When
            List<ReviewResponseDTO> shop1Reviews = reviewService.getReviewsByShopId(testShop.getId());
            List<ReviewResponseDTO> shop2Reviews = reviewService.getReviewsByShopId(shop2.getId());

            // Then
            assertThat(shop1Reviews).hasSize(1);
            assertThat(shop2Reviews).hasSize(1);
            assertThat(shop1Reviews.get(0).getContent()).isEqualTo("Shop 1 review");
            assertThat(shop2Reviews.get(0).getContent()).isEqualTo("Shop 2 review");
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @Test
        @DisplayName("Should maintain referential integrity")
        void shouldMaintainReferentialIntegrity() {
            // Given
            ReviewRequestDTO request = new ReviewRequestDTO();
            request.setRating((short) 5);
            request.setContent("Integrity test");
            request.setShopId(testShop.getId());
            request.setUserId(testReviewer.getId());

            // When
            ReviewResponseDTO created = reviewService.createReview(request);

            // Then
            Optional<Review> savedReview = reviewRepository.findById(created.getId());
            assertThat(savedReview).isPresent();
            assertThat(savedReview.get().getShop()).isNotNull();
            assertThat(savedReview.get().getUser()).isNotNull();
        }
    }
}
