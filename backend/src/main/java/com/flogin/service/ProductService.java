package com.flogin.service;

import com.flogin.dto.ProductDto;
import com.flogin.entity.Category;
import com.flogin.entity.Product;
import com.flogin.entity.User;
import com.flogin.repository.CategoryRepository;
import com.flogin.repository.ProductRepository;
import com.flogin.repository.UserRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository; // <-- 3. AUTOWIRE

    @Autowired
    private CategoryRepository categoryRepository; // <-- 4. AUTOWIRE

    // LẤY TẤT CẢ SẢN PHẨM (READ ALL)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // LẤY 1 SẢN PHẨM (READ ONE)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));
        return convertToDto(product);
    }

    // TẠO SẢN PHẨM (CREATE)
    public ProductDto createProduct(ProductDto productDto) {
        Product product = convertToEntity(productDto); // 5. SỬ DỤNG HÀM MỚI
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    // CẬP NHẬT SẢN PHẨM (UPDATE)
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));

        // Cập nhật các trường
        existingProduct.setTen(productDto.getTen());
        existingProduct.setGia(productDto.getGia());
        existingProduct.setSoLuong(productDto.getSoLuong());

        // 6. CẬP NHẬT CATEGORY VÀ USER (NẾU CẦN)
        if(productDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Category ID: " + productDto.getCategoryId()));
            existingProduct.setCategory(category);
        }
        
        // (Không cho phép cập nhật người tạo sản phẩm)

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }

    // XÓA SẢN PHẨM (DELETE)
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy sản phẩm với id: " + id);
        }
        productRepository.deleteById(id);
    }

    // --- CÁC HÀM TIỆN ÍCH CHUYỂN ĐỔI ---
    
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTen(product.getTen());     // Dùng getTen() để lấy 'name'
        dto.setGia(product.getGia());     // Dùng getGia() để lấy 'price'
        dto.setSoLuong(product.getSoLuong()); // Dùng getSoLuong() để lấy 'quantity'
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }
        if (product.getCreatedBy() != null) {
            dto.setCreatedById(product.getCreatedBy().getId());
        }
        return dto;
    }

    private Product convertToEntity(ProductDto dto) {
        Product product = new Product();
        
        // 7. SỬA HÀM NÀY ĐỂ FIX LỖI @NotNull
        product.setTen(dto.getTen());       // Dùng setTen() để set 'name'
        product.setGia(dto.getGia());       // Dùng setGia() để set 'price'
        product.setSoLuong(dto.getSoLuong()); // Dùng setSoLuong() để set 'quantity'

        // Lấy Category từ ID
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Category ID: " + dto.getCategoryId()));
        product.setCategory(category);

        // Lấy User (người tạo) từ ID
        User createdBy = userRepository.findById(dto.getCreatedById())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User ID: " + dto.getCreatedById()));
        product.setCreatedBy(createdBy);
        
        // Bỏ qua ID (vì đây là tạo mới) và timestamps (tự động tạo)
        return product;
    }
}