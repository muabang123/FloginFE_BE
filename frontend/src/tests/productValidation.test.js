// src/tests/productValidation.test.js
import { validateProduct } from '../utils/productValidation';

describe('Product Validation Tests', () => {

    // Test case từ ví dụ [cite: 303-323]
    test('TC1: Product hợp lệ - không có lỗi', () => {
        const product = {
            ten: 'Laptop Dell',
            gia: 15000000,
            soLuong: 10,
            category: 'Electronics'
        };
        const errors = validateProduct(product);
        expect(Object.keys(errors).length).toBe(0);
    });

    // Test case từ ví dụ [cite: 274-285]
    test('TC2: Product name rỗng - trả về lỗi', () => {
        const product = { ten: '', gia: 1000, soLuong: 10 };
        const errors = validateProduct(product);
        expect(errors.ten).toBe('Tên sản phẩm không được để trống');
    });

    // Test case từ ví dụ [cite: 286-302]
    test('TC3: Price âm - trả về lỗi', () => {
        const product = { ten: 'Laptop', gia: -1000, soLuong: 10 };
        const errors = validateProduct(product);
        expect(errors.gia).toBe('Giá sản phẩm phải lớn hơn 0');
    });

    // === Các test case bổ sung cho boundary tests ===

    test('TC4 (Boundary): Tên sản phẩm quá ngắn (< 3) - trả về lỗi', () => {
        const product = { ten: 'Ab', gia: 1000, soLuong: 10 };
        const errors = validateProduct(product);
        expect(errors.ten).toBe('Tên sản phẩm phải có ít nhất 3 ký tự'); 
    });

    test('TC5 (Boundary): Tên sản phẩm quá dài (> 100) - trả về lỗi', () => {
        const product = { ten: 'a'.repeat(101), gia: 1000, soLuong: 10 };
        const errors = validateProduct(product);
        expect(errors.ten).toBe('Tên sản phẩm không được quá 100 ký tự');
    });

    test('TC6 (Boundary): Giá quá lớn (> 999,999,999) - trả về lỗi', () => {
        const product = { ten: 'Sản phẩm siêu đắt', gia: 1000000000, soLuong: 1 };
        const errors = validateProduct(product);
        expect(errors.gia).toBe('Giá sản phẩm không được vượt quá 999,999,999'); 
    });
    
    test('TC7 (Boundary): Số lượng âm (< 0) - trả về lỗi', () => {
        const product = { ten: 'Sản phẩm', gia: 100, soLuong: -1 };
        const errors = validateProduct(product);
        expect(errors.soLuong).toBe('Số lượng không được nhỏ hơn 0');
    });

    test('TC8 (Boundary): Số lượng quá lớn (> 99,999) - trả về lỗi', () => {
        const product = { ten: 'Sản phẩm', gia: 100, soLuong: 100000 };
        const errors = validateProduct(product);
        expect(errors.soLuong).toBe('Số lượng không được vượt quá 99,999'); 
    });

    test('TC9: Số lượng bằng 0 (hợp lệ) - không có lỗi', () => {
        const product = { ten: 'Hàng sắp hết', gia: 100, soLuong: 0 };
        const errors = validateProduct(product);
        expect(errors.soLuong).toBeUndefined(); // Không có lỗi [cite: 116]
    });
});