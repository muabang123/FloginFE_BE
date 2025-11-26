/**
 * =========================================================================================
 * DANH SÁCH KIỂM TRA (CHECKLIST) UNIT TEST - AUTH SERVICE
 * =========================================================================================
 * I. MỤC TIÊU LOGIC NGHIỆP VỤ:
 * - Xác thực: Kiểm tra logic so sánh password và trạng thái tài khoản (Active/Inactive).
 * - Token: Đảm bảo Token được sinh ra đúng định dạng khi đăng nhập thành công.
 * * II. XỬ LÝ NGOẠI LỆ (EXCEPTION HANDLING):
 * - Kiểm tra việc ném ra RuntimeException khi thông tin đăng nhập sai.
 * - Đảm bảo thông báo lỗi (Error Message) rõ ràng và chính xác.
 * =========================================================================================
 */

package com.flogin.service;

import com.flogin.dto.LoginRequest;
import com.flogin.dto.LoginResponse;
import com.flogin.entity.User;
import com.flogin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests cho AuthService
 * Câu 3.1.2: Backend Unit Tests - Login Service (5 điểm)
 * 
 * Test Coverage:
 * - Login thành công với credentials hợp lệ
 * - Login thất bại với username không tồn tại
 * - Login thất bại với password sai
 * - Login thất bại với tài khoản bị khóa
 * - Validation các edge cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Khởi tạo test user cho mỗi test case
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("Test123");
        testUser.setEmail("test@example.com");
        testUser.setIsActive(true);
    }

    // ============================================
    // TEST CASE 1: Login thành công
    // ============================================
    
    @Test
    @DisplayName("TC1: Login thành công với credentials hợp lệ")
    void testAuthenticateSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "Test123");
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        LoginResponse response = authService.authenticate(request);

        // Assert
        assertNotNull(response, "Response không được null");
        assertEquals("Đăng nhập thành công", response.message(), 
                    "Message phải là 'Đăng nhập thành công'");
        assertNotNull(response.token(), "Token không được null");
        assertTrue(response.token().startsWith("dummy-token-for-"), 
                  "Token phải bắt đầu với 'dummy-token-for-'");
        
        // Verify repository được gọi đúng 1 lần
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    // ============================================
    // TEST CASE 2: Username không tồn tại
    // ============================================
    
    @Test
    @DisplayName("TC2: Login thất bại - Username không tồn tại")
    void testAuthenticateUsernameNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("wronguser", "Test123");
        when(userRepository.findByUsername("wronguser"))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.authenticate(request),
            "Phải throw RuntimeException khi username không tồn tại"
        );

        assertEquals("Sai tên tài khoản hoặc mật khẩu!", 
                    exception.getMessage(),
                    "Error message phải chính xác");
        
        verify(userRepository, times(1)).findByUsername("wronguser");
    }

    // ============================================
    // TEST CASE 3: Password không đúng
    // ============================================
    
    @Test
    @DisplayName("TC3: Login thất bại - Password sai")
    void testAuthenticateWrongPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "WrongPassword123");
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.authenticate(request),
            "Phải throw RuntimeException khi password sai"
        );

        assertEquals("Sai tên tài khoản hoặc mật khẩu!", 
                    exception.getMessage(),
                    "Error message phải chính xác");
        
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    // ============================================
    // TEST CASE 4: Tài khoản bị khóa
    // ============================================
    
    @Test
    @DisplayName("TC4: Login thất bại - Tài khoản bị khóa")
    void testAuthenticateInactiveUser() {
        // Arrange
        testUser.setIsActive(false); // Khóa tài khoản
        LoginRequest request = new LoginRequest("testuser", "Test123");
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.authenticate(request),
            "Phải throw RuntimeException khi tài khoản bị khóa"
        );

        assertEquals("Tài khoản của bạn đã bị khóa!", 
                    exception.getMessage(),
                    "Error message phải là 'Tài khoản của bạn đã bị khóa!'");
        
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    // ============================================
    // TEST CASE 5: Username rỗng
    // ============================================
    
    @Test
    @DisplayName("TC5: Login thất bại - Username rỗng")
    void testAuthenticateEmptyUsername() {
        // Arrange
        LoginRequest request = new LoginRequest("", "Test123");
        when(userRepository.findByUsername(""))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.authenticate(request)
        );

        assertEquals("Sai tên tài khoản hoặc mật khẩu!", 
                    exception.getMessage());
        
        verify(userRepository, times(1)).findByUsername("");
    }

    // ============================================
    // TEST CASE 6: Password rỗng
    // ============================================
    
    @Test
    @DisplayName("TC6: Login thất bại - Password rỗng")
    void testAuthenticateEmptyPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "");
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.authenticate(request)
        );

        assertEquals("Sai tên tài khoản hoặc mật khẩu!", 
                    exception.getMessage());
    }

    // ============================================
    // TEST CASE 7: Username với ký tự đặc biệt
    // ============================================
    
    @Test
    @DisplayName("TC7: Login với username chứa ký tự đặc biệt hợp lệ")
    void testAuthenticateUsernameWithSpecialChars() {
        // Arrange
        testUser.setUsername("test.user_123");
        LoginRequest request = new LoginRequest("test.user_123", "Test123");
        when(userRepository.findByUsername("test.user_123"))
            .thenReturn(Optional.of(testUser));

        // Act
        LoginResponse response = authService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals("Đăng nhập thành công", response.message());
        assertTrue(response.token().contains("test.user_123"));
    }

    // ============================================
    // TEST CASE 8: Username với độ dài tối thiểu
    // ============================================
    
    @Test
    @DisplayName("TC8: Login với username độ dài tối thiểu (3 ký tự)")
    void testAuthenticateMinLengthUsername() {
        // Arrange
        testUser.setUsername("abc");
        LoginRequest request = new LoginRequest("abc", "Test123");
        when(userRepository.findByUsername("abc"))
            .thenReturn(Optional.of(testUser));

        // Act
        LoginResponse response = authService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals("Đăng nhập thành công", response.message());
    }

    // ============================================
    // TEST CASE 9: Password với độ dài tối thiểu
    // ============================================
    
    @Test
    @DisplayName("TC9: Login với password độ dài tối thiểu (6 ký tự)")
    void testAuthenticateMinLengthPassword() {
        // Arrange
        testUser.setPassword("Pass12");
        LoginRequest request = new LoginRequest("testuser", "Pass12");
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        LoginResponse response = authService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals("Đăng nhập thành công", response.message());
    }

    // ============================================
    // TEST CASE 10: Case sensitive password
    // ============================================
    
    @Test
    @DisplayName("TC10: Password phân biệt hoa thường")
    void testAuthenticatePasswordCaseSensitive() {
        // Arrange
        testUser.setPassword("Test123");
        LoginRequest request = new LoginRequest("testuser", "test123"); // lowercase
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.authenticate(request)
        );

        assertEquals("Sai tên tài khoản hoặc mật khẩu!", 
                    exception.getMessage());
    }

    // ============================================
    // TEST CASE 11: Null username
    // ============================================
    
    @Test
    @DisplayName("TC11: Login thất bại - Username null")
    void testAuthenticateNullUsername() {
        // Arrange
        LoginRequest request = new LoginRequest(null, "Test123");
        when(userRepository.findByUsername(null))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, 
                    () -> authService.authenticate(request));
    }

    // ============================================
    // TEST CASE 12: Verify token generation
    // ============================================
    
    @Test
    @DisplayName("TC12: Kiểm tra token được generate đúng format")
    void testTokenGeneration() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "Test123");
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        LoginResponse response = authService.authenticate(request);

        // Assert
        assertNotNull(response.token());
        assertEquals("dummy-token-for-testuser", response.token(),
                    "Token format phải là 'dummy-token-for-{username}'");
    }

    // ============================================
    // TEST CASE 13: Multiple login attempts
    // ============================================
    
    @Test
    @DisplayName("TC13: Multiple login attempts thành công")
    void testMultipleLoginAttempts() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "Test123");
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act - Login 3 lần
        LoginResponse response1 = authService.authenticate(request);
        LoginResponse response2 = authService.authenticate(request);
        LoginResponse response3 = authService.authenticate(request);

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
        
        // Verify repository được gọi 3 lần
        verify(userRepository, times(3)).findByUsername("testuser");
    }

    // ============================================
    // TEST CASE 14: Username with spaces
    // ============================================
    
    @Test
    @DisplayName("TC14: Login thất bại - Username có khoảng trắng")
    void testAuthenticateUsernameWithSpaces() {
        // Arrange
        LoginRequest request = new LoginRequest("test user", "Test123");
        when(userRepository.findByUsername("test user"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, 
                    () -> authService.authenticate(request));
    }

    // ============================================
    // TEST CASE 15: Repository returns null user
    // ============================================
    
    @Test
    @DisplayName("TC15: Xử lý khi repository không tìm thấy user")
    void testAuthenticateRepositoryReturnsEmpty() {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent", "Test123");
        when(userRepository.findByUsername(anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.authenticate(request)
        );

        assertTrue(exception.getMessage().contains("Sai tên tài khoản"));
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }
}