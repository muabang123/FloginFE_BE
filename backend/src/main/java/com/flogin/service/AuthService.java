package com.flogin.service;

import com.flogin.dto.LoginRequest;
import com.flogin.dto.LoginResponse;
import com.flogin.entity.User;
import com.flogin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public LoginResponse authenticate(LoginRequest request) {
        // 1. Tìm user trong database
        Optional<User> userOptional = userRepository.findByUsername(request.username());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Sai tên tài khoản hoặc mật khẩu!");
        }

        User user = userOptional.get();

        // 2. KIỂM TRA MẬT KHẨU
        if (!user.getPassword().equals(request.password())) {
            throw new RuntimeException("Sai tên tài khoản hoặc mật khẩu!");
        }

        // 3. THÊM BƯỚC KIỂM TRA NÀY
        // (Sử dụng hàm isActive() từ file User.java của bạn)
        if (!user.isActive()) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa!");
        }

        // 4. Tạo token nếu mọi thứ đều ổn
        String token = "dummy-token-for-" + user.getUsername();
        return new LoginResponse("Đăng nhập thành công", token);
    }
}