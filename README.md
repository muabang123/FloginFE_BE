# Bài Tập Lớn - Kiểm Thử Phần Mềm
# Ứng dụng Đăng nhập & Quản lý Sản phẩm (Version 1.0)

Đây là đồ án môn học **Kiểm Thử Phần Mềm** tại Trường Đại học Sài Gòn.

Mục tiêu của dự án là xây dựng một ứng dụng web hoàn chỉnh bao gồm chức năng **Đăng nhập** và **Quản lý Sản phẩm (CRUD)**, đồng thời áp dụng các kỹ thuật kiểm thử phần mềm (Unit Test, Integration Test, E2E Test) theo phương pháp **Test-Driven Development (TDD)**.

## 🎓 Thông tin môn học

* **Trường:** Trường Đại học Sài Gòn
* **Khoa:** Khoa Công nghệ Thông tin
* **Môn học:** Kiểm Thử Phần Mềm
* **GVHD:** Từ Lãng Phiêu
* **Niên khóa:** 2024-2025

## 🚀 Công nghệ sử dụng

| Phần | Công nghệ | Mục đích |
| :--- | :--- | :--- |
| **Frontend** | React 18+ | Framework JavaScript |
| | Jest | Testing Framework |
| | React Testing Library | Testing cho React |
| | Axios | HTTP Client |
| **Backend** | Spring Boot 3.2+ | Framework Java |
| | Java 17+ | Ngôn ngữ lập trình |
| | JUnit 5 | Testing Framework |
| | Mockito | Mock Framework |
| | Spring Data JPA | Database Operations |
| | Maven | Build Tool |

## 📂 Cấu trúc thư mục

```text
FloginFE_BE/
├── frontend/       # Ứng dụng React
│   ├── src/
│   │   ├── components/
│   │   ├── services/  
│   │   ├── utils/     
│   │   └── tests/     
│   └── package.json   
└── backend/        # Ứng dụng Spring Boot
    ├── src/
    │   ├── main/java/com/flogin/
    │   │   ├── controller/
    │   │   ├── service/   
    │   │   ├── dto/       
    │   │   ├── entity/    
    │   │   └── repository/
    │   └── test/java/     
    └── pom.xml
```

## 🛠️ Cài đặt và Khởi chạy

Bạn cần chạy cả máy chủ Backend và Frontend để ứng dụng hoạt động đầy đủ.

### 1. Backend (Spring Boot)

1.  Di chuyển vào thư mục backend:
    ```bash
    cd backend
    ```
2.  Build dự án và cài đặt các dependencies (phụ thuộc) với Maven:
    ```bash
    mvn clean install
    ```
3.  Chạy máy chủ Spring Boot:
    ```bash
    mvn spring-boot:run
    ```
    > ℹ️ Backend sẽ khởi chạy tại địa chỉ `http://localhost:8080`.

### 2. Frontend (React)

1.  Mở một cửa sổ dòng lệnh (terminal) mới và di chuyển vào thư mục frontend:
    ```bash
    cd frontend
    ```
2.  Cài đặt các gói (packages) Node.js:
    ```bash
    npm install
    ```
3.  Khởi chạy máy chủ phát triển (development server):
    ```bash
    npm run dev
    ```
    > ℹ️ Frontend sẽ khởi chạy tại địa chỉ `http://localhost:3000` (hoặc một cổng khác được chỉ định trong cửa sổ dòng lệnh).

## 🧪 Chạy Kiểm thử (Running Tests)

Đây là phần cốt lõi của bài tập lớn, dùng để xác minh các chức năng của dự án.

### Backend Tests (JUnit & MockMvc)

Để chạy toàn bộ Unit Test và Integration Test cho backend:

```bash
cd backend
mvn clean test
