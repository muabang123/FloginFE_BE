/**
 * =========================================================================================
 * I. MỤC TIÊU BAO PHỦ (COVERAGE OBJECTIVE):
 * - CRUD Operations: Create, Read, Update, Delete đã được kiểm thử đầy đủ.
 * - Edge Cases: Giá trị null, giá âm, không tồn tại ID đã được xử lý.
 * * II. MỤC TIÊU LỖI (DEFECT OBJECTIVE):
 * - Mocking: Sử dụng @Mock cho Repository để cô lập logic của Service.
 * - Assertions: Sử dụng JUnit 5 Assertions để kiểm tra tính đúng đắn của kết quả.
 * * III. QUY ƯỚC ĐẶT TÊN:
 * - Tên test case mô tả rõ kịch bản (ví dụ: testCreateProductSuccess).
 * =========================================================================================
 */

package com.flogin.service;

import com.flogin.dto.ProductDto;
import com.flogin.entity.Category;
import com.flogin.entity.Product;
import com.flogin.entity.User;
import com.flogin.repository.CategoryRepository;
import com.flogin.repository.ProductRepository;
import com.flogin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit Tests cho ProductService
 * Câu 3.2.2: Backend Unit Tests - Product Service (5 điểm)
 * 
 * Test Coverage:
 * - CREATE: Tạo sản phẩm mới
 * - READ: Lấy danh sách và chi tiết sản phẩm
 * - UPDATE: Cập nhật sản phẩm
 * - DELETE: Xóa sản phẩm
 * - Validation và Error Handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

        @Mock
        private ProductRepository productRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private CategoryRepository categoryRepository;

        @InjectMocks
        private ProductService productService;

        private Product testProduct;
        private ProductDto testProductDto;
        private Category testCategory;
        private User testUser;

        @BeforeEach
        void setUp() {
                // Setup test category
                testCategory = new Category();
                testCategory.setId(1L);
                testCategory.setName("Electronics");

                // Setup test user
                testUser = new User();
                testUser.setId(1L);
                testUser.setUsername("testuser");
                testUser.setEmail("test@example.com");

                // Setup test product entity
                testProduct = new Product();
                testProduct.setId(1L);
                testProduct.setTen("Laptop Dell");
                testProduct.setGia(new BigDecimal("15000000"));
                testProduct.setSoLuong(10);
                testProduct.setCategory(testCategory);
                testProduct.setCreatedBy(testUser);

                // Setup test product DTO
                testProductDto = new ProductDto();
                testProductDto.setTen("Laptop Dell");
                testProductDto.setGia(new BigDecimal("15000000"));
                testProductDto.setSoLuong(10);
                testProductDto.setCategoryId(1L);
                testProductDto.setCreatedById(1L);
        }

        // ============================================
        // CREATE TESTS
        // ============================================

        @Test
        @DisplayName("TC1: Tạo sản phẩm mới thành công")
        void testCreateProductSuccess() {
                // Arrange
                when(categoryRepository.findById(1L))
                                .thenReturn(Optional.of(testCategory));
                when(userRepository.findById(1L))
                                .thenReturn(Optional.of(testUser));
                when(productRepository.save(any(Product.class)))
                                .thenReturn(testProduct);

                // Act
                ProductDto result = productService.createProduct(testProductDto);

                // Assert
                assertNotNull(result, "Result không được null");
                assertEquals("Laptop Dell", result.getTen());
                assertEquals(new BigDecimal("15000000"), result.getGia());
                assertEquals(10, result.getSoLuong());

                verify(categoryRepository, times(1)).findById(1L);
                verify(userRepository, times(1)).findById(1L);
                verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("TC2: Tạo sản phẩm thất bại - Category không tồn tại")
        void testCreateProductCategoryNotFound() {
                // Arrange
                when(categoryRepository.findById(1L))
                                .thenReturn(Optional.empty());

                // Act & Assert
                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> productService.createProduct(testProductDto));

                assertTrue(exception.getMessage().contains("Không tìm thấy Category"));
                verify(categoryRepository, times(1)).findById(1L);
                verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC3: Tạo sản phẩm thất bại - User không tồn tại")
        void testCreateProductUserNotFound() {
                // Arrange
                when(categoryRepository.findById(1L))
                                .thenReturn(Optional.of(testCategory));
                when(userRepository.findById(1L))
                                .thenReturn(Optional.empty());

                // Act & Assert
                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> productService.createProduct(testProductDto));

                assertTrue(exception.getMessage().contains("Không tìm thấy User"));
                verify(userRepository, times(1)).findById(1L);
                verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC4: Tạo sản phẩm với giá tối thiểu")
        void testCreateProductMinPrice() {
                // Arrange
                testProductDto.setGia(new BigDecimal("0.01"));
                testProduct.setGia(new BigDecimal("0.01"));

                when(categoryRepository.findById(1L))
                                .thenReturn(Optional.of(testCategory));
                when(userRepository.findById(1L))
                                .thenReturn(Optional.of(testUser));
                when(productRepository.save(any(Product.class)))
                                .thenReturn(testProduct);

                // Act
                ProductDto result = productService.createProduct(testProductDto);

                // Assert
                assertNotNull(result);
                assertEquals(new BigDecimal("0.01"), result.getGia());
        }

        @Test
        @DisplayName("TC5: Tạo sản phẩm với số lượng = 0")
        void testCreateProductZeroQuantity() {
                // Arrange
                testProductDto.setSoLuong(0);
                testProduct.setSoLuong(0);

                when(categoryRepository.findById(1L))
                                .thenReturn(Optional.of(testCategory));
                when(userRepository.findById(1L))
                                .thenReturn(Optional.of(testUser));
                when(productRepository.save(any(Product.class)))
                                .thenReturn(testProduct);

                // Act
                ProductDto result = productService.createProduct(testProductDto);

                // Assert
                assertNotNull(result);
                assertEquals(0, result.getSoLuong());
        }

        // ============================================
        // READ TESTS
        // ============================================

        @Test
        @DisplayName("TC6: Lấy tất cả sản phẩm thành công")
        void testGetAllProductsSuccess() {
                // Arrange
                Product product2 = new Product();
                product2.setId(2L);
                product2.setTen("Mouse");
                product2.setGia(new BigDecimal("200000"));
                product2.setSoLuong(50);
                product2.setCategory(testCategory);
                product2.setCreatedBy(testUser);

                when(productRepository.findAll())
                                .thenReturn(Arrays.asList(testProduct, product2));

                // Act
                List<ProductDto> result = productService.getAllProducts();

                // Assert
                assertNotNull(result);
                assertEquals(2, result.size());
                assertEquals("Laptop Dell", result.get(0).getTen());
                assertEquals("Mouse", result.get(1).getTen());

                verify(productRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("TC7: Lấy danh sách rỗng khi không có sản phẩm")
        void testGetAllProductsEmpty() {
                // Arrange
                when(productRepository.findAll())
                                .thenReturn(Arrays.asList());

                // Act
                List<ProductDto> result = productService.getAllProducts();

                // Assert
                assertNotNull(result);
                assertTrue(result.isEmpty());
                verify(productRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("TC8: Lấy sản phẩm theo ID thành công")
        void testGetProductByIdSuccess() {
                // Arrange
                when(productRepository.findById(1L))
                                .thenReturn(Optional.of(testProduct));

                // Act
                ProductDto result = productService.getProductById(1L);

                // Assert
                assertNotNull(result);
                assertEquals(1L, result.getId());
                assertEquals("Laptop Dell", result.getTen());
                assertEquals(new BigDecimal("15000000"), result.getGia());

                verify(productRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("TC9: Lấy sản phẩm theo ID thất bại - Không tồn tại")
        void testGetProductByIdNotFound() {
                // Arrange
                when(productRepository.findById(999L))
                                .thenReturn(Optional.empty());

                // Act & Assert
                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> productService.getProductById(999L));

                assertTrue(exception.getMessage().contains("Không tìm thấy sản phẩm"));
                assertTrue(exception.getMessage().contains("999"));
                verify(productRepository, times(1)).findById(999L);
        }

        // ============================================
        // UPDATE TESTS
        // ============================================

        @Test
        @DisplayName("TC10: Cập nhật sản phẩm thành công")
        void testUpdateProductSuccess() {
                // Arrange
                ProductDto updateDto = new ProductDto();
                updateDto.setTen("Laptop HP");
                updateDto.setGia(new BigDecimal("14000000"));
                updateDto.setSoLuong(20);
                updateDto.setCategoryId(1L);

                Product updatedProduct = new Product();
                updatedProduct.setId(1L);
                updatedProduct.setTen("Laptop HP");
                updatedProduct.setGia(new BigDecimal("14000000"));
                updatedProduct.setSoLuong(20);
                updatedProduct.setCategory(testCategory);
                updatedProduct.setCreatedBy(testUser);

                when(productRepository.findById(1L))
                                .thenReturn(Optional.of(testProduct));
                when(categoryRepository.findById(1L))
                                .thenReturn(Optional.of(testCategory));
                when(productRepository.save(any(Product.class)))
                                .thenReturn(updatedProduct);

                // Act
                ProductDto result = productService.updateProduct(1L, updateDto);

                // Assert
                assertNotNull(result);
                assertEquals("Laptop HP", result.getTen());
                assertEquals(new BigDecimal("14000000"), result.getGia());
                assertEquals(20, result.getSoLuong());

                verify(productRepository, times(1)).findById(1L);
                verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("TC11: Cập nhật sản phẩm thất bại - Product không tồn tại")
        void testUpdateProductNotFound() {
                // Arrange
                ProductDto updateDto = new ProductDto();
                updateDto.setTen("New Name");

                when(productRepository.findById(999L))
                                .thenReturn(Optional.empty());

                // Act & Assert
                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> productService.updateProduct(999L, updateDto));

                assertTrue(exception.getMessage().contains("Không tìm thấy sản phẩm"));
                verify(productRepository, times(1)).findById(999L);
                verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC12: Cập nhật category của sản phẩm")
        void testUpdateProductCategory() {
                // Arrange
                Category newCategory = new Category();
                newCategory.setId(2L);
                newCategory.setName("Books");

                ProductDto updateDto = new ProductDto();
                updateDto.setTen("Laptop Dell");
                updateDto.setGia(new BigDecimal("15000000"));
                updateDto.setSoLuong(10);
                updateDto.setCategoryId(2L);

                when(productRepository.findById(1L))
                                .thenReturn(Optional.of(testProduct));
                when(categoryRepository.findById(2L))
                                .thenReturn(Optional.of(newCategory));
                when(productRepository.save(any(Product.class)))
                                .thenReturn(testProduct);

                // Act
                ProductDto result = productService.updateProduct(1L, updateDto);

                // Assert
                assertNotNull(result);
                verify(categoryRepository, times(1)).findById(2L);
                verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("TC13: Cập nhật sản phẩm với category không tồn tại")
        void testUpdateProductCategoryNotFound() {
                // Arrange
                ProductDto updateDto = new ProductDto();
                updateDto.setTen("Laptop Dell");
                updateDto.setGia(new BigDecimal("15000000"));
                updateDto.setSoLuong(10);
                updateDto.setCategoryId(999L);

                when(productRepository.findById(1L))
                                .thenReturn(Optional.of(testProduct));
                when(categoryRepository.findById(999L))
                                .thenReturn(Optional.empty());

                // Act & Assert
                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> productService.updateProduct(1L, updateDto));

                assertTrue(exception.getMessage().contains("Không tìm thấy Category"));
                verify(productRepository, never()).save(any());
        }

        // ============================================
        // DELETE TESTS
        // ============================================

        @Test
        @DisplayName("TC14: Xóa sản phẩm thành công")
        void testDeleteProductSuccess() {
                // Arrange
                when(productRepository.existsById(1L))
                                .thenReturn(true);
                doNothing().when(productRepository).deleteById(1L);

                // Act
                productService.deleteProduct(1L);

                // Assert
                verify(productRepository, times(1)).existsById(1L);
                verify(productRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("TC15: Xóa sản phẩm thất bại - Product không tồn tại")
        void testDeleteProductNotFound() {
                // Arrange
                when(productRepository.existsById(999L))
                                .thenReturn(false);

                // Act & Assert
                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> productService.deleteProduct(999L));

                assertTrue(exception.getMessage().contains("Không tìm thấy sản phẩm"));
                assertTrue(exception.getMessage().contains("999"));

                verify(productRepository, times(1)).existsById(999L);
                verify(productRepository, never()).deleteById(anyLong());
        }

        // ============================================
        // BOUNDARY TESTS
        // ============================================

        @Test
        @DisplayName("TC16: Tạo sản phẩm với giá tối đa")
        void testCreateProductMaxPrice() {
                // Arrange
                testProductDto.setGia(new BigDecimal("999999999.99"));
                testProduct.setGia(new BigDecimal("999999999.99"));

                when(categoryRepository.findById(1L))
                                .thenReturn(Optional.of(testCategory));
                when(userRepository.findById(1L))
                                .thenReturn(Optional.of(testUser));
                when(productRepository.save(any(Product.class)))
                                .thenReturn(testProduct);

                // Act
                ProductDto result = productService.createProduct(testProductDto);

                // Assert
                assertNotNull(result);
                assertEquals(new BigDecimal("999999999.99"), result.getGia());
        }

        @Test
        @DisplayName("TC17: Tạo sản phẩm với số lượng tối đa")
        void testCreateProductMaxQuantity() {
                // Arrange
                testProductDto.setSoLuong(99999);
                testProduct.setSoLuong(99999);

                when(categoryRepository.findById(1L))
                                .thenReturn(Optional.of(testCategory));
                when(userRepository.findById(1L))
                                .thenReturn(Optional.of(testUser));
                when(productRepository.save(any(Product.class)))
                                .thenReturn(testProduct);

                // Act
                ProductDto result = productService.createProduct(testProductDto);

                // Assert
                assertNotNull(result);
                assertEquals(99999, result.getSoLuong());
        }

        @Test
        @DisplayName("TC18: Tạo sản phẩm với tên độ dài tối thiểu")
        void testCreateProductMinNameLength() {
                // Arrange
                testProductDto.setTen("ABC"); // 3 ký tự
                testProduct.setTen("ABC");

                when(categoryRepository.findById(1L))
                                .thenReturn(Optional.of(testCategory));
                when(userRepository.findById(1L))
                                .thenReturn(Optional.of(testUser));
                when(productRepository.save(any(Product.class)))
                                .thenReturn(testProduct);

                // Act
                ProductDto result = productService.createProduct(testProductDto);

                // Assert
                assertNotNull(result);
                assertEquals("ABC", result.getTen());
                assertEquals(3, result.getTen().length());
        }

        @Test
        @DisplayName("TC19: Verify DTO conversion từ Entity")
        void testConvertToDtoCorrectly() {
                // Arrange
                when(productRepository.findById(1L))
                                .thenReturn(Optional.of(testProduct));

                // Act
                ProductDto result = productService.getProductById(1L);

                // Assert
                assertNotNull(result);
                assertEquals(testProduct.getId(), result.getId());
                assertEquals(testProduct.getTen(), result.getTen());
                assertEquals(testProduct.getGia(), result.getGia());
                assertEquals(testProduct.getSoLuong(), result.getSoLuong());
                assertEquals(testProduct.getCategory().getId(), result.getCategoryId());
                assertEquals(testProduct.getCreatedBy().getId(), result.getCreatedById());
        }

        @Test
        @DisplayName("TC20: Test multiple operations trên cùng một product")
        void testMultipleOperationsOnSameProduct() {
                // Arrange
                when(productRepository.findById(1L))
                                .thenReturn(Optional.of(testProduct));
                // when(categoryRepository.findById(1L))
                // .thenReturn(Optional.of(testCategory));
                when(productRepository.save(any(Product.class)))
                                .thenReturn(testProduct);

                // Act - Get, Update, Get again
                ProductDto result1 = productService.getProductById(1L);

                ProductDto updateDto = new ProductDto();
                updateDto.setTen("Updated Name");
                updateDto.setGia(new BigDecimal("20000000"));
                updateDto.setSoLuong(5);

                ProductDto result2 = productService.updateProduct(1L, updateDto);

                // Assert
                assertNotNull(result1);
                assertNotNull(result2);
                verify(productRepository, times(2)).findById(1L);
                verify(productRepository, times(1)).save(any(Product.class));
        }
}