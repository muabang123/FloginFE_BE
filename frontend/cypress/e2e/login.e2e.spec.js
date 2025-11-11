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
    
    cy.intercept("POST", "/api/auth/login", {
      statusCode: 200,
      body: { token: "fake-jwt-token-123" }
    }).as("loginSuccess"); // Đặt tên cho nó là @loginSuccess

    // 2. Hành động
    loginPage.login("admin", "admin123");

    // 3. Chờ
    cy.wait("@loginSuccess"); // Giờ nó sẽ tìm thấy

    // 4. Kiểm tra
    cy.url().should("include", "/products");
  });

  // Yêu cầu c) Test error flow
  it("TC4: Nên hiển thị lỗi API khi credentials sai", () => {
    // Test này cũng cần Backend đang chạy
    cy.intercept("POST", "/api/auth/login", {
      statusCode: 401,
      body: { message: "Sai tên tài khoản hoặc mật khẩu!" }
    }).as("loginFail"); // Đặt tên là @loginFail

    // 2. Hành động
    loginPage.login("wronguser", "Wrongpass123");

    // 3. Chờ
    cy.wait("@loginFail"); // Giờ nó sẽ tìm thấy

    // 4. Kiểm tra
    loginPage.getApiError()
      .should("be.visible")
      .and("contain", "Sai tên tài khoản hoặc mật khẩu!");
      
    cy.url().should("not.include", "/products");
  });
});