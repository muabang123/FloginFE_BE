package com.flogin.repository;

import com.flogin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all active users
     */
    List<User> findByIsActiveTrue();
    
    /**
     * Find users by role
     */
    List<User> findByRole(User.Role role);
    
    /**
     * Find users by username containing (for search)
     */
    List<User> findByUsernameContainingIgnoreCase(String username);
    
    /**
     * Custom query: Find users with products
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.products WHERE u.isActive = true")
    List<User> findActiveUsersWithProducts();
}