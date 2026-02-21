package com.example.demo.repository;

import com.example.demo.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    List<MenuItem> findByMenuId(Long menuId);
    
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.isAvailable = true")
    List<MenuItem> findAvailableItemsByMenuId(@Param("menuId") Long menuId);
    
    List<MenuItem> findByIsHot(Boolean isHot);
    
    List<MenuItem> findByIsSignature(Boolean isSignature);
    
    List<MenuItem> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT mi FROM MenuItem mi ORDER BY mi.viewCount DESC")
    List<MenuItem> findTopViewedItems();
}
