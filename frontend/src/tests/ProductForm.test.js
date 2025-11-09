import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import ProductForm from '../components/ProductForm';
import * as productService from '../service/productService'; // Import service

// Mock service (cho getAllCategories)
jest.mock('../service/productService');

// Mock data
const MOCK_CATEGORIES = [
    { id: 1, name: 'Electronics' },
    { id: 2, name: 'Accessories' },
];

describe('ProductForm Component Test', () => {
    const mockOnSave = jest.fn();
    const mockOnCancel = jest.fn();

    beforeEach(() => {
        mockOnSave.mockClear();
        mockOnCancel.mockClear();
        jest.resetAllMocks(); // Đảm bảo reset mock

        // Cung cấp mock cho API được gọi trong useEffect của ProductForm
        productService.getAllCategories.mockResolvedValue(MOCK_CATEGORIES);
    });

    test('TC1 (Form): Gửi form hợp lệ, hàm onSave được gọi với đúng data (bao gồm categoryId)', async () => {
        render(<ProductForm onSave={mockOnSave} onCancel={mockOnCancel} />);
        
        // Chờ categories tải xong
        expect(await screen.findByRole('option', { name: /Electronics/i })).toBeInTheDocument();

        // Điền dữ liệu
        await userEvent.type(screen.getByLabelText(/Tên sản phẩm/i), 'Laptop Mới');
        await userEvent.type(screen.getByLabelText(/Giá/i), '25000000');
        await userEvent.type(screen.getByLabelText(/Số lượng/i), '50');
        
        // Chọn category
        await userEvent.selectOptions(
            screen.getByLabelText(/Category/i),
            screen.getByRole('option', { name: /Accessories/i }) // Chọn category "Accessories" (ID 2)
        );

        // Nhấn nút Lưu
        await userEvent.click(screen.getByRole('button', { name: /Lưu/i }));

        // Chờ đợi và kiểm tra
        await waitFor(() => {
            expect(mockOnSave).toHaveBeenCalledTimes(1);
            // Kiểm tra dữ liệu gửi đi khớp với logic của ProductForm.jsx
            expect(mockOnSave).toHaveBeenCalledWith({
                ten: 'Laptop Mới',
                gia: 25000000,
                soLuong: 50,
                categoryId: 2,   // Đã chọn 'Accessories' (value là 2)
                createdById: 1 // Hardcoded trong component
            });
        });
    });

    test('TC2 (Form): Gửi form không hợp lệ (tên rỗng), onSave không được gọi', async () => {
        render(<ProductForm onSave={mockOnSave} onCancel={mockOnCancel} />);
        
        // Chờ categories tải xong
        expect(await screen.findByRole('option', { name: /Electronics/i })).toBeInTheDocument();

        // Điền dữ liệu không hợp lệ (tên rỗng)
        await userEvent.type(screen.getByLabelText(/Giá/i), '25000000');
        await userEvent.type(screen.getByLabelText(/Số lượng/i), '50');

        // Nhấn nút Lưu
        await userEvent.click(screen.getByRole('button', { name: /Lưu/i }));

        await waitFor(() => {
            // Lỗi validation phải xuất hiện
            expect(screen.getByText('Tên sản phẩm không được để trống')).toBeInTheDocument();
        });

        // Hàm onSave không được gọi
        expect(mockOnSave).not.toHaveBeenCalled();
    });

    test('TC3 (Form): Nhấn nút Hủy, hàm onCancel được gọi', async () => {
        render(<ProductForm onSave={mockOnSave} onCancel={mockOnCancel} />);
        
        // Chờ categories tải xong
        expect(await screen.findByRole('option', { name: /Electronics/i })).toBeInTheDocument();

        // Nhấn nút Hủy
        await userEvent.click(screen.getByRole('button', { name: /Hủy/i }));
        
        expect(mockOnCancel).toHaveBeenCalledTimes(1);
    });
});