package com.flogin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity class representing Login History
 * Tracks all login attempts for security and audit purposes
 */
@Entity
@Table(name = "login_history", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_login_time", columnList = "login_time"),
    @Index(name = "idx_is_success", columnList = "is_success")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nullable - vì có thể login với username không tồn tại
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_login_history_user"))
    private User user;

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String username;

    @Size(max = 45, message = "IP address must not exceed 45 characters")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 255, message = "User agent must not exceed 255 characters")
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @NotNull(message = "Success status is required")
    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    @Size(max = 255, message = "Failure reason must not exceed 255 characters")
    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "login_time", nullable = false, updatable = false)
    private LocalDateTime loginTime;

    /**
     * Constructor for successful login
     */
    public LoginHistory(User user, String ipAddress, String userAgent) {
        this.user = user;
        this.username = user.getUsername();
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isSuccess = true;
        this.failureReason = null;
    }

    /**
     * Constructor for failed login
     */
    public LoginHistory(String username, String ipAddress, String userAgent, String failureReason) {
        this.user = null;
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isSuccess = false;
        this.failureReason = failureReason;
    }

    /**
     * Constructor with all fields
     */
    public LoginHistory(User user, String username, String ipAddress, String userAgent, 
                       Boolean isSuccess, String failureReason) {
        this.user = user;
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isSuccess = isSuccess;
        this.failureReason = failureReason;
    }

    /**
     * Check if login was successful
     */
    public boolean isSuccessful() {
        return this.isSuccess != null && this.isSuccess;
    }

    /**
     * Check if login failed
     */
    public boolean isFailed() {
        return this.isSuccess == null || !this.isSuccess;
    }

    /**
     * Get formatted login time
     */
    @Transient
    public String getFormattedLoginTime() {
        if (loginTime == null) return "N/A";
        return loginTime.toString();
    }

    /**
     * Get login status text
     */
    @Transient
    public String getStatusText() {
        return isSuccessful() ? "Success" : "Failed";
    }

    /**
     * Common failure reasons
     */
    public static class FailureReason {
        public static final String USERNAME_NOT_FOUND = "Username not found";
        public static final String INVALID_PASSWORD = "Invalid password";
        public static final String ACCOUNT_DISABLED = "Account is disabled";
        public static final String ACCOUNT_LOCKED = "Account is locked";
        public static final String TOO_MANY_ATTEMPTS = "Too many login attempts";
        public static final String INVALID_CREDENTIALS = "Invalid credentials";
        public static final String VALIDATION_ERROR = "Validation error";
    }

    /**
     * Create success login history
     */
    public static LoginHistory createSuccessHistory(User user, String ipAddress, String userAgent) {
        return new LoginHistory(user, ipAddress, userAgent);
    }

    /**
     * Create failed login history
     */
    public static LoginHistory createFailureHistory(String username, String ipAddress, 
                                                    String userAgent, String failureReason) {
        return new LoginHistory(username, ipAddress, userAgent, failureReason);
    }

    @Override
    public String toString() {
        return "LoginHistory{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", username='" + username + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", isSuccess=" + isSuccess +
                ", failureReason='" + failureReason + '\'' +
                ", loginTime=" + loginTime +
                '}';
    }
}