/**
 * =========================================================================================
 * DANH SÁCH KIỂM TRA (CHECKLIST) INTEGRATION TEST - PRODUCT CONTROLLER
 * =========================================================================================
 * I. MỤC TIÊU API RESTFUL:
 * - Verbs: Kiểm tra đúng phương thức HTTP (GET, POST, PUT, DELETE).
 * - Serialization: Kiểm tra việc chuyển đổi giữa Object Java và JSON (Jackson).
 * * II. MỤC TIÊU LUỒNG DỮ LIỆU:
 * - Mock Service: Đảm bảo Controller gọi đúng phương thức của Service với tham số chính xác.
 * - Exception Mapping: Kiểm tra việc map Exception từ Service sang HTTP Status 404/500.
 * =========================================================================================
 */

package com.flogin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flogin.dto.ProductDto;
import com.flogin.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests cho ProductController
 * Câu 4.2.2: Backend API Integration - Product (5 điểm)
 * 
 * Test Coverage:
 * - POST /api/products (Create)
 * - GET /api/products (Read all)
 * - GET /api/products/{id} (Read one)
 * - PUT /api/products/{id} (Update)
 * - DELETE /api/products/{id} (Delete)
 */
@WebMvcTest(ProductController.class)
@DisplayName("ProductController Integration Tests")
class ProductControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ProductService productService;

        private ProductDto testProductDto;
        private ProductDto testProductDto2;

        @BeforeEach
        void setUp() {
                // Setup test product 1
                testProductDto = new ProductDto();
                testProductDto.setId(1L);
                testProductDto.setTen("Laptop Dell");
                testProductDto.setGia(new BigDecimal("15000000"));
                testProductDto.setSoLuong(10);
                testProductDto.setCategoryId(1L);
                testProductDto.setCreatedById(1L);

                // Setup test product 2
                testProductDto2 = new ProductDto();
                testProductDto2.setId(2L);
                testProductDto2.setTen("Mouse Logitech");
                testProductDto2.setGia(new BigDecimal("200000"));
                testProductDto2.setSoLuong(50);
                testProductDto2.setCategoryId(1L);
                testProductDto2.setCreatedById(1L);
        }

        // ============================================
        // GET /api/products - READ ALL
        // ============================================

        @Test
        @DisplayName("TC1: GET /api/products - Lấy danh sách sản phẩm thành công")
        void testGetAllProductsSuccess() throws Exception {
                // Arrange
                List<ProductDto> products = Arrays.asList(testProductDto, testProductDto2);
                when(productService.getAllProducts()).thenReturn(products);

                // Act & Assert
                mockMvc.perform(get("/api/products")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[0].ten").value("Laptop Dell"))
                                .andExpect(jsonPath("$[0].gia").value(15000000))
                                .andExpect(jsonPath("$[0].soLuong").value(10))
                                .andExpect(jsonPath("$[1].ten").value("Mouse Logitech"))
                                .andExpect(jsonPath("$[1].gia").value(200000));

                verify(productService, times(1)).getAllProducts();
        }

        @Test
        @DisplayName("TC2: GET /api/products - Danh sách rỗng")
        void testGetAllProductsEmpty() throws Exception {
                // Arrange
                when(productService.getAllProducts()).thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get("/api/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(0)))
                                .andExpect(jsonPath("$", empty()));

                verify(productService, times(1)).getAllProducts();
        }

        // ============================================
        // GET /api/products/{id} - READ ONE
        // ============================================

        @Test
        @DisplayName("TC3: GET /api/products/{id} - Lấy sản phẩm theo ID thành công")
        void testGetProductByIdSuccess() throws Exception {
                // Arrange
                when(productService.getProductById(1L)).thenReturn(testProductDto);

                // Act & Assert
                mockMvc.perform(get("/api/products/1"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.ten").value("Laptop Dell"))
                                .andExpect(jsonPath("$.gia").value(15000000))
                                .andExpect(jsonPath("$.soLuong").value(10));

                verify(productService, times(1)).getProductById(1L);
        }

        @Test
        @DisplayName("TC4: GET /api/products/{id} - Sản phẩm không tồn tại (500)")
        void testGetProductByIdNotFound() throws Exception {
                // Arrange
                when(productService.getProductById(999L))
                                .thenThrow(new RuntimeException("Không tìm thấy sản phẩm với id: 999"));

                // Act & Assert
                mockMvc.perform(get("/api/products/999"))
                                .andExpect(status().isNotFound());

                verify(productService, times(1)).getProductById(999L);
        }

        @Test
        @DisplayName("TC5: GET /api/products/{id} - ID không hợp lệ (400)")
        void testGetProductByIdInvalidId() throws Exception {
                // Act & Assert
                mockMvc.perform(get("/api/products/invalid"))
                                .andExpect(status().isBadRequest());
        }

        // ============================================
        // POST /api/products - CREATE
        // ============================================

        @Test
        @DisplayName("TC6: POST /api/products - Tạo sản phẩm mới thành công (201)")
        void testCreateProductSuccess() throws Exception {
                // Arrange
                ProductDto newProduct = new ProductDto();
                newProduct.setTen("iPhone 15");
                newProduct.setGia(new BigDecimal("25000000"));
                newProduct.setSoLuong(5);
                newProduct.setCategoryId(2L);
                newProduct.setCreatedById(1L);

                ProductDto savedProduct = new ProductDto();
                savedProduct.setId(3L);
                savedProduct.setTen("iPhone 15");
                savedProduct.setGia(new BigDecimal("25000000"));
                savedProduct.setSoLuong(5);
                savedProduct.setCategoryId(2L);
                savedProduct.setCreatedById(1L);

                when(productService.createProduct(any(ProductDto.class)))
                                .thenReturn(savedProduct);

                // Act & Assert
                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newProduct)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(3))
                                .andExpect(jsonPath("$.ten").value("iPhone 15"))
                                .andExpect(jsonPath("$.gia").value(25000000))
                                .andExpect(jsonPath("$.soLuong").value(5));

                verify(productService, times(1)).createProduct(any(ProductDto.class));
        }

        @Test
        @DisplayName("TC7: POST /api/products - Tạo sản phẩm với giá tối thiểu")
        void testCreateProductMinPrice() throws Exception {
                // Arrange
                ProductDto minPriceProduct = new ProductDto();
                minPriceProduct.setTen("Bút bi");
                minPriceProduct.setGia(new BigDecimal("0.01"));
                minPriceProduct.setSoLuong(100);
                minPriceProduct.setCategoryId(1L);
                minPriceProduct.setCreatedById(1L);

                when(productService.createProduct(any(ProductDto.class)))
                                .thenReturn(minPriceProduct);

                // Act & Assert
                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(minPriceProduct)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.gia").value(0.01));
        }

        @Test
        @DisplayName("TC8: POST /api/products - Tạo sản phẩm với số lượng = 0")
        void testCreateProductZeroQuantity() throws Exception {
                // Arrange
                ProductDto zeroQtyProduct = new ProductDto();
                zeroQtyProduct.setTen("Hết hàng");
                zeroQtyProduct.setGia(new BigDecimal("100000"));
                zeroQtyProduct.setSoLuong(0);
                zeroQtyProduct.setCategoryId(1L);
                zeroQtyProduct.setCreatedById(1L);

                when(productService.createProduct(any(ProductDto.class)))
                                .thenReturn(zeroQtyProduct);

                // Act & Assert
                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(zeroQtyProduct)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.soLuong").value(0));
        }

        @Test
        @DisplayName("TC9: POST /api/products - Category không tồn tại (500)")
        void testCreateProductCategoryNotFound() throws Exception {
                // Arrange
                ProductDto invalidProduct = new ProductDto();
                invalidProduct.setTen("Product");
                invalidProduct.setGia(new BigDecimal("100000"));
                invalidProduct.setSoLuong(10);
                invalidProduct.setCategoryId(999L);
                invalidProduct.setCreatedById(1L);

                when(productService.createProduct(any(ProductDto.class)))
                                .thenThrow(new RuntimeException("Không tìm thấy Category ID: 999"));

                // Act & Assert
                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidProduct)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC10: POST /api/products - Invalid JSON format (400)")
        void testCreateProductInvalidJson() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json}"))
                                .andExpect(status().isBadRequest());
        }

        // ============================================
        // PUT /api/products/{id} - UPDATE
        // ============================================

        @Test
        @DisplayName("TC11: PUT /api/products/{id} - Cập nhật sản phẩm thành công")
        void testUpdateProductSuccess() throws Exception {
                // Arrange
                ProductDto updateDto = new ProductDto();
                updateDto.setTen("Laptop HP Updated");
                updateDto.setGia(new BigDecimal("14000000"));
                updateDto.setSoLuong(20);
                updateDto.setCategoryId(1L);

                ProductDto updatedProduct = new ProductDto();
                updatedProduct.setId(1L);
                updatedProduct.setTen("Laptop HP Updated");
                updatedProduct.setGia(new BigDecimal("14000000"));
                updatedProduct.setSoLuong(20);
                updatedProduct.setCategoryId(1L);

                when(productService.updateProduct(eq(1L), any(ProductDto.class)))
                                .thenReturn(updatedProduct);

                // Act & Assert
                mockMvc.perform(put("/api/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.ten").value("Laptop HP Updated"))
                                .andExpect(jsonPath("$.gia").value(14000000))
                                .andExpect(jsonPath("$.soLuong").value(20));

                verify(productService, times(1)).updateProduct(eq(1L), any(ProductDto.class));
        }

        @Test
        @DisplayName("TC12: PUT /api/products/{id} - Sản phẩm không tồn tại (500)")
        void testUpdateProductNotFound() throws Exception {
                // Arrange
                ProductDto updateDto = new ProductDto();
                updateDto.setTen("Updated Name");
                updateDto.setGia(new BigDecimal("100000"));
                updateDto.setSoLuong(10);

                when(productService.updateProduct(eq(999L), any(ProductDto.class)))
                                .thenThrow(new RuntimeException("Không tìm thấy sản phẩm với id: 999"));

                // Act & Assert
                mockMvc.perform(put("/api/products/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC13: PUT /api/products/{id} - Cập nhật chỉ giá")
        void testUpdateProductPriceOnly() throws Exception {
                // Arrange
                ProductDto updateDto = new ProductDto();
                updateDto.setTen("Laptop Dell");
                updateDto.setGia(new BigDecimal("12000000")); // Chỉ đổi giá
                updateDto.setSoLuong(10);
                updateDto.setCategoryId(1L);

                ProductDto updatedProduct = new ProductDto();
                updatedProduct.setId(1L);
                updatedProduct.setTen("Laptop Dell");
                updatedProduct.setGia(new BigDecimal("12000000"));
                updatedProduct.setSoLuong(10);

                when(productService.updateProduct(eq(1L), any(ProductDto.class)))
                                .thenReturn(updatedProduct);

                // Act & Assert
                mockMvc.perform(put("/api/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.gia").value(12000000));
        }

        // ============================================
        // DELETE /api/products/{id} - DELETE
        // ============================================

        @Test
        @DisplayName("TC14: DELETE /api/products/{id} - Xóa sản phẩm thành công (204)")
        void testDeleteProductSuccess() throws Exception {
                // Arrange
                doNothing().when(productService).deleteProduct(1L);

                // Act & Assert
                mockMvc.perform(delete("/api/products/1"))
                                .andDo(print())
                                .andExpect(status().isNoContent());

                verify(productService, times(1)).deleteProduct(1L);
        }

        @Test
        @DisplayName("TC15: DELETE /api/products/{id} - Sản phẩm không tồn tại (500)")
        void testDeleteProductNotFound() throws Exception {
                // Arrange
                doThrow(new RuntimeException("Không tìm thấy sản phẩm với id: 999"))
                                .when(productService).deleteProduct(999L);

                // Act & Assert
                mockMvc.perform(delete("/api/products/999"))
                                .andExpect(status().isNotFound());

                verify(productService, times(1)).deleteProduct(999L);
        }

        @Test
        @DisplayName("TC16: DELETE /api/products/{id} - ID không hợp lệ (400)")
        void testDeleteProductInvalidId() throws Exception {
                // Act & Assert
                mockMvc.perform(delete("/api/products/invalid"))
                                .andExpect(status().isBadRequest());
        }

        // ============================================
        // BOUNDARY VALUE TESTS
        // ============================================

        @Test
        @DisplayName("TC17: POST /api/products - Tên sản phẩm độ dài tối thiểu (3 chars)")
        void testCreateProductMinNameLength() throws Exception {
                // Arrange
                ProductDto minNameProduct = new ProductDto();
                minNameProduct.setTen("ABC");
                minNameProduct.setGia(new BigDecimal("100000"));
                minNameProduct.setSoLuong(10);
                minNameProduct.setCategoryId(1L);
                minNameProduct.setCreatedById(1L);

                when(productService.createProduct(any(ProductDto.class)))
                                .thenReturn(minNameProduct);

                // Act & Assert
                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(minNameProduct)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.ten").value("ABC"));
        }

        @Test
        @DisplayName("TC18: POST /api/products - Giá tối đa (999,999,999.99)")
        void testCreateProductMaxPrice() throws Exception {
                // Arrange
                ProductDto maxPriceProduct = new ProductDto();
                maxPriceProduct.setTen("Siêu xe");
                maxPriceProduct.setGia(new BigDecimal("999999999.99"));
                maxPriceProduct.setSoLuong(1);
                maxPriceProduct.setCategoryId(1L);
                maxPriceProduct.setCreatedById(1L);

                when(productService.createProduct(any(ProductDto.class)))
                                .thenReturn(maxPriceProduct);

                // Act & Assert
                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(maxPriceProduct)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.gia").value(999999999.99));
        }

        @Test
        @DisplayName("TC19: POST /api/products - Số lượng tối đa (99,999)")
        void testCreateProductMaxQuantity() throws Exception {
                // Arrange
                ProductDto maxQtyProduct = new ProductDto();
                maxQtyProduct.setTen("Mass product");
                maxQtyProduct.setGia(new BigDecimal("1000"));
                maxQtyProduct.setSoLuong(99999);
                maxQtyProduct.setCategoryId(1L);
                maxQtyProduct.setCreatedById(1L);

                when(productService.createProduct(any(ProductDto.class)))
                                .thenReturn(maxQtyProduct);

                // Act & Assert
                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(maxQtyProduct)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.soLuong").value(99999));
        }

        // ============================================
        // RESPONSE STRUCTURE VALIDATION
        // ============================================

        @Test
        @DisplayName("TC20: GET /api/products - Verify complete response structure")
        void testGetAllProductsResponseStructure() throws Exception {
                // Arrange
                List<ProductDto> products = Arrays.asList(testProductDto);
                when(productService.getAllProducts()).thenReturn(products);

                // Act
                MvcResult result = mockMvc.perform(get("/api/products"))
                                .andExpect(status().isOk())
                                .andReturn();

                // Assert
                String jsonResponse = result.getResponse().getContentAsString();
                List<ProductDto> responseProducts = objectMapper.readValue(
                                jsonResponse,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, ProductDto.class));

                assertNotNull(responseProducts);
                assertFalse(responseProducts.isEmpty());

                ProductDto firstProduct = responseProducts.get(0);
                assertNotNull(firstProduct.getId());
                assertNotNull(firstProduct.getTen());
                assertNotNull(firstProduct.getGia());
                assertNotNull(firstProduct.getSoLuong());
        }
}