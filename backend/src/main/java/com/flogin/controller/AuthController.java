package com.flogin.controller;

import com.flogin.dto.LoginRequest;
import com.flogin.dto.LoginResponse;
import com.flogin.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

/**
 * =========================================================================================
 * CONTROLLER XÁC THỰC (AUTH CONTROLLER)
 * =========================================================================================
 * MỤC TIÊU (CHECKLIST III.9 - Module Interface):
 * - Cung cấp các endpoint liên quan đến đăng nhập và bảo mật.
 * - Validate dữ liệu đầu vào trước khi gọi Service.
 * - Cấu hình CORS cho phép Frontend truy cập.
 * =========================================================================================
 */
@RestController
@RequestMapping("/api/auth")
// Cho phép React (localhost:5173) gọi API này
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:8080" })
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Endpoint Đăng nhập
     * 
     * @param request       DTO chứa username và password
     * @param bindingResult Kết quả validate dữ liệu đầu vào
     * @return JWT Token hoặc thông báo lỗi
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            BindingResult bindingResult) {

        // 1. Kiểm tra lỗi Validation (Input Validation - Security Checklist)
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .findFirst()
                    .orElse("Sai tên tài khoản hoặc mật khẩu!");
            return ResponseEntity.status(401).body(new LoginResponse(message, null));
        }

        // 2. Gọi Service xử lý nghiệp vụ
        try {
            LoginResponse response = authService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Bắt lỗi nghiệp vụ từ Service (ví dụ: sai pass, khóa acc) và trả về 401
            return ResponseEntity.status(401).body(new LoginResponse(e.getMessage(), null));
        }
    }
}