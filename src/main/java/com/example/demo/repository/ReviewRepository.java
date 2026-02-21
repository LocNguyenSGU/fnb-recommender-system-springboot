package com.example.demo.repository;

import com.example.demo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByShopIdOrderByCreatedAtDesc(Long shopId);
    
    List<Review> findByUserId(Long userId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.shop.id = :shopId")
    Double findAverageRatingByShopId(@Param("shopId") Long shopId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.shop.id = :shopId")
    Long countReviewsByShopId(@Param("shopId") Long shopId);
}
