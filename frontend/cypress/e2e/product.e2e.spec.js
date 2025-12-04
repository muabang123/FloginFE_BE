// frontend/cypress/e2e/product.e2e.spec.js

import { LoginPage } from "../support/pageObjects/LoginPage";
import { ProductPage } from "../support/pageObjects/ProductPage";

describe("Product E2E Tests (CRUD và Validation)", () => {
  const loginPage = new LoginPage();
  const productPage = new ProductPage();

  const MOCK_PRODUCTS = [
    { id: 1, ten: 'Laptop Pro X1', gia: 35000000, soLuong: 50, categoryId: 1 },
    { id: 2, ten: 'Bàn phím cơ K10', gia: 1800000, soLuong: 120, categoryId: 1 },
  ];

  // Tạo data test duy nhất để tránh xung đột
  const testData = {
    name: `Test Product ${Date.now()}`,
    price: "123456",
    quantity: "10",
    category: "1", // Phải khớp với text trong <option>

    updatedName: `UPDATED Product ${Date.now()}`,
    updatedPrice: "999999",
  };

  // Đăng nhập một lần duy nhất trước khi chạy tất cả test
  before(() => {
    cy.intercept("POST", "/api/auth/login", {
      statusCode: 200, body: { token: "fake-jwt-token-123" }
    }).as("loginSuccess");
    loginPage.visit();
    loginPage.login("admin", "admin123");
    cy.wait("@loginSuccess");
    cy.url().should("include", "/products");
  });

  // === SỬA "beforeEach" ===
  beforeEach(() => {
    cy.intercept("GET", "/api/categories", {
      statusCode: 200,
      body: [
        { id: 1, name: 'Electronics' },
        { id: 2, name: 'Accessories' }
      ]
    }).as("getCategories");

    // Mock GET /api/products trả về 2 sản phẩm
    cy.intercept("GET", "/api/products", {
      statusCode: 200, body: MOCK_PRODUCTS
    }).as("getProducts");

    productPage.visit();
    cy.wait("@getProducts");
  });

  // --- Test Case cho CREATE [Yêu cầu a] ---
  it("TC1 [Create]: Nên tạo sản phẩm mới thành công (Happy Path)", () => {
    productPage.clickAddNew();
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");

    productPage.fillForm({
      name: testData.name,
      price: testData.price,
      quantity: testData.quantity,
      category: testData.category
    });
    cy.intercept("POST", "/api/products", { statusCode: 201, body: { id: 3, ...testData } }).as("createProduct");

    cy.intercept("GET", "/api/products", {
      statusCode: 200,
      body: [
        ...MOCK_PRODUCTS,
        { id: 3, ten: testData.name, gia: 123456, soLuong: 10 } 
      ]
    }).as("getProductsAfterCreate");

    productPage.submitForm();
    cy.wait(["@createProduct", "@getProductsAfterCreate"]); 

    productPage.getModal().should("not.exist");
    productPage.getTableRow(testData.name).should("be.visible"); 
  });

  it("TC2 [Create]: Nên hiển thị lỗi validation khi Tên rỗng", () => {
    productPage.clickAddNew();
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");
    productPage.fillForm({
      price: testData.price,
      quantity: testData.quantity,
      category: testData.category
    });
    productPage.submitForm();
    productPage.getModal().should("exist");
    productPage.getFormValidationError(/Tên sản phẩm/i)
      .should("be.visible")
      .and("contain", "Tên sản phẩm không được để trống");
  });

  it("TC3 [Create]: Nên hủy tạo sản phẩm", () => {
    productPage.clickAddNew();
    productPage.fillForm({ name: "Sản phẩm sẽ bị hủy" });
    productPage.cancelForm();
    productPage.getModal().should("not.exist");
    cy.contains("Sản phẩm sẽ bị hủy").should("not.exist");
  });

  // --- Test Case cho READ [Yêu cầu b] ---
  it("TC4 [Read]: Nên hiển thị đúng thông tin sản phẩm (Tĩnh)", () => {
    productPage.getTableRow("Laptop Pro X1").within(() => {
      cy.get("td").eq(1).should("contain", "Laptop Pro X1");
      cy.get("td").eq(2).should("contain", "35,000,000 VNĐ");
      cy.get("td").eq(3).should("contain", "50");
    });
  });

  // --- Test Case cho UPDATE [Yêu cầu c] ---
  it("TC5 [Update]: Nên cập nhật sản phẩm thành công", () => {
    productPage.clickEditOnProduct("Laptop Pro X1");
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");

    productPage.fillForm({
      name: testData.updatedName,
      price: testData.updatedPrice 
    });

    cy.intercept("PUT", `/api/products/*`, { statusCode: 200 }).as("updateProduct");
    cy.intercept("GET", "/api/products", {
      statusCode: 200,
      body: [
        { id: 1, ten: testData.updatedName, gia: 999999, soLuong: 50 },
        MOCK_PRODUCTS[1]
      ]
    }).as("getProductsAfterUpdate");

    productPage.submitForm();
    cy.wait(["@updateProduct", "@getProductsAfterUpdate"]);

    productPage.getModal().should("not.exist");
    cy.get("table > tbody").should("not.contain", "Laptop Pro X1"); 
    productPage.getTableRow(testData.updatedName).should("be.visible");
  });

  it("TC6 [Update]: Nên giữ nguyên thông tin khi Hủy cập nhật", () => {
    productPage.clickEditOnProduct("Laptop Pro X1");
    productPage.fillForm({ name: "Tên tạm" });
    productPage.cancelForm();

    productPage.getModal().should("not.exist");
    productPage.getTableRow("Laptop Pro X1").should("be.visible");
  });

  // --- Test Case cho DELETE [Yêu cầu d] ---
  it("TC7 [Delete]: Nên xoá sản phẩm thành công (Happy Path)", () => {
    productPage.clickDeleteOnProduct("Laptop Pro X1");
    productPage.getModal().should("contain", "Xác nhận xoá");
    productPage.getModal().should("contain", "Laptop Pro X1");

    cy.intercept("DELETE", `/api/products/*`, { statusCode: 204 }).as("deleteProduct");
    cy.intercept("GET", "/api/products", {
      statusCode: 200,
      body: [
        MOCK_PRODUCTS[1]
      ]
    }).as("getProductsAfterDelete");

    productPage.confirmDelete();
    cy.wait(["@deleteProduct", "@getProductsAfterDelete"]);

    productPage.getModal().should("not.exist");
    cy.contains("Laptop Pro X1").should("not.exist"); 
  });

  it("TC8 [Delete]: Nên hủy xoá sản phẩm", () => {
    const staticProduct = "Bàn phím cơ K10";
    productPage.clickDeleteOnProduct(staticProduct);
    productPage.getModal().should("contain", "Xác nhận xoá");
    productPage.cancelDelete();
    productPage.getModal().should("not.exist");
    productPage.getTableRow(staticProduct).should("be.visible");
  });

  // --- Test Case cho API Failure (Dùng cy.intercept) ---
  it("TC9 [Create Failure]: Nên hiển thị lỗi khi API Create thất bại", () => {
    cy.intercept("POST", "/api/products", { statusCode: 500 }).as("createFail");

    productPage.clickAddNew();
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");
    productPage.fillForm({ name: "API Fail Test", price: "1", quantity: "1", category: "1" });
    productPage.submitForm();

    productPage.getModal().should("not.exist");
    productPage.getAlert()
      .should("be.visible")
      .and("contain", "Lưu sản phẩm thất bại.");
  });

  it("TC10 [Delete Failure]: Nên hiển thị lỗi khi API Delete thất bại", () => {
    const staticProduct = "Bàn phím cơ K10";
    cy.intercept("DELETE", "/api/products/*", { statusCode: 500 }).as("deleteFail");
    productPage.clickDeleteOnProduct(staticProduct);
    productPage.confirmDelete();
    productPage.getModal().should("not.exist"); 
    productPage.getAlert()
      .should("be.visible")
      .and("contain", "Xoá sản phẩm thất bại.");
  });

  // --- Test Case cho Yêu cầu e (Search/Filter) ---
  it("TC11 [Search/Filter]: Placeholder for Search/Filter [Req. e]", () => {
    cy.log("Bỏ qua test Search/Filter vì chưa có chức năng.");
    expect(true).to.equal(true);
  });

  //LiÊM THÊM

  // frontend/cypress/e2e/product.e2e.spec.js

  // === CÁC TEST CASE BỔ SUNG CHO PRODUCT ===

  it("TC12 [Validation]: Nên hiển thị lỗi validation khi Giá âm", () => {
    productPage.clickAddNew();
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");

    productPage.fillForm({
      name: "Sản phẩm giá âm",
      price: "-1000", 
      quantity: "10",
      category: "1"
    });
    productPage.submitForm();

    productPage.getModal().should("exist");
    productPage.getFormValidationError(/Giá/i)
      .should("be.visible")
      .and("contain", "Giá sản phẩm phải lớn hơn 0");
  });

  it("TC13 [Validation]: Nên hiển thị lỗi validation khi Số lượng âm", () => {
    productPage.clickAddNew();
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");

    productPage.fillForm({
      name: "Sản phẩm số lượng âm",
      price: "1000",
      quantity: "-5", 
      category: "1"
    });
    productPage.submitForm();

    productPage.getModal().should("exist");
    productPage.getFormValidationError(/Số lượng/i)
      .should("be.visible")
      .and("contain", "Số lượng không được nhỏ hơn 0");
  });



  it("TC14 [Update Failure]: Nên hiển thị lỗi khi API Update thất bại", () => {
    cy.intercept("PUT", "/api/products/*", { statusCode: 500 }).as("updateFail");

    productPage.clickEditOnProduct("Laptop Pro X1");
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");

    productPage.fillForm({ name: "Tên mới" });
    productPage.submitForm();
    cy.wait("@updateFail");

    productPage.getModal().should("not.exist");
    productPage.getAlert() 
      .should("be.visible")
      .and("contain", "Lưu sản phẩm thất bại.");
  });

  it("TC15 [UX]: Form nên được điền đúng Category khi Sửa", () => {
    productPage.clickEditOnProduct("Bàn phím cơ K10"); //
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");
    productPage.getModal().find("select#productCategoryId").should("have.value", "1");
  });

  it("TC16 [UX]: Form nên được reset sau khi Tạo thành công", () => {
    productPage.clickAddNew();
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");
    productPage.fillForm({ name: "SP 1", price: "1", quantity: "1", category: "1" });

    cy.intercept("POST", "/api/products", { statusCode: 201 }).as("createProduct");
    cy.intercept("GET", "/api/products", { statusCode: 200, body: MOCK_PRODUCTS }).as("getProducts"); // Mock load lại
    productPage.submitForm();
    cy.wait(["@createProduct", "@getProducts"]);
    productPage.getModal().should("not.exist");

    productPage.clickAddNew();
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");

    productPage.getModal().find("input#productTen").should("have.value", "");
    productPage.getModal().find("input#productGia").should("have.value", "");
  });

});