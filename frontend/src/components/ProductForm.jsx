import React, { useState, useEffect } from 'react';
import { Form, Button, Alert, Spinner } from 'react-bootstrap';
import { validateProduct } from '../utils/productValidation';
// 1. Import (đổi tên) hàm service
import { getAllCategories } from '../service/productService';

const ProductForm = ({ productToEdit, onSave, onCancel }) => {
    const [ten, setTen] = useState('');
    const [gia, setGia] = useState('');
    const [soLuong, setSoLuong] = useState('');
    
    // State cho Category
    const [categoryId, setCategoryId] = useState(''); 
    const [categories, setCategories] = useState([]); // State để giữ danh sách categories
    const [loadingCategories, setLoadingCategories] = useState(false);
    
    const [errors, setErrors] = useState({});

    // 2. Tải danh sách categories khi form được mở
    useEffect(() => {
        const loadCategories = async () => {
            setLoadingCategories(true);
            const data = await getAllCategories();
            setCategories(data);
            setLoadingCategories(false);

            // Set giá trị mặc định cho ComboBox
            if (productToEdit) {
                setCategoryId(productToEdit.categoryId || (data.length > 0 ? data[0].id : ''));
            } else {
                setCategoryId(data.length > 0 ? data[0].id : ''); // Chọn cái đầu tiên
            }
        };

        loadCategories();
    }, [productToEdit]); // Tải lại mỗi khi mở form

    // 3. Điền dữ liệu vào form khi sửa
    useEffect(() => {
        if (productToEdit) {
            setTen(productToEdit.ten);
            setGia(productToEdit.gia);
            setSoLuong(productToEdit.soLuong);
            // categoryId đã được set ở useEffect trên
        } else {
            setTen('');
            setGia('');
            setSoLuong('');
        }
        setErrors({});
    }, [productToEdit]);

    const handleSubmit = (e) => {
        e.preventDefault();
        
        const productData = {
            ten,
            gia,
            soLuong,
            categoryId, // Thêm categoryId vào validation
        };
        
        const validationErrors = validateProduct(productData);
        setErrors(validationErrors);

        if (Object.keys(validationErrors).length > 0) {
            return;
        }

        const finalProductData = {
            ten: productData.ten,
            gia: parseFloat(productData.gia),
            soLuong: parseInt(productData.soLuong, 10),
            categoryId: parseInt(productData.categoryId, 10),
            createdById: 1 // Giả định user 'admin' (ID=1) là người tạo
        };
        
        onSave(finalProductData);
    };

    return (
        <Form onSubmit={handleSubmit} noValidate>
            {errors.general && <Alert variant="danger">{errors.general}</Alert>}
            
            <Form.Group className="mb-3" controlId="productTen">
                <Form.Label>Tên sản phẩm</Form.Label>
                <Form.Control
                    type="text"
                    placeholder="Nhập tên sản phẩm"
                    value={ten}
                    onChange={(e) => setTen(e.target.value)}
                    isInvalid={!!errors.ten}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.ten}
                </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3" controlId="productGia">
                <Form.Label>Giá</Form.Label>
                <Form.Control
                    type="number"
                    placeholder="Nhập giá"
                    value={gia}
                    onChange={(e) => setGia(e.target.value)}
                    isInvalid={!!errors.gia}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.gia}
                </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3" controlId="productSoLuong">
                <Form.Label>Số lượng</Form.Label>
                <Form.Control
                    type="number"
                    placeholder="Nhập số lượng"
                    value={soLuong}
                    onChange={(e) => setSoLuong(e.target.value)}
                    isInvalid={!!errors.soLuong}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.soLuong}
                </Form.Control.Feedback>
            </Form.Group>

            {/* 4. THAY THẾ Ô NHẬP BẰNG COMBOBOX (Form.Select) */}
            <Form.Group className="mb-3" controlId="productCategoryId">
                <Form.Label>Category</Form.Label>
                {loadingCategories ? (
                    <Spinner animation="border" size="sm" />
                ) : (
                    <Form.Select
                        value={categoryId}
                        onChange={(e) => setCategoryId(e.target.value)}
                        isInvalid={!!errors.categoryId}
                    >
                        {/* Tự động tạo các <option> từ API */}
                        {categories.map(cat => (
                            <option key={cat.id} value={cat.id}>
                                {cat.id} - {cat.name}
                            </option>
                        ))}
                    </Form.Select>
                )}
                <Form.Control.Feedback type="invalid">
                    {errors.categoryId}
                </Form.Control.Feedback>
            </Form.Group>

            <div className="d-flex justify-content-end">
                <Button variant="secondary" onClick={onCancel} className="me-2">
                    Hủy
                </Button>
                <Button variant="primary" type="submit">
                    Lưu
                </Button>
            </div>
        </Form>
    );
};

export default ProductForm;