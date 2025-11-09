package com.flogin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity class representing a Product
 * Manages product information and inventory
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_category", columnList = "category_id"),
    @Index(name = "idx_price", columnList = "price"),
    @Index(name = "idx_created_by", columnList = "created_by"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999999.99", message = "Price must not exceed 999,999,999.99")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 99999, message = "Quantity must not exceed 99,999")
    @Column(nullable = false)
    private Integer quantity = 0;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    // Relationships
    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_product_category"))
    private Category category;

    @NotNull(message = "Creator is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_product_user"))
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Constructor for testing purposes
     */
    public Product(String name, BigDecimal price, Integer quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    /**
     * Constructor with category
     */
    public Product(String name, BigDecimal price, Integer quantity, Category category, User createdBy) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.createdBy = createdBy;
    }

    /**
     * Full constructor
     */
    public Product(String name, BigDecimal price, Integer quantity, String description, 
                   Category category, User createdBy) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.category = category;
        this.createdBy = createdBy;
    }

    /**
     * Check if product is in stock
     */
    @Transient
    public boolean isInStock() {
        return this.quantity != null && this.quantity > 0;
    }

    /**
     * Check if product is low stock (less than 10)
     */
    @Transient
    public boolean isLowStock() {
        return this.quantity != null && this.quantity > 0 && this.quantity < 10;
    }

    /**
     * Check if product is out of stock
     */
    @Transient
    public boolean isOutOfStock() {
        return this.quantity == null || this.quantity <= 0;
    }

    /**
     * Get formatted price with currency
     */
    @Transient
    public String getFormattedPrice() {
        if (price == null) return "0 VND";
        return String.format("%,.0f VND", price);
    }

    /**
     * Update stock quantity
     */
    public void updateStock(Integer newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = newQuantity;
    }

    /**
     * Decrease stock (for sale)
     */
    public void decreaseStock(Integer amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + this.quantity);
        }
        this.quantity -= amount;
    }

    /**
     * Increase stock (for restock)
     */
    public void increaseStock(Integer amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (this.quantity + amount > 99999) {
            throw new IllegalArgumentException("Stock cannot exceed 99,999");
        }
        this.quantity += amount;
    }

    /**
     * Validate product data
     */
    @PrePersist
    @PreUpdate
    private void validateProduct() {
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (quantity != null && (quantity < 0 || quantity > 99999)) {
            throw new IllegalArgumentException("Quantity must be between 0 and 99,999");
        }
        if (name != null && (name.length() < 3 || name.length() > 100)) {
            throw new IllegalArgumentException("Product name must be between 3 and 100 characters");
        }
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", description='" + description + '\'' +
                ", categoryId=" + (category != null ? category.getId() : null) +
                ", createdById=" + (createdBy != null ? createdBy.getId() : null) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    public Long getId() { return id; }
    public String getTen() { return name; }
    public BigDecimal getGia() { return price; }
    public Integer getSoLuong() { return quantity; }

    public void setTen(String ten) { this.name = ten; }
    public void setGia(BigDecimal gia) { this.price = gia; }
    public void setSoLuong(Integer soLuong) { this.quantity= soLuong; }
}