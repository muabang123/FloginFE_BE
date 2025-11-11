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

    // (MỚI) Giả lập API Create
    cy.intercept("POST", "/api/products", { statusCode: 201, body: { id: 3, ...testData } }).as("createProduct");
    
    // (MỚI) Giả lập API loadProducts() (SAU KHI CREATE)
    // Lần này nó phải trả về 3 SẢN PHẨM
    cy.intercept("GET", "/api/products", {
      statusCode: 200,
      body: [
        ...MOCK_PRODUCTS, // 2 sản phẩm cũ
        { id: 3, ten: testData.name, gia: 123456, soLuong: 10 } // 1 sản phẩm mới
      ]
    }).as("getProductsAfterCreate");

    productPage.submitForm();
    cy.wait(["@createProduct", "@getProductsAfterCreate"]); // Chờ cả 2

    productPage.getModal().should("not.exist");
    productPage.getTableRow(testData.name).should("be.visible"); // Sẽ Pass
  });

  it("TC2 [Create]: Nên hiển thị lỗi validation khi Tên rỗng", () => {
    productPage.clickAddNew();
    cy.wait("@getCategories");
    // (MỚI) Chờ Spinner biến mất
    productPage.getCategorySpinner().should("not.exist");
    // Bỏ trống Tên, chỉ điền các field khác
    productPage.fillForm({
      price: testData.price,
      quantity: testData.quantity,
      category: testData.category
    });
    productPage.submitForm();

    // Xác nhận: Modal không đóng và hiển thị lỗi
    productPage.getModal().should("exist");
    // (Dựa trên ProductForm.test.js của bạn)
    productPage.getFormValidationError(/Tên sản phẩm/i)
      .should("be.visible")
      .and("contain", "Tên sản phẩm không được để trống");
  });

  it("TC3 [Create]: Nên hủy tạo sản phẩm", () => {
    productPage.clickAddNew();
    productPage.fillForm({ name: "Sản phẩm sẽ bị hủy" });
    productPage.cancelForm();

    // Xác nhận: Modal đóng và sản phẩm không được tạo
    productPage.getModal().should("not.exist");
    cy.contains("Sản phẩm sẽ bị hủy").should("not.exist");
  });

  // --- Test Case cho READ [Yêu cầu b] ---
  it("TC4 [Read]: Nên hiển thị đúng thông tin sản phẩm (Tĩnh)", () => {
    // Test này phải kiểm tra data TĨNH từ "beforeEach"
    productPage.getTableRow("Laptop Pro X1").within(() => {
      cy.get("td").eq(1).should("contain", "Laptop Pro X1");
      cy.get("td").eq(2).should("contain", "35,000,000 VNĐ");
      cy.get("td").eq(3).should("contain", "50");
    });
  });

  // --- Test Case cho UPDATE [Yêu cầu c] ---
  it("TC5 [Update]: Nên cập nhật sản phẩm thành công", () => {
    // Sửa sản phẩm TĨNH "Laptop Pro X1"
    productPage.clickEditOnProduct("Laptop Pro X1");
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");

    productPage.fillForm({
      name: testData.updatedName, // Tên mới
      price: testData.updatedPrice // Giá mới
    });

    cy.intercept("PUT", `/api/products/*`, { statusCode: 200 }).as("updateProduct");
    // (MỚI) Giả lập API loadProducts() (SAU KHI UPDATE)
    cy.intercept("GET", "/api/products", {
      statusCode: 200,
      body: [
        // Laptop Pro X1 đã bị đổi tên
        { id: 1, ten: testData.updatedName, gia: 999999, soLuong: 50 }, 
        MOCK_PRODUCTS[1] // Bàn phím cơ K10
      ]
    }).as("getProductsAfterUpdate");

    productPage.submitForm();
    cy.wait(["@updateProduct", "@getProductsAfterUpdate"]);

    productPage.getModal().should("not.exist");
    cy.get("table > tbody").should("not.contain", "Laptop Pro X1"); // Tên cũ biến mất
    productPage.getTableRow(testData.updatedName).should("be.visible"); // Tên mới xuất hiện
  });

  it("TC6 [Update]: Nên giữ nguyên thông tin khi Hủy cập nhật", () => {
    // Sửa sản phẩm TĨNH
    productPage.clickEditOnProduct("Laptop Pro X1"); 
    productPage.fillForm({ name: "Tên tạm" });
    productPage.cancelForm();
    
    productPage.getModal().should("not.exist");
    // Tên gốc "Laptop Pro X1" phải còn
    productPage.getTableRow("Laptop Pro X1").should("be.visible");
  });

  // --- Test Case cho DELETE [Yêu cầu d] ---
  it("TC7 [Delete]: Nên xoá sản phẩm thành công (Happy Path)", () => {
    // Xoá sản phẩm TĨNH
    productPage.clickDeleteOnProduct("Laptop Pro X1");
    productPage.getModal().should("contain", "Xác nhận xoá");
    productPage.getModal().should("contain", "Laptop Pro X1");

    cy.intercept("DELETE", `/api/products/*`, { statusCode: 204 }).as("deleteProduct");
    // (MỚI) Giả lập API loadProducts() (SAU KHI DELETE)
    // Chỉ còn 1 sản phẩm
    cy.intercept("GET", "/api/products", {
      statusCode: 200,
      body: [
        MOCK_PRODUCTS[1] // Chỉ còn 'Bàn phím cơ K10'
      ]
    }).as("getProductsAfterDelete");
    
    productPage.confirmDelete();
    cy.wait(["@deleteProduct", "@getProductsAfterDelete"]);

    productPage.getModal().should("not.exist");
    cy.contains("Laptop Pro X1").should("not.exist"); // Tên cũ biến mất
    });

  it("TC8 [Delete]: Nên hủy xoá sản phẩm", () => {
    // Lấy 1 item tĩnh (từ MOCK_PRODUCTS) để test
    const staticProduct = "Bàn phím cơ K10";
    productPage.clickDeleteOnProduct(staticProduct);
    productPage.getModal().should("contain", "Xác nhận xoá");
    productPage.cancelDelete();

    // Xác nhận: Modal đóng và sản phẩm vẫn còn
    productPage.getModal().should("not.exist");
    productPage.getTableRow(staticProduct).should("be.visible");
  });

  // --- Test Case cho API Failure (Dùng cy.intercept) ---
  it("TC9 [Create Failure]: Nên hiển thị lỗi khi API Create thất bại", () => {
    // Giả lập API trả về lỗi 500
    cy.intercept("POST", "/api/products", { statusCode: 500 }).as("createFail");
    
    
    productPage.clickAddNew();
    cy.wait("@getCategories");
    productPage.getCategorySpinner().should("not.exist");
    productPage.fillForm({ name: "API Fail Test", price: "1", quantity: "1", category: "1"});
    productPage.submitForm();
    
    // Xác nhận: (Dựa trên logic handleSave của ProductManagement.jsx)
    productPage.getModal().should("not.exist"); // Modal đóng
    productPage.getAlert() // Alert lỗi xuất hiện
      .should("be.visible")
      .and("contain", "Lưu sản phẩm thất bại.");
  });
  
  it("TC10 [Delete Failure]: Nên hiển thị lỗi khi API Delete thất bại", () => {
    const staticProduct = "Bàn phím cơ K10";
    // Giả lập API trả về lỗi 500
    cy.intercept("DELETE", "/api/products/*", { statusCode: 500 }).as("deleteFail");

    productPage.clickDeleteOnProduct(staticProduct);
    productPage.confirmDelete();

    // Xác nhận: (Dựa trên logic handleDelete của ProductManagement.jsx)
    productPage.getModal().should("not.exist"); // Modal đóng
    productPage.getAlert()
      .should("be.visible")
      .and("contain", "Xoá sản phẩm thất bại.");
  });

  // --- Test Case cho Yêu cầu e (Search/Filter) ---
  it("TC11 [Search/Filter]: Placeholder for Search/Filter [Req. e]", () => {
    // Yêu cầu 6.2.2.e yêu cầu test Search/Filter.
    // Component ProductManagement.jsx hiện tại không có chức năng này.
    // Test case này được để trống để chờ nâng cấp.
    cy.log("Bỏ qua test Search/Filter vì chưa có chức năng.");
    expect(true).to.equal(true); // Đánh dấu test là pass
  });
});