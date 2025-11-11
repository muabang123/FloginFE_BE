// frontend/cypress/support/pageObjects/ProductPage.js

export class ProductPage {
  // --- Điều hướng ---
  visit() {
    cy.visit("/products");
  }

  // --- Các hành động trên trang chính ---
  clickAddNew() {
    // Tìm nút "Thêm sản phẩm mới"
    cy.get("button").contains(/Thêm sản phẩm mới/i).click();
  }

  // --- Lấy phần tử ---
  getModal() {
    // Lấy modal đang hoạt động
    return cy.get(".modal-dialog");
  }

  getTableRow(productName) {
    // Tìm hàng (tr) trong bảng chứa tên sản phẩm
    return cy.get("table > tbody").contains("td", productName).parent("tr");
  }

  getAlert() {
    // Lấy alert báo lỗi (ví dụ: Xoá thất bại)
    return cy.get(".alert-danger");
  }
  getCategorySpinner() {
    // Tìm Spinner của react-bootstrap bên trong modal
    return this.getModal().find('.spinner-border');
  }

  // --- Các hành động trong Form (Modal) ---
  fillForm(product) {
    this.getModal().within(() => {
      if (product.name) {
        cy.get("form").find("label").contains(/Tên sản phẩm/i).next("input")
          .type(`{selectall}${product.name}`);
      }
      if (product.price) {
        cy.get("form").find("label").contains(/Giá/i).next("input")
          .type(`{selectall}${product.price}`);
      }
      if (product.quantity) {
        cy.get("form").find("label").contains(/Số lượng/i).next("input")
          .type(`{selectall}${product.quantity}`);
      }
      if (product.category) {
        // 'product.category' là text, ví dụ "Electronics"
        cy.get("form").find("label").contains(/Category/i).next("select").select(product.category);
      }
    });
  }

  submitForm() {
    this.getModal().find("button").contains(/Lưu/i).click();
  }

  cancelForm() {
    this.getModal().find("button").contains(/Hủy/i).click();
  }

  getFormValidationError(fieldLabel) {
    // Lấy text lỗi validation từ <Form.Control.Feedback>
    return cy.get("form").find("label").contains(fieldLabel).siblings(".invalid-feedback");
  }

  // --- Các hành động trên Hàng (Row) ---
  clickEditOnProduct(productName) {
    this.getTableRow(productName).within(() => {
      cy.get("button").contains(/Sửa/i).click();
    });
  }

  clickDeleteOnProduct(productName) {
    this.getTableRow(productName).within(() => {
      cy.get("button").contains(/Xoá/i).click();
    });
  }

  // --- Các hành động trong Modal Xoá ---
  confirmDelete() {
    this.getModal().find("button").contains(/Xoá/i).click();
  }

  cancelDelete() {
    this.getModal().find("button").contains(/Hủy/i).click();
  }
}