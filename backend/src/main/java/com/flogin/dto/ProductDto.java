package com.flogin.dto;

import java.math.BigDecimal;

// Dùng Lombok (mà bạn đã có) để code ngắn gọn
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductDto {
    private Long id;
    
    // Các trường này phải khớp với frontend (ProductForm.jsx)
    private String ten;
    private BigDecimal gia;
    private int soLuong;

    // Thêm các trường khác nếu file Product.java của bạn có
    private String moTa;
    // private String danhMuc;
    private Long categoryId;
    private Long createdById;
}