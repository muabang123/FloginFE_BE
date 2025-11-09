import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ProductDetail from '../components/ProductDetail';
import * as productService from '../service/productService';

jest.mock('../service/productService');

const MOCK_PRODUCT = {
    id: 1,
    ten: 'Laptop Dell XPS',
    gia: 45000000,
    soLuong: 10
};

describe('c) ProductDetail Component', () => {

    beforeEach(() => {
        jest.resetAllMocks();
    });

    test('c.1 - Hiển thị "Dang tai..." ban đầu', () => {
        // SỬA LỖI: Bỏ cú pháp '(as jest.Mock)'
        productService.getProductById.mockImplementation(() => new Promise(() => {}));
        render(<ProductDetail id={1} />);
        expect(screen.getByText('Dang tai...')).toBeInTheDocument();
    });

    test('c.2 - Hiển thị chi tiết sản phẩm thành công (SAU KHI SỬA LỖI COMPONENT)', async () => {
        // SỬA LỖI: Bỏ cú pháp '(as jest.Mock)'
        productService.getProductById.mockResolvedValue(MOCK_PRODUCT);
        
        render(<ProductDetail id={1} />);

        // Chờ component nhận data và re-render (sau khi bạn sửa lỗi component)
        expect(await screen.findByText('Laptop Dell XPS')).toBeInTheDocument();
        expect(screen.getByText('Giá: 45000000')).toBeInTheDocument();
        expect(screen.getByText('Số lượng: 10')).toBeInTheDocument();
        expect(screen.queryByText('Dang tai...')).not.toBeInTheDocument();
    });

    test('c.3 - Hiển thị lỗi khi API thất bại', async () => {
        // SỬA LỖI: Bỏ cú pháp '(as jest.Mock)'
        productService.getProductById.mockRejectedValue(new Error('Not Found'));
        
        render(<ProductDetail id={999} />);

        expect(await screen.findByText('Khong tim thay san pham')).toBeInTheDocument();
        expect(screen.queryByText('Dang tai...')).not.toBeInTheDocument();
    });
});