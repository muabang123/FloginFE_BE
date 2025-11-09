-- CHỌN DATABASE CỦA BẠN
USE flogin_db;

-- XÓA BỎ CÁC BẢNG CŨ ĐỂ TRÁNH XUNG ĐỘT
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS login_history;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS categories; -- Xóa bảng 'categories' (số nhiều)
DROP TABLE IF EXISTS category;   -- Xóa bảng 'category' (số ít)

-- 1. TẠO BẢNG USERS (Khớp với User.java của bạn)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    role VARCHAR(10) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_is_active (is_active)
);

-- Thêm user 'admin' (ID=1)
INSERT INTO users (username, password, email, role, is_active, created_at)
VALUES ('admin', 'admin123', 'admin@example.com', 'ADMIN', 1, NOW());

-- 2. TẠO BẢNG CATEGORIES (Khớp với Category.java của bạn)
CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    description TEXT,
    is_active BIT NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    INDEX idx_name (name),
    INDEX idx_is_active (is_active)
);

-- Thêm 3 category mẫu (ID 1, 2, 3)
INSERT INTO categories (name, is_active, created_at, description)
VALUES 
('Electronics', 1, NOW(), 'Đồ điện tử'),
('Accessories', 1, NOW(), 'Phụ kiện'),
('Books', 1, NOW(), 'Sách');


-- 3. TẠO BẢNG PRODUCTS (Khớp với Product.java của bạn)
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    description VARCHAR(500),
    
    category_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    -- Ràng buộc khóa ngoại
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    
    -- Indexes
    INDEX idx_name (name),
    INDEX idx_category (category_id),
    INDEX idx_price (price),
    INDEX idx_created_by (created_by),
    INDEX idx_created_at (created_at)
);

-- Thêm sản phẩm mẫu (tạo bởi user 1, thuộc category 1 và 2)
INSERT INTO products (name, price, quantity, description, category_id, created_by, created_at)
VALUES 
('Laptop Pro X1', 35000000.00, 50, 'Laptop cấu hình mạnh', 1, 1, NOW()),
('Bàn phím cơ K10', 1800000.00, 120, 'Bàn phím cơ Blue switch', 2, 1, NOW());