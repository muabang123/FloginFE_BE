import React from 'react';
import { Navbar, Container, Nav, Button } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';

function AppNavbar() {
    const navigate = useNavigate();

    const handleLogout = () => {
        // Trong tương lai, bạn có thể xóa token tại đây
        console.log("Đã đăng xuất");
        localStorage.removeItem('authToken');
        navigate('/'); // Quay về trang Login
    };

    return (
        // Dùng `bg="dark"` và `variant="dark"` để có giao diện admin
        <Navbar bg="dark" variant="dark" expand="lg" sticky="top">
            <Container>
                {/* Dùng Link để không tải lại trang */}
                <Navbar.Brand as={Link} to="/products">
                    Flogin Admin
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                        <Nav.Link as={Link} to="/products">Quản lý Sản phẩm</Nav.Link>
                        {/* Bạn có thể thêm các link khác ở đây, ví dụ: Quản lý User */}
                    </Nav>
                    {/* Thêm nút Logout ở bên phải */}
                    <Nav>
                        <Button variant="outline-danger" onClick={handleLogout}>
                            Đăng xuất
                        </Button>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}

export default AppNavbar;