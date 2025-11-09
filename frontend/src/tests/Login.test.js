// src/components/Login.test.jsx

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter } from 'react-router-dom'; // Cần thiết nếu Login dùng useNavigate
import Login from '../components/Login'; // Giả sử Login.jsx ở cùng thư mục

import * as authService from '../service/authService';
jest.mock('../service/authService');

// 2. Mock 'react-router-dom' để giả lập hàm useNavigate
// (Vì sau khi login thành công, component có thể sẽ điều hướng)
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'), // giữ lại các tính năng gốc
  useNavigate: () => mockNavigate,
}));

// Bắt đầu nhóm test
describe('Login Component Integration Tests', () => {
  // Reset mock trước mỗi test
  beforeEach(() => {
    jest.clearAllMocks();
    // Render component trong MemoryRouter
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );
  });

  // Yêu cầu a) Test rendering và user interactions
  test('renders login form elements', () => {
    // Kiểm tra các input và button đã render
    // (Sử dụng 'getByTestId' giống như trong hình ví dụ của bạn)
    expect(screen.getByTestId('username-input')).toBeInTheDocument();
    expect(screen.getByTestId('password-input')).toBeInTheDocument();
    expect(screen.getByTestId('login-button')).toBeInTheDocument();
  });

  test('allows user to type in inputs', () => {
    const usernameInput = screen.getByTestId('username-input');
    const passwordInput = screen.getByTestId('password-input');

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });

    expect(usernameInput.value).toBe('testuser');
    expect(passwordInput.value).toBe('password123');
  });

  // Yêu cầu c) Test error handling (Validation)
  test('hien thi loi khi submit form rong', async () => {
    const submitButton = screen.getByTestId('login-button');
    fireEvent.click(submitButton);

    // Chờ cho đến khi message lỗi xuất hiện
    await waitFor(() => {
      // (Giả sử bạn có testId 'username-error' và 'password-error'
      // giống như trong hình ví dụ)
      expect(screen.getByTestId('username-error')).toBeInTheDocument();
      expect(screen.getByTestId('password-error')).toBeInTheDocument();
    });

    // API không được gọi
    expect(authService.login).not.toHaveBeenCalled();
  });

  // Yêu cầu b) Test form submission và API calls
  // và Yêu cầu c) Test success messages (hoặc navigation)
  test('goi API va dieu huong khi submit form hop le', async () => {
    // Giả lập API trả về thành công
    authService.login.mockResolvedValue({ token: 'fake-token' });

    // Điền form
    fireEvent.change(screen.getByTestId('username-input'), {
      target: { value: 'testuser' },
    });
    fireEvent.change(screen.getByTestId('password-input'), {
      target: { value: 'Test123' },
    });

    // Submit form
    fireEvent.click(screen.getByTestId('login-button'));

    // Chờ và kiểm tra API đã được gọi đúng
    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledTimes(1);
      expect(authService.login).toHaveBeenCalledWith('testuser', 'Test123');
    });

    // Chờ và kiểm tra đã điều hướng thành công
    // (Dựa trên App.jsx, sau khi login sẽ tới /products)
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/products');
    });
  });

  // Yêu cầu c) Test error handling (API Error)
  test('hien thi loi khi API tra ve loi', async () => {
    // Giả lập API trả về lỗi
    const errorMessage = 'Tên đăng nhập hoặc mật khẩu không đúng';
    authService.login.mockRejectedValue(new Error(errorMessage));

    // Điền form
    fireEvent.change(screen.getByTestId('username-input'), {
      target: { value: 'user' },
    });
    fireEvent.change(screen.getByTestId('password-input'), {
      target: { value: 'wrongpass123' }, 
    });

    // Submit form
    fireEvent.click(screen.getByTestId('login-button'));

    // Chờ và kiểm tra API đã được gọi
    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledTimes(1);
    });

    // Chờ và kiểm tra message lỗi từ API được hiển thị
    // (Giả sử bạn dùng testId 'login-message' như trong hình ví dụ)
    await waitFor(() => {
      const errorAlert = screen.getByTestId('login-message');
      expect(errorAlert).toBeInTheDocument();
      expect(errorAlert).toHaveTextContent(errorMessage);
    });

    // Không điều hướng đi đâu cả
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});