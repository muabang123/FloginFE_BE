package com.flogin.dto;

import java.math.BigDecimal;

// Import thêm AllArgsConstructor
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    
    // Các trường này khớp với frontend (ProductForm.jsx)
    private String ten;
    private BigDecimal gia;
    private int soLuong;

    // Các trường bổ sung
    private String moTa;
    // private String danhMuc;
    private Long categoryId;
    private Long createdById;
}