package com.flogin.controller;

import com.flogin.dto.LoginRequest;
import com.flogin.dto.LoginResponse;
import com.flogin.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
// Cho phép React (localhost:5173 hoặc 3000) gọi API này
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.authenticate(request);
            // Trả về 200 OK nếu thành công
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Trả về 401 Unauthorized nếu thất bại
            return ResponseEntity.status(401).body(new LoginResponse(e.getMessage(), null));
        }
    }
}