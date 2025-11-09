package com.flogin.repository;

import com.flogin.entity.LoginHistory;
import com.flogin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    
    /**
     * Find login history by user
     */
    List<LoginHistory> findByUser(User user);
    
    /**
     * Find login history by user with pagination
     */
    Page<LoginHistory> findByUser(User user, Pageable pageable);
    
    /**
     * Find login history by username
     */
    List<LoginHistory> findByUsername(String username);
    
    /**
     * Find successful logins
     */
    List<LoginHistory> findByIsSuccessTrue();
    
    /**
     * Find failed logins
     */
    List<LoginHistory> findByIsSuccessFalse();
    
    /**
     * Find failed logins by username
     */
    List<LoginHistory> findByUsernameAndIsSuccessFalse(String username);
    
    /**
     * Find login history by IP address
     */
    List<LoginHistory> findByIpAddress(String ipAddress);
    
    /**
     * Find recent login attempts for a user
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.username = :username " +
           "AND lh.loginTime >= :since ORDER BY lh.loginTime DESC")
    List<LoginHistory> findRecentLoginAttempts(
        @Param("username") String username,
        @Param("since") LocalDateTime since
    );
    
    /**
     * Count failed login attempts in time period
     */
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.username = :username " +
           "AND lh.isSuccess = false AND lh.loginTime >= :since")
    Long countFailedAttempts(
        @Param("username") String username,
        @Param("since") LocalDateTime since
    );
    
    /**
     * Find login history by date range
     */
    List<LoginHistory> findByLoginTimeBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Get login statistics by username
     */
    @Query("SELECT lh.username, COUNT(lh), " +
           "SUM(CASE WHEN lh.isSuccess = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN lh.isSuccess = false THEN 1 ELSE 0 END) " +
           "FROM LoginHistory lh GROUP BY lh.username")
    List<Object[]> getLoginStatistics();
    
    /**
     * Find last successful login
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user.id = :userId " +
           "AND lh.isSuccess = true ORDER BY lh.loginTime DESC")
    List<LoginHistory> findLastSuccessfulLogin(@Param("userId") Long userId, Pageable pageable);
}