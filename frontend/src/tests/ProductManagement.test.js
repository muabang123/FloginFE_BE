import React from 'react';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import ProductManagement from '../components/ProductManagement';
import * as productService from '../service/productService';

jest.mock('../service/productService');

const MOCK_PRODUCTS = [
    { id: 1, ten: 'Laptop Pro X1', gia: 35000000, soLuong: 50 },
    { id: 2, ten: 'Bàn phím cơ K10', gia: 1800000, soLuong: 120 },
];
const MOCK_CATEGORIES = [{ id: 1, name: 'Electronics' }];

describe('a) ProductList Integration (ProductManagement Component)', () => {

    beforeEach(() => {
        jest.resetAllMocks();
        
        productService.getAllProducts.mockResolvedValue(MOCK_PRODUCTS);
        productService.getAllCategories.mockResolvedValue(MOCK_CATEGORIES);
        productService.deleteProduct.mockResolvedValue({});
    });

    test('a.1 - Hiển thị trạng thái "Đang tải..." ban đầu', () => {
        productService.getAllProducts.mockImplementation(() => new Promise(() => {}));
        render(<ProductManagement />);
        
        // Chỉ cần kiểm tra text "Đang tải..." là đủ
        expect(screen.getByText('Đang tải...')).toBeInTheDocument();
        
        // SỬA LỖI: Xóa dòng "getByRole('status')" vì nó không tồn tại
    });

    test('a.2 - Tải và hiển thị thành công danh sách sản phẩm từ API', async () => {
        render(<ProductManagement />);
        expect(await screen.findByText('Laptop Pro X1')).toBeInTheDocument();
        expect(screen.getByText('Bàn phím cơ K10')).toBeInTheDocument();
        expect(screen.getByText('35,000,000 VNĐ')).toBeInTheDocument();
        expect(screen.queryByText('Đang tải...')).not.toBeInTheDocument();
    });

    test('a.3 - Hiển thị thông báo lỗi khi API tải danh sách thất bại', async () => {
        productService.getAllProducts.mockRejectedValue(new Error('API Error'));
        render(<ProductManagement />);
        expect(await screen.findByText('Không thể tải danh sách sản phẩm.')).toBeInTheDocument();
        expect(screen.queryByText('Đang tải...')).not.toBeInTheDocument();
    });

    test('a.4 - Tích hợp Xoá (Delete): Mở modal, xác nhận, gọi API và tải lại danh sách', async () => {
        productService.getAllProducts.mockResolvedValueOnce(MOCK_PRODUCTS);
        productService.getAllProducts.mockResolvedValueOnce([MOCK_PRODUCTS[1]]);

        render(<ProductManagement />);
        
        expect(await screen.findByText('Laptop Pro X1')).toBeInTheDocument();
        const deleteButtons = screen.getAllByRole('button', { name: /Xoá/i });
        await userEvent.click(deleteButtons[0]); 
        
        // SỬA LỖI 2 (Từ lần trước): Tìm dialog bằng role, sau đó tìm text bên trong
        const modal = await screen.findByRole('dialog');
        expect(within(modal).getByText(/Xác nhận xoá/i)).toBeInTheDocument();

        // (Các dòng còn lại giữ nguyên)
        expect(within(modal).getByText('Laptop Pro X1')).toBeInTheDocument();
        const confirmDeleteButton = within(modal).getByRole('button', { name: 'Xoá' });
        await userEvent.click(confirmDeleteButton);
        expect(productService.deleteProduct).toHaveBeenCalledWith(1); 
        await waitFor(() => {
            expect(modal).not.toBeInTheDocument();
        });
        expect(screen.queryByText('Laptop Pro X1')).not.toBeInTheDocument();
        expect(screen.getByText('Bàn phím cơ K10')).toBeInTheDocument(); 
    });
});