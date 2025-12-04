// frontend/cypress/e2e/login.e2e.spec.js

// Import POM đã tạo
import { LoginPage } from "../support/pageObjects/LoginPage";

describe("Login E2E Tests", () => {
  const loginPage = new LoginPage();


  beforeEach(() => {
    // Truy cập trang login trước mỗi test
    cy.clearLocalStorage();
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

  // Yêu cầu a
  it("TC3: Nên login thành công và điều hướng tới /products", () => {

    cy.intercept("POST", "/api/auth/login", {
      statusCode: 200,
      body: { token: "fake-jwt-token-123" }
    }).as("loginSuccess"); 

    loginPage.login("admin", "admin123");

    cy.wait("@loginSuccess"); 

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

  //-------------------------------------------------------------------------------
  //Liêm thêm

  it("TC5: Nút Đăng nhập nên bị disabled khi đang gửi yêu cầu (FIXED)", () => {
    // Giả lập API trả về chậm
    cy.intercept("POST", "/api/auth/login", {
      delay: 1000,
      statusCode: 200,
      body: { token: "x" }
    }).as("slowLogin");

    loginPage.login("admin", "admin123");

    loginPage.getLoginButton().should("be.disabled");
    loginPage.getLoginButton().should("contain", "Đang đăng nhập...");

    cy.wait("@slowLogin");

    // Kiểm tra nút đã trở lại bình thường
    loginPage.getLoginButton().should("not.be.disabled");
    loginPage.getLoginButton().should("contain", "Đăng nhập");
  });

  it("TC6 [E2E]: Nên đăng xuất thành công", () => {
    // Test này cần 1 lần đăng nhập thành công
    cy.intercept("POST", "/api/auth/login", {
      statusCode: 200, body: { token: "fake-jwt-token-123" }
    }).as("loginSuccess");

    loginPage.login("admin", "admin123");
    cy.wait("@loginSuccess");
    cy.url().should("include", "/products");

    cy.get("button").contains(/Đăng xuất/i).click();

    // Xác nhận đã quay về trang login
    cy.url().should("not.include", "/products");
    cy.url().should("include", "/");

    // (Quan trọng) Kiểm tra xem localStorage đã bị xóa
    cy.window().then((win) => {
      expect(win.localStorage.getItem('authToken')).to.be.null;
    });
  });

  it("TC7 [Validation]: Nên hiển thị lỗi khi Mật khẩu không có số", () => {
    // Lấy logic từ validation.js
    loginPage.login("admin", "PasswordKhongCoSo");
    loginPage.getPasswordError()
      .should("be.visible")
      .and("contain", "Mật khẩu phải chứa ít nhất một số");
  });

  it("TC8 [Validation]: Nên hiển thị lỗi khi Tên đăng nhập quá ngắn", () => {
    // Lấy logic từ validation.js
    loginPage.login("ad", "admin123");
    loginPage.getUsernameError()
      .should("be.visible")
      .and("contain", "Tên đăng nhập phải có ít nhất 3 ký tự");
  });

  it("TC9 [UI]: Trường mật khẩu nên che ký tự (type='password')", () => {
    loginPage.getPasswordInput().should("have.attr", "type", "password");
  });

  it("TC10 [Validation]: Nên hiển thị lỗi khi Mật khẩu quá ngắn", () => {
    loginPage.login("admin", "123"); // Mật khẩu "123"
    loginPage.getPasswordError()
      .should("be.visible")
      .and("contain", "Mật khẩu phải có ít nhất 6 ký tự"); 
  });

});