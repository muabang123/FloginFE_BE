package com.flogin.repository;

import com.flogin.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);
    
    /**
     * Check if category name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find all active categories
     */
    List<Category> findByIsActiveTrue();
    
    /**
     * Find categories by name containing (for search)
     */
    List<Category> findByNameContainingIgnoreCase(String name);
    
    /**
     * Custom query: Find categories with product count
     */
    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN c.products p " +
           "WHERE c.isActive = true GROUP BY c ORDER BY COUNT(p) DESC")
    List<Object[]> findCategoriesWithProductCount();
    
    /**
     * Find categories with products
     */
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.products " +
           "WHERE c.isActive = true")
    List<Category> findActiveCategoriesWithProducts();
}