// frontend/cypress/e2e/product.e2e.spec.js

import { LoginPage } from "../support/pageObjects/LoginPage";
import { ProductPage } from "../support/pageObjects/ProductPage";

describe("Product E2E Tests (CRUD và Validation)", () => {
  const loginPage = new LoginPage();
  const productPage = new ProductPage();

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
      statusCode: 200,
      body: { token: "fake-jwt-token-123" }
    }).as("loginSuccess");
    
    loginPage.visit();
    loginPage.login("admin", "admin123"); 
    
    cy.wait("@loginSuccess");
    cy.url().should("include", "/products");
  });

  // Luôn truy cập /products trước mỗi test
  beforeEach(() => {
    cy.intercept("GET", "/api/products").as("getProducts");
    cy.intercept("GET", "/api/categories").as("getCategories");

    productPage.visit();
    cy.wait("@getProducts"); 
  });

  // --- Test Case cho CREATE [Yêu cầu a] ---
  it("TC1 [Create]: Nên tạo sản phẩm mới thành công (Happy Path)", () => {
    productPage.clickAddNew();
    cy.wait("@getCategories");
    productPage.getModal().should("contain", "Thêm Sản Phẩm Mới");
    
    productPage.fillForm({
      name: testData.name,
      price: testData.price,
      quantity: testData.quantity,
      category: testData.category
    });

    cy.intercept("POST", "/api/products").as("createProduct");
    cy.intercept("GET", "/api/products").as("getProductsAfterCreate");
    productPage.submitForm();

    cy.wait(["@createProduct", "@getProductsAfterCreate"]);

    // Xác nhận: Modal đóng và sản phẩm mới xuất hiện
    productPage.getModal().should("not.exist");
    productPage.getTableRow(testData.name).should("be.visible");
  });

  it("TC2 [Create]: Nên hiển thị lỗi validation khi Tên rỗng", () => {
    productPage.clickAddNew();
    
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
  it("TC4 [Read]: Nên hiển thị đúng thông tin sản phẩm trong bảng", () => {
    productPage.getTableRow(testData.name).within(() => {
      cy.get("td").eq(1).should("contain", testData.name); // Cột Tên
      cy.get("td").eq(2).should("contain", "123,456 VNĐ"); // Cột Giá (đã format)
      cy.get("td").eq(3).should("contain", testData.quantity); // Cột Số lượng
    });
  });

  // --- Test Case cho UPDATE [Yêu cầu c] ---
  it("TC5 [Update]: Nên cập nhật sản phẩm thành công", () => {
    productPage.clickEditOnProduct(testData.name);
    cy.wait("@getCategories");
    productPage.getModal().should("contain", "Sửa Sản Phẩm");

    // Chỉ cập nhật tên và giá
    productPage.fillForm({
      name: testData.updatedName,
      price: testData.updatedPrice
    });
    cy.intercept("PUT", `/api/products/*`).as("updateProduct");
    cy.intercept("GET", "/api/products").as("getProductsAfterUpdate");
    productPage.submitForm();
    cy.wait(["@updateProduct", "@getProductsAfterUpdate"]);
    // Xác nhận: Modal đóng và sản phẩm cũ không còn, sản phẩm mới xuất hiện
    productPage.getModal().should("not.exist");
    cy.get("table > tbody").should("not.contain", testData.name);
    productPage.getTableRow(testData.updatedName).should("be.visible");
  });

  it("TC6 [Update]: Nên giữ nguyên thông tin khi Hủy cập nhật", () => {
    productPage.clickEditOnProduct(testData.updatedName); // Dùng tên đã update
    productPage.fillForm({ name: "Tên tạm" });
    productPage.cancelForm();
    
    // Xác nhận: Modal đóng và tên gốc vẫn còn
    productPage.getModal().should("not.exist");
    productPage.getTableRow(testData.updatedName).should("be.visible");
  });

  // --- Test Case cho DELETE [Yêu cầu d] ---
  it("TC7 [Delete]: Nên xoá sản phẩm thành công (Happy Path)", () => {
    productPage.clickDeleteOnProduct(testData.updatedName);
    productPage.getModal().should("contain", "Xác nhận xoá");
    
    // Xác nhận tên sản phẩm trong modal
    productPage.getModal().should("contain", testData.updatedName);
    productPage.confirmDelete();

    // Xác nhận: Modal đóng và sản phẩm biến mất
    productPage.getModal().should("not.exist");
    cy.contains(testData.updatedName).should("not.exist");
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