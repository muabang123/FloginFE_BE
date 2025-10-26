
import { validateUsername, validatePassword } from '../utils/validation';
// Tests cho Username Validation
describe('Login Validation Tests - Username', () => {
  test('TC1: Username rỗng, trả về lỗi', () => {
    expect(validateUsername("")).toBe("Tên đăng nhập không được để trống");
  });

  test('TC2: Username quá ngắn (dưới 3 ký tự), trả về lỗi', () => {
    expect(validateUsername("ab")).toBe("Tên đăng nhập phải có ít nhất 3 ký tự");
  });

  test('TC3: Username quá dài (trên 50 ký tự), trả về lỗi', () => {
    const longUsername = "a".repeat(51);
    expect(validateUsername(longUsername)).toBe("Tên đăng nhập không được quá 50 ký tự");
  });

  test('TC4: Username chứa ký tự đặc biệt, trả về lỗi', () => {
    expect(validateUsername("user!@#")).toBe("Tên đăng nhập chỉ được chứa chữ cái và số");
  });

  test('TC5: Username hợp lệ (user123), không có lỗi', () => {
    expect(validateUsername("user123")).toBe("");
  });

  test('TC6: Username hợp lệ (Admin), không có lỗi', () => {
    expect(validateUsername("Admin")).toBe("");
  });
});

// Tests cho Password Validation
describe('Login Validation Tests - Password', () => {
  test('TC7: Password rỗng, trả về lỗi', () => {
    expect(validatePassword("")).toBe("Mật khẩu không được để trống");
  });

  test('TC8: Password quá ngắn (dưới 6 ký tự), trả về lỗi', () => {
    expect(validatePassword("pass1")).toBe("Mật khẩu phải có ít nhất 6 ký tự");
  });

  test('TC9: Password quá dài (trên 100 ký tự), trả về lỗi', () => {
    const longPassword = "a".repeat(101) + "1";
    expect(validatePassword(longPassword)).toBe("Mật khẩu không được quá 100 ký tự");
  });

  test('TC10: Password không có số, trả về lỗi', () => {
    expect(validatePassword("PasswordKhongCoSo")).toBe("Mật khẩu phải chứa ít nhất một số");
  });

  test('TC11: Password không có chữ, trả về lỗi', () => {
    expect(validatePassword("123456789")).toBe("Mật khẩu phải chứa ít nhất một chữ cái");
  });

  test('TC12: Password hợp lệ (Pass123), không có lỗi', () => {
    expect(validatePassword("Pass123")).toBe("");
  });
});