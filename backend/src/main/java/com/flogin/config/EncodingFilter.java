package com.flogin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * =========================================================================================
 * BỘ LỌC MÃ HÓA KÝ TỰ (ENCODING FILTER)
 * =========================================================================================
 * MỤC TIÊU (CHECKLIST III.8 - Input/Output):
 * - Đảm bảo tất cả request và response đều sử dụng bảng mã UTF-8.
 * - Ngăn chặn lỗi hiển thị font chữ Tiếng Việt khi truyền tải dữ liệu.
 * =========================================================================================
 */
@Component
public class EncodingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Thiết lập chuẩn UTF-8 cho cả chiều đi và chiều về
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Chuyển tiếp request đến filter tiếp theo trong chuỗi
        filterChain.doFilter(request, response);
    }
}