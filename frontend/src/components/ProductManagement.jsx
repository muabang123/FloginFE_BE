// src/components/ProductManagement.jsx
import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Container, Alert, Spinner } from 'react-bootstrap';
import * as productService from '../service/productService';
import ProductForm from './ProductForm';
import './ProductManagement.css';

const ProductManagement = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // State cho Modal (Form)
    const [showFormModal, setShowFormModal] = useState(false);
    const [currentProduct, setCurrentProduct] = useState(null); // null = Add, object = Edit

    // State cho Modal (Delete)
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [productToDelete, setProductToDelete] = useState(null);

    // Load data
    const loadProducts = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await productService.getAllProducts();
            setProducts(data);
        } catch (err) {
            setError('Không thể tải danh sách sản phẩm.');
        } finally {
            setLoading(false);
        }
    };

    // Chạy loadProducts khi component mount
    useEffect(() => {
        loadProducts();
    }, []);

    // --- Xử lý Modal Form (Add/Edit) ---
    const handleShowAddModal = () => {
        setCurrentProduct(null); // Đảm bảo là form Add
        setShowFormModal(true);
    };

    const handleShowEditModal = (product) => {
        setCurrentProduct(product); // Set sản phẩm cần sửa
        setShowFormModal(true);
    };

    const handleCloseFormModal = () => {
        setShowFormModal(false);
        setCurrentProduct(null);
    };

    // Hàm này được gọi từ ProductForm
    const handleSave = async (productData) => {
        try {
            if (currentProduct) {
                // Chế độ Edit
                await productService.updateProduct(currentProduct.id, productData);
            } else {
                // Chế độ Add
                await productService.createProduct(productData);
            }
            handleCloseFormModal();
            loadProducts(); // Tải lại danh sách
        } catch (err) {
            setError('Lưu sản phẩm thất bại.');
        }
    };

    // --- Xử lý Modal Delete ---
    const handleShowDeleteModal = (product) => {
        setProductToDelete(product);
        setShowDeleteModal(true);
    };

    const handleCloseDeleteModal = () => {
        setProductToDelete(null);
        setShowDeleteModal(false);
    };

    const handleDelete = async () => {
        if (!productToDelete) return;
        try {
            await productService.deleteProduct(productToDelete.id);
            handleCloseDeleteModal();
            loadProducts(); // Tải lại danh sách
        } catch (err) {
            setError('Xoá sản phẩm thất bại.');
        }
    };
    

    // --- Render ---
    return (
        <Container className="mt-4 product-management-container">
            <h2>Quản Lý Sản Phẩm</h2>
            
            <Button variant="primary" onClick={handleShowAddModal} className="mb-3">
                Thêm sản phẩm mới
            </Button>

            {error && <Alert variant="danger">{error}</Alert>}

            {loading ? (
                <div className="text-center">
                    <Spinner animation="border" />
                    <p>Đang tải...</p>
                </div>
            ) : (
                <Table striped bordered hover responsive>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tên sản phẩm</th>
                            <th>Giá</th>
                            <th>Số lượng</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        {products.map(product => (
                            <tr key={product.id}>
                                <td>{product.id}</td>
                                <td>{product.ten}</td>
                                <td>{product.gia.toLocaleString()} VNĐ</td>
                                <td>{product.soLuong}</td>
                                <td>
                                    <Button 
                                        variant="warning" 
                                        size="sm" 
                                        className="me-2"
                                        onClick={() => handleShowEditModal(product)}
                                    >
                                        Sửa
                                    </Button>
                                    <Button 
                                        variant="danger" 
                                        size="sm"
                                        onClick={() => handleShowDeleteModal(product)}
                                    >
                                        Xoá
                                    </Button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </Table>
            )}

            {/* Modal cho Add/Edit Form */}
            <Modal show={showFormModal} onHide={handleCloseFormModal} centered>
                <Modal.Header closeButton>
                    <Modal.Title>
                        {currentProduct ? 'Sửa Sản Phẩm' : 'Thêm Sản Phẩm Mới'}
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <ProductForm
                        productToEdit={currentProduct}
                        onSave={handleSave}
                        onCancel={handleCloseFormModal}
                    />
                </Modal.Body>
            </Modal>
            
            {/* Modal cho Delete Confirmation */}
            <Modal show={showDeleteModal} onHide={handleCloseDeleteModal} centered>
                <Modal.Header closeButton>
                    <Modal.Title>Xác nhận xoá</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Bạn có chắc chắn muốn xoá sản phẩm 
                    <strong> {productToDelete?.ten}</strong>?
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleCloseDeleteModal}>
                        Hủy
                    </Button>
                    <Button variant="danger" onClick={handleDelete}>
                        Xoá
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
};

export default ProductManagement;