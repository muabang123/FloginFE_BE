import React, { useState } from "react";
import { Form, Button, Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import "./Login.css";

import BackgroundImage from "../assets/background.png";
import Logo from "../assets/Logo-DH-Sai-Gon-SGU.png";
import { login as loginService } from "../service/authService"; 
import { validateUsername, validatePassword } from "../utils/validation";

const Login = () => {
  const [inputUsername, setInputUsername] = useState("");
  const [inputPassword, setInputPassword] = useState("");

  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErrors({});
    setApiError("");

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
    try {
      console.log(`Đang gọi API... Username: ${inputUsername}`);
      const data = await loginService(inputUsername, inputPassword);

      if (data.token) {
        localStorage.setItem('authToken', data.token);
        navigate("/products");
      } else {
        setApiError("Lỗi không xác định từ server.");
      }

    } catch (error) {
      if (error.response && error.response.data && error.response.data.message) {
        setApiError(error.response.data.message);
      } else {
        setApiError(error.message || "Đã xảy ra lỗi, vui lòng thử lại.");
      }
    } finally {
      setLoading(false);
    }
  };

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

        {apiError && (
          <Alert
            className="mb-2"
            variant="danger"
            onClose={() => setApiError("")}
            dismissible
            data-testid="login-message" 
          >
            {apiError}
          </Alert>
        )}
        
        <Form.Group className="mb-2" controlId="username">
          <Form.Label>Tên đăng nhập</Form.Label>
          <Form.Control
            type="text"
            value={inputUsername}
            placeholder="Tên đăng nhập"
            onChange={(e) => setInputUsername(e.target.value)}
            required
            isInvalid={!!errors.username}
            data-testid="username-input" 
          />
          <Form.Control.Feedback type="invalid" data-testid="username-error">
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
            data-testid="password-input" 
          />
          <Form.Control.Feedback type="invalid" data-testid="password-error">
            {errors.password}
          </Form.Control.Feedback>
        </Form.Group>

        {!loading ? (
          <Button
            className="w-100"
            variant="primary"
            type="submit"
            data-testid="login-button" 
          >
            Đăng nhập
          </Button>
        ) : (
          <Button
            className="w-100"
            variant="primary"
            type="submit"
            disabled
            data-testid="login-button" 
          >
            Đang đăng nhập...
          </Button>
        )}
      </Form>
    </div>
  );
};

export default Login;