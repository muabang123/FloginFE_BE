// src/utils/productValidation.js

// Hàm này sẽ được test bởi productValidation.test.js
export const validateProduct = (product) => {
    const errors = {};

    // 1. Test product name validation [cite: 113, 262]
    if (!product.ten || product.ten.trim() === '') {
        errors.ten = 'Tên sản phẩm không được để trống';
    } else if (product.ten.length < 3) {
        errors.ten = 'Tên sản phẩm phải có ít nhất 3 ký tự';
    } else if (product.ten.length > 100) {
        errors.ten = 'Tên sản phẩm không được quá 100 ký tự';
    }

    // 2. Test price validation [cite: 115, 263]
    const gia = parseFloat(product.gia);
    if (isNaN(gia)) {
        errors.gia = 'Giá phải là một con số';
    } else if (gia <= 0) {
        errors.gia = 'Giá sản phẩm phải lớn hơn 0';
    } else if (gia > 999999999) {
        errors.gia = 'Giá sản phẩm không được vượt quá 999,999,999';
    }

    // 3. Test quantity validation [cite: 116, 264]
    const soLuong = parseInt(product.soLuong, 10);
    if (isNaN(soLuong)) {
        errors.soLuong = 'Số lượng phải là một con số';
    } else if (soLuong < 0) {
        errors.soLuong = 'Số lượng không được nhỏ hơn 0';
    } else if (soLuong > 99999) {
        errors.soLuong = 'Số lượng không được vượt quá 99,999';
    }

    // 4. Test description length (Giả sử có trường description) [cite: 117, 265]
    if (product.description && product.description.length > 500) {
        errors.description = 'Mô tả không được quá 500 ký tự';
    }
    
    // 5. Test category validation (Giả sử có trường category) [cite: 118, 266]
    if (!product.category) {
         // errors.category = 'Danh mục không được để trống';
         // Tài liệu không ghi rõ category là bắt buộc, nên tạm thời bỏ qua
    }

    return errors;
};