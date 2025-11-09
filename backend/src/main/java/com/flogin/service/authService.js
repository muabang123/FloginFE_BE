// src/services/authService.js
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth'; // Giả sử đây là URL backend của bạn

export const login = async (username, password) => {
    try {
        // Backend Spring Boot thường mong đợi {username, password}
        const response = await axios.post(`${API_URL}/login`, {
            username,
            password,
        });
        // Giả sử backend trả về token hoặc thông báo thành công
        return response.data; 
    } catch (error) {
        console.error("Login error:", error);
        // Ném lỗi ra để component có thể bắt và xử lý
        throw error;
    }
};