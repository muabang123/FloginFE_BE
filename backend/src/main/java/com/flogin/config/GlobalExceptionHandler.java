package com.flogin.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * =========================================================================================
 * XỬ LÝ NGOẠI LỆ TẬP TRUNG (GLOBAL EXCEPTION HANDLER)
 * =========================================================================================
 * MỤC TIÊU (CHECKLIST III.7 - Control Flow):
 * - Tập trung xử lý lỗi tại một nơi duy nhất (Clean Code).
 * - Đảm bảo mọi exception đều được trả về client dưới dạng HTTP Status phù hợp.
 * =========================================================================================
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý các lỗi RuntimeException chung
     * Phân loại lỗi dựa trên nội dung message để trả về status code đúng:
     * - 404 NOT FOUND: Nếu không tìm thấy dữ liệu.
     * - 400 BAD REQUEST: Các lỗi logic khác.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();

        // Kiểm tra các từ khóa lỗi đặc thù của nghiệp vụ
        if (message.contains("Không tìm thấy sản phẩm") ||
                message.contains("Không tìm thấy Category")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}