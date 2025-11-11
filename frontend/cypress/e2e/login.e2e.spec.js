// frontend/cypress/e2e/login.e2e.spec.js

// Import POM đã tạo
import { LoginPage } from "../support/pageObjects/LoginPage";

describe("Login E2E Tests", () => {
  const loginPage = new LoginPage();

  beforeEach(() => {
    // Truy cập trang login trước mỗi test
    loginPage.visit();
  });

  // Yêu cầu d) Test UI elements interactions
  it("TC1: Nên hiển thị form login chính xác", () => {
    loginPage.getUsernameInput().should("be.visible");
    loginPage.getPasswordInput().should("be.visible");
    loginPage.getLoginButton().should("be.visible").and("contain", "Đăng nhập");
  });

  // Yêu cầu b) Test validation messages
  it("TC2: Nên hiển thị lỗi validation khi submit form rỗng", () => {
    loginPage.getLoginButton().click();

    // Kiểm tra lỗi (dựa trên Login.jsx và validation.js của bạn)
    loginPage.getUsernameError()
      .should("be.visible")
      .and("contain", "Tên đăng nhập không được để trống");
      
    loginPage.getPasswordError()
      .should("be.visible")
      .and("contain", "Mật khẩu không được để trống");
  });

  // Yêu cầu a, c) Test success flow
  it("TC3: Nên login thành công và điều hướng tới /products", () => {
    // Lưu ý: Test này cần Backend phải đang chạy
    // và có tài khoản (testuser, Test123) tồn tại trong DB.
    
    // (Nếu bạn không muốn test với DB thật, bạn phải dùng cy.intercept()
    // để mock API, tương tự như cách bạn mock authService trong Jest)
    
    loginPage.login("admin", "admin123");

    cy.wait("@loginSuccess");

    // Kiểm tra điều hướng (dựa theo navigate("/products") trong Login.jsx)
    cy.url().should("include", "/products");
  });

  // Yêu cầu c) Test error flow
  it("TC4: Nên hiển thị lỗi API khi credentials sai", () => {
    // Test này cũng cần Backend đang chạy
    loginPage.login("wronguser", "Wrongpass123");

    cy.wait("@loginFail");

    // Kiểm tra lỗi API (data-testid="login-message" trong Login.jsx)
    loginPage.getApiError()
      .should("be.visible")
      .and("contain", "Sai tên tài khoản hoặc mật khẩu!"); // (Hoặc message lỗi chính xác từ API của bạn)
      
    // Đảm bảo vẫn ở trang login
    cy.url().should("not.include", "/products");
  });
});