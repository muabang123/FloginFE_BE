package com.flogin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a Product Category
 * Manages product categorization
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_is_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    /**
     * Constructor for testing purposes
     */
    public Category(String name) {
        this.name = name;
        this.isActive = true;
    }

    /**
     * Constructor with description
     */
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
    }

    /**
     * Check if category is active
     */
    public boolean isActive() {
        return this.isActive != null && this.isActive;
    }

    /**
     * Get total number of products in this category
     */
    @Transient
    public int getProductCount() {
        return products != null ? products.size() : 0;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", productCount=" + getProductCount() +
                '}';
    }
}