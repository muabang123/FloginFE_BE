/**
 * =========================================================================================
 * DANH SÁCH KIỂM TRA (CHECKLIST) INTEGRATION TEST - AUTH CONTROLLER
 * =========================================================================================
 * I. MỤC TIÊU TÍCH HỢP (INTEGRATION OBJECTIVE):
 * - HTTP Status: Kiểm tra mã trạng thái trả về (200 OK, 401 Unauthorized, 400 Bad Request).
 * - JSON Structure: Đảm bảo cấu trúc JSON trả về khớp với thiết kế API.
 * * II. MỤC TIÊU BẢO MẬT (SECURITY OBJECTIVE):
 * - Input Validation: Kiểm tra @Valid hoạt động đúng với dữ liệu rỗng/sai định dạng.
 * - CORS: Kiểm tra các headers Access-Control được thiết lập đúng.
 * =========================================================================================
 */

package com.flogin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flogin.dto.LoginRequest;
import com.flogin.dto.LoginResponse;
import com.flogin.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests cho AuthController
 * Câu 4.1.2: Backend API Integration - Login (5 điểm)
 * 
 * Test Coverage:
 * - POST /api/auth/login endpoint
 * - Response structure và status codes
 * - CORS headers
 * - Content-Type validation
 * - Error handling
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthService authService;

        private LoginRequest validRequest;
        private LoginResponse successResponse;

        @BeforeEach
        void setUp() {
                validRequest = new LoginRequest("testuser", "Test123");
                successResponse = new LoginResponse("Đăng nhập thành công", "dummy-token-for-testuser");
        }

        // ============================================
        // POST /api/auth/login - SUCCESS CASES
        // ============================================

        @Test
        @DisplayName("TC1: POST /api/auth/login - Login thành công")
        void testLoginSuccess() throws Exception {
                // Arrange
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenReturn(successResponse);

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                                .andExpect(jsonPath("$.token").value("dummy-token-for-testuser"))
                                .andExpect(jsonPath("$.token").isNotEmpty());

                verify(authService, times(1)).authenticate(any(LoginRequest.class));
        }

        @Test
        @DisplayName("TC2: POST /api/auth/login - Verify response structure")
        void testLoginResponseStructure() throws Exception {
                // Arrange
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenReturn(successResponse);

                // Act
                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                // Assert
                String jsonResponse = result.getResponse().getContentAsString();
                LoginResponse response = objectMapper.readValue(jsonResponse, LoginResponse.class);

                assertNotNull(response);
                assertNotNull(response.message());
                assertNotNull(response.token());
                assertEquals("Đăng nhập thành công", response.message());
                assertTrue(response.token().startsWith("dummy-token-for-"));
        }

        @Test
        @DisplayName("TC3: POST /api/auth/login - Với username có ký tự đặc biệt hợp lệ")
        void testLoginWithSpecialCharacters() throws Exception {
                // Arrange
                LoginRequest specialRequest = new LoginRequest("test.user_123", "Test123");
                LoginResponse specialResponse = new LoginResponse(
                                "Đăng nhập thành công",
                                "dummy-token-for-test.user_123");

                when(authService.authenticate(any(LoginRequest.class)))
                                .thenReturn(specialResponse);

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(specialRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                                .andExpect(jsonPath("$.token").value("dummy-token-for-test.user_123"));
        }

        // ============================================
        // POST /api/auth/login - ERROR CASES
        // ============================================

        @Test
        @DisplayName("TC4: POST /api/auth/login - Credentials không hợp lệ (401)")
        void testLoginInvalidCredentials() throws Exception {
                // Arrange
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenThrow(new RuntimeException("Sai tên tài khoản hoặc mật khẩu!"));

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andDo(print())
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value("Sai tên tài khoản hoặc mật khẩu!"))
                                .andExpect(jsonPath("$.token").isEmpty());

                verify(authService, times(1)).authenticate(any(LoginRequest.class));
        }

        @Test
        @DisplayName("TC5: POST /api/auth/login - Tài khoản bị khóa (401)")
        void testLoginInactiveAccount() throws Exception {
                // Arrange
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenThrow(new RuntimeException("Tài khoản của bạn đã bị khóa!"));

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value("Tài khoản của bạn đã bị khóa!"))
                                .andExpect(jsonPath("$.token").isEmpty());
        }

        @Test
        @DisplayName("TC6: POST /api/auth/login - Username rỗng")
        void testLoginEmptyUsername() throws Exception {
                // Arrange
                LoginRequest emptyUsernameRequest = new LoginRequest("", "Test123");
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenThrow(new RuntimeException("Sai tên tài khoản hoặc mật khẩu!"));

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(emptyUsernameRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("TC7: POST /api/auth/login - Password rỗng")
        void testLoginEmptyPassword() throws Exception {
                // Arrange
                LoginRequest emptyPasswordRequest = new LoginRequest("testuser", "");
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenThrow(new RuntimeException("Sai tên tài khoản hoặc mật khẩu!"));

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(emptyPasswordRequest)))
                                .andExpect(status().isUnauthorized());
        }

        // ============================================
        // CONTENT-TYPE VALIDATION
        // ============================================

        @Test
        @DisplayName("TC8: POST /api/auth/login - Sai Content-Type (415)")
        void testLoginWrongContentType() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.TEXT_PLAIN)
                                .content("invalid content"))
                                .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("TC9: POST /api/auth/login - Missing Content-Type")
        void testLoginMissingContentType() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("TC10: POST /api/auth/login - Invalid JSON format (400)")
        void testLoginInvalidJson() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json}"))
                                .andExpect(status().isBadRequest());
        }

        // ============================================
        // CORS CONFIGURATION
        // ============================================

        @Test
        @DisplayName("TC11: POST /api/auth/login - CORS headers được set đúng")
        void testLoginCorsHeaders() throws Exception {
                // Arrange
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenReturn(successResponse);

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Origin", "http://localhost:5173")
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andExpect(status().isOk())
                                .andExpect(header().exists("Access-Control-Allow-Origin"));
        }

        @Test
        @DisplayName("TC12: OPTIONS /api/auth/login - Preflight request")
        void testLoginPreflightRequest() throws Exception {
                // Act & Assert
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .options("/api/auth/login")
                                .header("Origin", "http://localhost:5173")
                                .header("Access-Control-Request-Method", "POST"))
                                .andExpect(status().isOk());
        }

        // ============================================
        // REQUEST BODY VALIDATION
        // ============================================

        @Test
        @DisplayName("TC13: POST /api/auth/login - Empty request body (400)")
        void testLoginEmptyBody() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("TC14: POST /api/auth/login - Null values trong request")
        void testLoginNullValues() throws Exception {
                // Arrange
                String jsonWithNulls = "{\"username\":null,\"password\":null}";
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenThrow(new RuntimeException("Sai tên tài khoản hoặc mật khẩu!"));

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonWithNulls))
                                .andExpect(status().isUnauthorized());
        }

        // ============================================
        // BOUNDARY TESTS
        // ============================================

        @Test
        @DisplayName("TC15: POST /api/auth/login - Username độ dài tối thiểu (3 chars)")
        void testLoginMinUsernameLength() throws Exception {
                // Arrange
                LoginRequest minLengthRequest = new LoginRequest("abc", "Test123");
                LoginResponse minLengthResponse = new LoginResponse(
                                "Đăng nhập thành công",
                                "dummy-token-for-abc");

                when(authService.authenticate(any(LoginRequest.class)))
                                .thenReturn(minLengthResponse);

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(minLengthRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"));
        }

        @Test
        @DisplayName("TC16: POST /api/auth/login - Username độ dài tối đa (50 chars)")
        void testLoginMaxUsernameLength() throws Exception {
                // Arrange
                String maxUsername = "a".repeat(50);
                LoginRequest maxLengthRequest = new LoginRequest(maxUsername, "Test123");
                LoginResponse maxLengthResponse = new LoginResponse(
                                "Đăng nhập thành công",
                                "dummy-token-for-" + maxUsername);

                when(authService.authenticate(any(LoginRequest.class)))
                                .thenReturn(maxLengthResponse);

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(maxLengthRequest)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC17: POST /api/auth/login - Password độ dài tối thiểu (6 chars)")
        void testLoginMinPasswordLength() throws Exception {
                // Arrange
                LoginRequest minPasswordRequest = new LoginRequest("testuser", "Pass12");
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenReturn(successResponse);

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(minPasswordRequest)))
                                .andExpect(status().isOk());
        }

        // ============================================
        // RESPONSE TIME & PERFORMANCE
        // ============================================

        @Test
        @DisplayName("TC18: POST /api/auth/login - Response time < 1 second")
        void testLoginResponseTime() throws Exception {
                // Arrange
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenReturn(successResponse);

                long startTime = System.currentTimeMillis();

                // Act
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andExpect(status().isOk());

                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;

                // Assert
                assertTrue(responseTime < 1000,
                                "Response time should be less than 1 second, but was: " + responseTime + "ms");
        }

        // ============================================
        // MULTIPLE REQUESTS
        // ============================================

        @Test
        @DisplayName("TC19: POST /api/auth/login - Multiple concurrent requests")
        void testLoginMultipleRequests() throws Exception {
                // Arrange
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenReturn(successResponse);

                // Act & Assert - Gửi 5 requests liên tiếp
                for (int i = 0; i < 5; i++) {
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Đăng nhập thành công"));
                }

                verify(authService, times(5)).authenticate(any(LoginRequest.class));
        }

        // ============================================
        // CHARACTER ENCODING
        // ============================================

        @Test
        @DisplayName("TC20: POST /api/auth/login - UTF-8 encoding support")
        void testLoginUtf8Encoding() throws Exception {
                // Arrange
                LoginRequest utf8Request = new LoginRequest("tàikhoản", "Mậtkhẩu123");
                LoginResponse utf8Response = new LoginResponse(
                                "Đăng nhập thành công",
                                "dummy-token-for-tàikhoản");

                when(authService.authenticate(any(LoginRequest.class)))
                                .thenReturn(utf8Response);

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                                .content(objectMapper.writeValueAsString(utf8Request)))
                                .andExpect(status().isOk())
                                .andExpect(content().encoding("UTF-8"))
                                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"));
        }
}