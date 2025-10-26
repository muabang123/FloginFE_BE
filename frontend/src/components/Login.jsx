import React, { useState } from "react";
import { Form, Button, Alert } from "react-bootstrap";
import "./Login.css";

import BackgroundImage from "../assets/background.png";
import Logo from "../assets/Logo-DH-Sai-Gon-SGU.png";

import { validateUsername, validatePassword } from "../utils/validation";

const Login = () => {
  const [inputUsername, setInputUsername] = useState("");
  const [inputPassword, setInputPassword] = useState("");


  const [errors, setErrors] = useState({});
  const [showApiError, setShowApiError] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErrors({}); 
    setShowApiError(false);

    const usernameError = validateUsername(inputUsername);
    const passwordError = validatePassword(inputPassword);

    if (usernameError || passwordError) {
      setErrors({
        username: usernameError,
        password: passwordError,
      });
      return;
    }

    setLoading(true);
    await delay(500);
    console.log(`Username :${inputUsername}, Password :${inputPassword}`);

    if (inputUsername !== "admin" || inputPassword !== "admin") {
      setShowApiError(true); 
    }
    
    setLoading(false);
  };

  const handlePassword = () => {};

  function delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  return (
    <div
      className="sign-in__wrapper"
      style={{ backgroundImage: `url(${BackgroundImage})` }}
    >
      <div className="sign-in__backdrop"></div>
      <Form className="shadow p-4 bg-white rounded" onSubmit={handleSubmit} noValidate>
        <img
          className="img-thumbnail mx-auto d-block mb-2"
          src={Logo}
          alt="logo"
        />
        <div className="h4 mb-2 text-center">Sign In</div>

        {showApiError ? (
          <Alert
            className="mb-2"
            variant="danger"
            onClose={() => setShowApiError(false)}
            dismissible
          >
            Sai tên tài khoản hoặc mật khẩu!
          </Alert>
        ) : (
          <div />
        )}

        {/* 4. Hiển thị lỗi validation cho Username */}
        <Form.Group className="mb-2" controlId="username">
          <Form.Label>Tên đăng nhập</Form.Label>
          <Form.Control
            type="text"
            value={inputUsername}
            placeholder="Tên đăng nhập"
            onChange={(e) => setInputUsername(e.target.value)}
            required
            isInvalid={!!errors.username} 
          />

          <Form.Control.Feedback type="invalid">
            {errors.username}
          </Form.Control.Feedback>
        </Form.Group>
        <Form.Group className="mb-2" controlId="password">
          <Form.Label>Mật khẩu</Form.Label>
          <Form.Control
            type="password"
            value={inputPassword}
            placeholder="Mật khẩu"
            onChange={(e) => setInputPassword(e.target.value)}
            required
            isInvalid={!!errors.password} 
          />
          <Form.Control.Feedback type="invalid">
            {errors.password}
          </Form.Control.Feedback>
        </Form.Group>
        
        {!loading ? (
          <Button className="w-100" variant="primary" type="submit">
            Đăng nhập
          </Button>
        ) : (
          <Button className="w-100" variant="primary" type="submit" disabled>
            Đang đăng nhập...
          </Button>
        )}
      </Form>
    </div>
  );
};

export default Login;