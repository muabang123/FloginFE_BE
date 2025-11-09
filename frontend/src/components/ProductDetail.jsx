// src/components/ProductDetail.jsx
import React, { useState, useEffect } from 'react';
import * as productService from '../service/productService'; 

const ProductDetail = ({ id }) => {
  const [product, setProduct] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProduct = async () => {
      try {
       const data = await productService.getProductById(id);
       setProduct(data); // Dòng bạn vừa sửa (ĐÚNG)
      } catch (err) {
        setError('Khong tim thay san pham');
      }
    };

    // THÊM DÒNG NÀY:
    // Bạn phải gọi hàm mà bạn đã định nghĩa
    fetchProduct(); 

  }, [id]); // Hook dependency đã đúng

  if (error) {
    return <div>{error}</div>;
  }

  if (!product) {
    return <div>Dang tai...</div>;
  }

  return (
    <div>
      <h1>{product.ten}</h1>
      <p>Giá: {product.gia}</p>
      <p>Số lượng: {product.soLuong}</p>
    </div>
  );
};

export default ProductDetail;