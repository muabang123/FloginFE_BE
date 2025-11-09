package com.flogin.repository;

import com.flogin.entity.Category;
import com.flogin.entity.Product;
import com.flogin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find product by name
     */
    Optional<Product> findByName(String name);
    
    /**
     * Find products by category
     */
    List<Product> findByCategory(Category category);
    
    /**
     * Find products by category with pagination
     */
    Page<Product> findByCategory(Category category, Pageable pageable);
    
    /**
     * Find products by created by user
     */
    List<Product> findByCreatedBy(User user);
    
    /**
     * Find products by name containing (search)
     */
    List<Product> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find products by name containing with pagination
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Find products by price range
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Find products by price range with pagination
     */
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    /**
     * Find products in stock (quantity > 0)
     */
    @Query("SELECT p FROM Product p WHERE p.quantity > 0")
    List<Product> findInStockProducts();
    
    /**
     * Find products out of stock
     */
    @Query("SELECT p FROM Product p WHERE p.quantity = 0")
    List<Product> findOutOfStockProducts();
    
    /**
     * Find low stock products (quantity < 10)
     */
    @Query("SELECT p FROM Product p WHERE p.quantity > 0 AND p.quantity < 10")
    List<Product> findLowStockProducts();
    
    /**
     * Custom query: Search products with filters
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProducts(
        @Param("name") String name,
        @Param("categoryId") Long categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );
    
    /**
     * Find top products by price
     */
    List<Product> findTop10ByOrderByPriceDesc();
    
    /**
     * Find recently added products
     */
    List<Product> findTop10ByOrderByCreatedAtDesc();
    
    /**
     * Count products by category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countByCategory(@Param("categoryId") Long categoryId);
    
    /**
     * Get total products count
     */
    @Query("SELECT COUNT(p) FROM Product p")
    Long getTotalProductCount();
    
    /**
     * Get total inventory value
     */
    @Query("SELECT SUM(p.price * p.quantity) FROM Product p")
    BigDecimal getTotalInventoryValue();
}