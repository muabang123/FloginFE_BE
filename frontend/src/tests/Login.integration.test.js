import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter } from 'react-router-dom';
import Login from '../components/Login';

// --- PHẦN 1: MOCKING (Làm giả dữ liệu & Môi trường) ---

// 1. Mock Service API
import * as authService from '../service/authService';
jest.mock('../service/authService');

// 2. Mock các file Ảnh (QUAN TRỌNG: Để tránh lỗi "SyntaxError" khi Jest đọc file ảnh)
jest.mock('../assets/background.png', () => 'background-image');
jest.mock('../assets/Logo-DH-Sai-Gon-SGU.png', () => 'logo-image');

// 3. Mock Router (để kiểm tra chuyển trang)
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

// --- PHẦN 2: TEST SUITE ---

describe('Login Component - Comprehensive Integration Tests', () => {
  
  // Reset mọi dữ liệu giả trước khi chạy mỗi bài test
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // --- NHÓM TEST GIAO DIỆN CƠ BẢN ---
  
  test('1. Hiển thị đầy đủ các phần tử và cho phép nhập liệu', () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    const usernameInput = screen.getByTestId('username-input');
    const passwordInput = screen.getByTestId('password-input');
    const submitButton = screen.getByTestId('login-button');

    // Kiểm tra sự tồn tại
    expect(usernameInput).toBeInTheDocument();
    expect(passwordInput).toBeInTheDocument();
    expect(submitButton).toBeInTheDocument();

    // Kiểm tra nhập liệu (User gõ chữ)
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });

    expect(usernameInput.value).toBe('testuser');
    expect(passwordInput.value).toBe('password123');
  });

  // --- NHÓM TEST VALIDATION (Kiểm tra lỗi nhập liệu) ---

  test('2. Hiển thị lỗi validation khi submit form rỗng', async () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    // Click nút khi chưa nhập gì
    fireEvent.click(screen.getByTestId('login-button'));

    await waitFor(() => {
      // Kiểm tra thông báo lỗi cụ thể
      const usernameError = screen.getByTestId('username-error');
      expect(usernameError).toBeInTheDocument();
      expect(usernameError.textContent).toBe("Tên đăng nhập không được để trống");
      
      expect(screen.getByTestId('password-error')).toBeInTheDocument();
    });

    // Quan trọng: Đảm bảo API KHÔNG được gọi
    expect(authService.login).not.toHaveBeenCalled();
  });

  test('3. Hiển thị lỗi validation riêng lẻ (VD: Pass thiếu số)', async () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    const usernameInput = screen.getByTestId('username-input');
    const passwordInput = screen.getByTestId('password-input');

    // Nhập Username đúng, Password sai quy tắc (thiếu số)
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'passwordkhongcoso' } });

    fireEvent.click(screen.getByTestId('login-button'));

    await waitFor(() => {
      // Lỗi password phải hiện
      expect(screen.getByTestId('password-error')).toHaveTextContent("Mật khẩu phải chứa ít nhất một số");
      
      // Lỗi username KHÔNG được hiện (vì nhập đúng rồi)
      expect(screen.queryByTestId('username-error').textContent).toBe("");
    });
  });

  // --- NHÓM TEST LOGIC API (Gọi Server) ---

  test('4. Login thành công: Gọi API đúng tham số và chuyển trang', async () => {
    // Giả lập API trả về thành công
    authService.login.mockResolvedValue({ token: 'fake-token-123' });

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    // Điền thông tin hợp lệ
    fireEvent.change(screen.getByTestId('username-input'), { target: { value: 'admin' } });
    fireEvent.change(screen.getByTestId('password-input'), { target: { value: 'admin123' } });

    fireEvent.click(screen.getByTestId('login-button'));

    // Kiểm tra API được gọi
    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledTimes(1);
      expect(authService.login).toHaveBeenCalledWith('admin', 'admin123');
    });

    // Kiểm tra chuyển trang
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/products');
    });
  });

  test('5. Login thất bại: Hiển thị lỗi từ API và KHÔNG chuyển trang', async () => {
    // Giả lập API trả về lỗi
    const errorMessage = "Sai tên tài khoản hoặc mật khẩu!";
    authService.login.mockRejectedValue(new Error(errorMessage));

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );
    fireEvent.change(screen.getByTestId('username-input'), { target: { value: 'wronguser' } });
    
    fireEvent.change(screen.getByTestId('password-input'), { target: { value: 'Wrongpass1' } }); 

    fireEvent.click(screen.getByTestId('login-button'));

    // Kiểm tra hiển thị lỗi API
    await waitFor(() => {
      const apiErrorAlert = screen.getByTestId('login-message');
      expect(apiErrorAlert).toBeInTheDocument();
      expect(apiErrorAlert).toHaveTextContent(errorMessage);
    });

    // Đảm bảo không chuyển trang
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  // --- NHÓM TEST TRẢI NGHIỆM NGƯỜI DÙNG (UX) ---

  test('6. Hiển thị trạng thái Loading (Disabled nút) khi đang gọi API', async () => {
    // Giả lập mạng chậm (0.5s)
    authService.login.mockImplementation(() => {
      return new Promise(resolve => setTimeout(() => resolve({ token: 'ok' }), 500));
    });

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    // Điền và Submit
    fireEvent.change(screen.getByTestId('username-input'), { target: { value: 'user' } });
    fireEvent.change(screen.getByTestId('password-input'), { target: { value: 'pass123' } });
    fireEvent.click(screen.getByTestId('login-button'));

    const submitButton = screen.getByTestId('login-button');

    // CHECK 1: Ngay lập tức phải disable nút và hiện Loading
    expect(submitButton).toBeDisabled();
    expect(submitButton).toHaveTextContent("Đang đăng nhập...");

    // CHECK 2: Sau khi xong (0.5s) thì trở lại bình thường
    await waitFor(() => {
      expect(submitButton).not.toBeDisabled();
      expect(submitButton).toHaveTextContent("Đăng nhập");
    });
  });

});