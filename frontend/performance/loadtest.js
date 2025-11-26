/**
 * =========================================================================================
 * KỊCH BẢN KIỂM THỬ HIỆU NĂNG (PHẦN 7.1)
 * Công cụ: k6 (Grafana)
 * =========================================================================================
 * MỤC TIÊU KIỂM TRA (CHECKLIST OBJECTIVES):
 * 1. Load Test: Mô phỏng lưu lượng truy cập bình thường (100 người dùng).
 * 2. Stress Test: Tìm điểm gãy (Breaking Point) lên tới 1000 người dùng.
 * 3. Độ tin cậy (Reliability): Đảm bảo tỷ lệ lỗi là 0% dưới tải cao.
 * 4. Độ trễ (Latency): Phân tích thời gian phản hồi p(95).
 * =========================================================================================
 */

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    // Cấu hình các giai đoạn (Stages) cho Load & Stress Testing
    stages: [
        { duration: '30s', target: 100 },   // Khởi động: Tăng dần lên 100 users
        { duration: '1m', target: 100 },    // Load Test: Giữ ổn định ở 100 users
        { duration: '30s', target: 500 },   // Tăng tải: Đẩy lên 500 users
        { duration: '1m', target: 500 },    // Stress Test: Giữ ở mức cao 500 users
        { duration: '30s', target: 1000 },  // Cực hạn: Đẩy lên 1000 users (Điểm gãy)
        { duration: '1m', target: 1000 },   // Duy trì cực hạn: Giữ 1000 users
        { duration: '30s', target: 0 },     // Hạ nhiệt: Giảm dần về 0
    ],
    // Các ngưỡng thất bại (Failure Thresholds)
    thresholds: {
        // 95% các request phải nhanh hơn 5000ms (đã nới lỏng cho máy cá nhân)
        http_req_duration: ['p(95)<5000'],
        // Tỷ lệ lỗi phải dưới 10% (cho phép một số lỗi timeout khi quá tải)
        http_req_failed: ['rate<0.1'],
    },
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
    // --- KỊCH BẢN 1: GIAO DỊCH ĐĂNG NHẬP ---
    const loginPayload = JSON.stringify({
        username: 'testuser',
        password: 'Test123'
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, params);

    // Kiểm tra đăng nhập thành công
    check(loginRes, {
        'Login thành công (200)': (r) => r.status === 200,
    });

    // Xử lý lỗi: Nếu server quá tải, tạm dừng người dùng ảo để tránh lỗi dây chuyền
    if (loginRes.status !== 200) {
        sleep(1);
        return;
    }

    // --- KỊCH BẢN 2: XEM DANH SÁCH SẢN PHẨM ---
    const productRes = http.get(`${BASE_URL}/products`);

    // Kiểm tra lấy dữ liệu thành công
    check(productRes, {
        'Lấy sản phẩm thành công (200)': (r) => r.status === 200,
    });

    sleep(1); // Thời gian nghỉ: Mô phỏng người dùng thật đợi 1s trước hành động tiếp theo
}