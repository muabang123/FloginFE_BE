// src/tests/Login.integration.test.js

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Login from '../components/Login';
import '@testing-library/jest-dom';

// 1. Mock 'authService' để ngăn lỗi Network Error
// File test này phải giả lập API giống như Login.test.js
import * as authService from '../service/authService';
jest.mock('../service/authService');
// ---------------------------------

// Cần mock 2 file ảnh
jest.mock('../assets/background.png', () => 'background-image');
jest.mock('../assets/Logo-DH-Sai-Gon-SGU.png', () => 'logo-image');

describe('Login Component Integration Tests', () => {

  // Reset mock trước mỗi test
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Test 1: Test validation (Yêu cầu a, c)
  test('Hiển thị lỗi validation khi submit form rỗng', async () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    const submitButton = screen.getByTestId('login-button');
    fireEvent.click(submitButton);

    await waitFor(() => {
      const usernameError = screen.getByTestId('username-error');
      expect(usernameError).toBeInTheDocument();
      expect(usernameError.textContent).toBe("Tên đăng nhập không được để trống");
      
      expect(screen.getByTestId('password-error')).toBeInTheDocument();
    });

    // Đảm bảo API không bị gọi nếu validation fail
    expect(authService.login).not.toHaveBeenCalled();
  });

  // Test 2: Test logic API (Yêu cầu b, c)
  test('Hiển thị lỗi API khi submit sai thông tin', async () => {
    // Giả lập API trả về lỗi
    authService.login.mockRejectedValue(new Error("Sai tên tài khoản hoặc mật khẩu!"));

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    const usernameInput = screen.getByTestId('username-input');
    const passwordInput = screen.getByTestId('password-input');
    const submitButton = screen.getByTestId('login-button');

    // Điền thông tin (hợp lệ validation, nhưng sai logic)
    fireEvent.change(usernameInput, { target: { value: 'user123' } });
    fireEvent.change(passwordInput, { target: { value: 'Pass123' } });

    fireEvent.click(submitButton);

    // Chờ đợi và kiểm tra lỗi API
    await waitFor(() => {
      // Sửa: Lỗi API bây giờ sẽ hiển thị trong 'login-message'
      const apiError = screen.getByTestId('login-message'); 
      expect(apiError).toBeInTheDocument();
      expect(apiError.textContent).toContain("Sai tên tài khoản hoặc mật khẩu!");
    });
  });

  // Test 3: Test flow thành công (Yêu cầu b, c)
  test('Gửi form hợp lệ và gọi API thành công', async () => {
    // Giả lập API trả về thành công (với token)
    authService.login.mockResolvedValue({ token: 'fake-token-123' });

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    const usernameInput = screen.getByTestId('username-input');
    const passwordInput = screen.getByTestId('password-input');
    const submitButton = screen.getByTestId('login-button');

    // Điền thông tin (hợp lệ)
    fireEvent.change(usernameInput, { target: { value: 'admin' } });
    fireEvent.change(passwordInput, { target: { value: 'admin123' } });

    fireEvent.click(submitButton);

    // Chờ đợi và kiểm tra API đã được gọi
    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledTimes(1);
      expect(authService.login).toHaveBeenCalledWith('admin', 'admin123');
    });
  });

  // Test 4: Test validation chi tiết (chỉ sai password)
  test('Hiển thị lỗi validation chỉ cho Mật khẩu (không có số)', async () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    const usernameInput = screen.getByTestId('username-input');
    const passwordInput = screen.getByTestId('password-input');
    const submitButton = screen.getByTestId('login-button');

    // Điền thông tin (username hợp lệ, password sai validation)
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'passwordkhongcoso' } });

    fireEvent.click(submitButton);

    await waitFor(() => {
      // Lỗi password phải xuất hiện
      const passwordError = screen.getByTestId('password-error');
      expect(passwordError).toBeInTheDocument();
      expect(passwordError.textContent).toBe("Mật khẩu phải chứa ít nhất một số");

      // Lỗi username không được xuất hiện
      // Dùng queryByTestId vì nó trả về null nếu không tìm thấy (thay vì văng lỗi)
      const usernameError = screen.queryByTestId('username-error');
      expect(usernameError.textContent).toBe("");
    });
  });

  // Test 5: Test trạng thái loading
  test('Hiển thị "Đang đăng nhập..." và vô hiệu hóa nút khi submit', async () => {
    // Giả lập API bị treo
    authService.login.mockImplementation(() => {
      return new Promise(resolve => setTimeout(() => resolve({ token: 'ok' }), 500));
    });

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    const usernameInput = screen.getByTestId('username-input');
    const passwordInput = screen.getByTestId('password-input');
    const submitButton = screen.getByTestId('login-button');

    // Điền thông tin hợp lệ
    fireEvent.change(usernameInput, { target: { value: 'admin' } });
    fireEvent.change(passwordInput, { target: { value: 'admin123' } });

    // Nhấn nút
    fireEvent.click(submitButton);

    // Kiểm tra ngay lập tức: nút bị vô hiệu hóa và đổi text
    expect(submitButton).toBeDisabled();
    expect(submitButton.textContent).toContain("Đang đăng nhập...");

    // Chờ cho API (giả) hoàn thành
    await waitFor(() => {
      // Nút phải trở lại bình thường
      expect(submitButton).not.toBeDisabled();
      expect(submitButton.textContent).toContain("Đăng nhập");
    });
  });

});