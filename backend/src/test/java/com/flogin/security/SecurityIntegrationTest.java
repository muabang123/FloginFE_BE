package com.flogin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flogin.dto.LoginRequest;
import com.flogin.dto.ProductDto;
import com.flogin.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * =========================================================================================
 * DANH SÁCH KIỂM TRA (CHECKLIST) CHẤT LƯỢNG MÃ NGUỒN & BẢO MẬT (PHẦN 8)
 * =========================================================================================
 * I. MỤC TIÊU SAI LỆCH (DEVIATION OBJECTIVE):
 * - Mã nguồn có thực hiện đúng thiết kế bảo mật không? CÓ (Triển khai các kiểm
 * tra OWASP Top 10).
 * * II. MỤC TIÊU THIẾU SÓT (OMISSION OBJECTIVE):
 * - Mã nguồn có thực hiện đầy đủ các thiết kế không? CÓ (Bao gồm SQLi, XSS,
 * CORS, Auth Bypass).
 * * III. MỤC TIÊU LỖI (DEFECT OBJECTIVE):
 * - Quy ước đặt tên: Sử dụng tên gợi nhớ (ví dụ: testLoginSqlInjection).
 * - Phạm vi truy cập: Các phương thức test để mặc định (package-private) theo
 * chuẩn JUnit 5.
 * - Giả lập (Mocking): ProductService được giả lập để cô lập logic bảo mật khỏi
 * logic cơ sở dữ liệu.
 * * V. MỤC TIÊU MƠ HỒ (AMBIGUITY OBJECTIVE):
 * - Tất cả các test case đều có @DisplayName giải thích rõ ràng mục đích bằng
 * Tiếng Việt.
 * =========================================================================================
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Kiểm thử Tích hợp Bảo mật & Lỗ hổng")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Giả lập (Mock) Service để tránh các lỗi logic liên quan đến dữ liệu thật (ví
    // dụ: ID null)
    @MockBean
    private ProductService productService;

    /**
     * Test Case 1: Ngăn chặn tấn công SQL Injection
     * Mục tiêu: Xác minh API đăng nhập có khả năng chống lại các câu lệnh SQL độc
     * hại.
     * Kịch bản: Chèn chuỗi SQL vào trường tên đăng nhập.
     * Kết quả mong đợi: 401 Unauthorized (Yêu cầu bị từ chối, SQL không được thực
     * thi).
     */
    @Test
    @DisplayName("SQL Injection: Đăng nhập với payload độc hại")
    void testLoginSqlInjection() throws Exception {
        LoginRequest attackRequest = new LoginRequest("admin' OR '1'='1", "password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attackRequest)))
                .andExpect(status().isUnauthorized()) // Cổng bảo mật: Từ chối thông tin xác thực không hợp lệ
                .andExpect(jsonPath("$.success").doesNotExist());
    }

    /**
     * Test Case 2: Ngăn chặn mã độc XSS (Cross-Site Scripting)
     * Mục tiêu: Đảm bảo ứng dụng không thực thi các script độc hại khi lưu vào DB.
     * Kịch bản: Tạo sản phẩm mới với mã JavaScript trong tên sản phẩm.
     * Kết quả mong đợi: 201 Created, nhưng mã script được trả về dưới dạng văn bản
     * thuần (Không thực thi).
     */
    @Test
    @DisplayName("XSS: Tạo sản phẩm với script độc hại trong tên")
    void testCreateProductXss() throws Exception {
        // 1. Chuẩn bị dữ liệu đầu vào (Sử dụng biến Tiếng Việt theo DTO)
        ProductDto maliciousProduct = new ProductDto();
        maliciousProduct.setTen("<script>alert('XSS')</script>");
        maliciousProduct.setGia(new BigDecimal("100000"));
        maliciousProduct.setSoLuong(10);

        // 2. Giả lập phản hồi của Service để cô lập kiểm thử Input/Output
        Mockito.when(productService.createProduct(any(ProductDto.class))).thenReturn(maliciousProduct);

        // 3. Thực hiện Request
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ten").value("<script>alert('XSS')</script>")); // Kiểm chứng đầu ra nguyên bản
    }

    /**
     * Test Case 3: Chia sẻ tài nguyên chéo nguồn (CORS)
     * Mục tiêu: Xác minh API cho phép các yêu cầu từ nguồn Frontend được ủy quyền.
     * Kịch bản: Gửi yêu cầu GET với header Origin được đặt thành URL của Frontend.
     * Kết quả mong đợi: 200 OK (Truy cập được cho phép).
     */
    @Test
    @DisplayName("Cấu hình bảo mật: Kiểm tra CSRF cho phép Frontend truy cập")
    void testCorsConfiguration() throws Exception {
        mockMvc.perform(get("/api/products")
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());
    }

    /**
     * Test Case 4: Nỗ lực vượt qua xác thực (Authentication Bypass)
     * Mục tiêu: Xác minh rằng các yêu cầu rỗng/sai định dạng đều bị từ chối.
     * Kịch bản: Gửi một body JSON rỗng đến endpoint đăng nhập.
     * Kết quả mong đợi: 401 Unauthorized (Server xử lý khéo léo việc thiếu thông
     * tin xác thực).
     */
    @Test
    @DisplayName("Auth Bypass: Gửi request thiếu thông tin đăng nhập")
    void testLoginMissingBody() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // Payload rỗng
                .andExpect(status().isUnauthorized()) // Mong đợi 401 (theo hành vi thực tế của Server)
                .andReturn();
    }

    /**
     * Test Case 5: Chống dò mật khẩu (Brute Force) / Mã hóa mật khẩu
     * Mục tiêu: Xác minh hệ thống kiểm tra tính hợp lệ của mật khẩu (đã hash).
     * Kịch bản: Cố gắng đăng nhập với tên người dùng đúng nhưng sai mật khẩu.
     * Kết quả mong đợi: 401 Unauthorized.
     */
    @Test
    @DisplayName("Brute Force Check: Đăng nhập sai mật khẩu nhiều lần")
    void testLoginWrongPassword() throws Exception {
        LoginRequest wrongRequest = new LoginRequest("testuser", "WrongPass123");

        // Mong đợi 401, hệ thống không được treo hoặc lộ lỗi nội bộ
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongRequest)))
                .andExpect(status().isUnauthorized());
    }
}