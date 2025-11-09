// src/components/ProtectedRoute.jsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

const ProtectedRoute = () => {
  // Kiểm tra xem token có tồn tại trong localStorage không
  const token = localStorage.getItem('authToken');

  if (!token) {
    // Nếu không có token, tự động chuyển hướng về trang Login
    return <Navigate to="/" replace />;
  }

  // Nếu có token, cho phép hiển thị component con (ví dụ: trang Products)
  return <Outlet />;
};

export default ProtectedRoute;