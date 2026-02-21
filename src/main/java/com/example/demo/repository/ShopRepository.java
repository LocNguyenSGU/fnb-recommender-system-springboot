package com.example.demo.repository;

import com.example.demo.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    
    List<Shop> findByOwnerId(Long ownerId);
    
    List<Shop> findByCategoryId(Long categoryId);
    
    List<Shop> findByStatus(String status);
    
    List<Shop> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT s FROM Shop s WHERE s.status = :status ORDER BY s.createdAt DESC")
    List<Shop> findByStatusOrderByCreatedAtDesc(@Param("status") String status);
    
    @Query(value = "SELECT * FROM shops s WHERE " +
           "ST_DWithin(ST_MakePoint(s.longitude, s.latitude)::geography, " +
           "ST_MakePoint(:longitude, :latitude)::geography, :radius)", 
           nativeQuery = true)
    List<Shop> findShopsWithinRadius(@Param("latitude") Double latitude, 
                                     @Param("longitude") Double longitude, 
                                     @Param("radius") Double radius);
}
