// src/App.jsx

import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import './App.css'; 
import ProductManagement from './components/ProductManagement';
import AppNavbar from './components/AppNavbar';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <div className="App">
      <Routes>
        {/* Route 1: Trang Login (không có Navbar) */}
        <Route path="/" element={<Login />} />
        
        {/* Route 2: Trang Products (có Navbar) */}
        <Route 
          path="/products" 
          element={
            <> {/* 2. Dùng Fragment để render nhiều component */}
              <AppNavbar />
              <ProductManagement />
            </>
          } 
        />
      </Routes>
    </div>
  );
}
export default App;