// src/services/productService.js
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/products'; // URL Backend

// <<< SỬA 1: Đồng bộ hóa tên thuộc tính (key) để khớp với component
const mockProducts = [
  {
    id: 1,
    ten: "Laptop Pro X1", // <-- Đã sửa (từ name)
    moTa: "Laptop cấu hình mạnh, màn hình 4K, 16GB RAM, 1TB SSD.", // <-- Đã sửa (từ description)
    gia: 35000000,        // <-- Đã sửa (từ price)
    danhMuc: "Electronics", // <-- Đã sửa (từ category)
    hinhAnh: "https://via.placeholder.com/300x200?text=Laptop+Pro+X1", // <-- Đã sửa (từ imageUrl)
    soLuong: 50           // <-- Đã sửa (từ stock)
  },
  {
    id: 2,
    ten: "Bàn phím cơ K10",
    moTa: "Bàn phím cơ Blue switch, full-size 104 phím, LED RGB.",
    gia: 1800000,
    danhMuc: "Accessories",
    hinhAnh: "https://via.placeholder.com/300x200?text=Keyboard+K10",
    soLuong: 120
  },
  {
    id: 3,
    ten: "Chuột quang không dây M720",
    moTa: "Chuột không dây đa thiết bị, kết nối Bluetooth và USB Receiver.",
    gia: 850000,
    danhMuc: "Accessories",
    hinhAnh: "https://via.placeholder.com/300x200?text=Mouse+M720",
    soLuong: 75
  },
  {
    id: 4,
    ten: "Màn hình UltraWide 34 inch",
    moTa: "Màn hình cong 34 inch, tỷ lệ 21:9, tần số quét 144Hz.",
    gia: 12500000,
    danhMuc: "Electronics",
    hinhAnh: "https://via.placeholder.com/300x200?text=Monitor+34+Inch",
    soLuong: 20
  }
];

// (GET) Lấy tất cả sản phẩm
// <<< SỬA 2: Thêm logic try...catch để dùng mock data làm dự phòng
export const getAllProducts = async () => {
    try {
        // Cố gắng gọi API thật
        const response = await axios.get(API_URL);
        return response.data; // Trả về dữ liệu thật nếu thành công
    } catch (error) {
        // Nếu API lỗi (ví dụ: backend chưa chạy), trả về dữ liệu mẫu
        console.warn("API Error: Không thể kết nối tới máy chủ. Sử dụng dữ liệu mẫu (mock data).");
        console.error(error);
        return Promise.resolve(mockProducts); // Trả về dữ liệu mẫu
    }
};

// (POST) Tạo sản phẩm mới
export const createProduct = async (product) => {
    try {
        const response = await axios.post(API_URL, product);
        return response.data;
    } catch (error) {
        console.error("Error creating product:", error);
        throw error;
    }
};

// (PUT) Cập nhật sản phẩm
export const updateProduct = async (id, product) => {
    try {
        const response = await axios.put(`${API_URL}/${id}`, product);
        return response.data;
    } catch (error) {
        console.error(`Error updating product ${id}:`, error);
        throw error;
    }
};

export const getProductById = async (id) => {
    try {
        const response = await axios.get(`${API_URL}/${id}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching product ${id}:`, error);
        throw error;
    }
};

// (DELETE) Xoá sản phẩm
export const deleteProduct = async (id) => {
    try {
        await axios.delete(`${API_URL}/${id}`);
    } catch (error) {
        console.error(`Error deleting product ${id}:`, error);
        throw error;
    }
};

const API_URL_CATEGORIES = 'http://localhost:8080/api/categories';

export const getAllCategories = async () => {
    try {
        const response = await axios.get(API_URL_CATEGORIES);
        return response.data; // Trả về mảng [ {id: 1, name: 'Electronics'}, ... ]
    } catch (error) {
        console.error("Lỗi khi tải danh mục:", error);
        // Trả về mảng rỗng nếu lỗi
        return []; 
    }
};