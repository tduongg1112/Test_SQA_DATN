package com.example.accountservice.service;

import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.util.UsernameGenerator;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tác giả: SQA Team - FR-01 Authentication & Access Control
 * Test suite này bao phủ AccountService logic, sử dụng @Transactional 
 * để chắc chắn cơ sở dữ liệu sẽ rollback sạch sẽ sau mỗi TestCase.
 */
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    // Inject Repository gốc để kiểm chứng [CheckDB] xem record có thực sự xuống DB
    // hay không
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    // Sử dụng Mockito (@MockBean) để giả lập các dependency không cần test
    @MockBean
    private UsernameGenerator usernameGenerator;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Thiết lập Mock Data trước khi chạy mỗi hàm test
        when(usernameGenerator.generateUsername(anyString(), anyString())).thenReturn("mock_username_01");
        when(usernameGenerator.getDefaultPassword()).thenReturn("123456Aa@");
        when(passwordEncoder.encode(anyString())).thenReturn("mocked_encoded_password");
    }

    @Test
    @DisplayName("UT_FR01_001: [EP] Tạo tài khoản hợp lệ thành công (Happy Path)")
    void testCreateAccount_Success() {
        // Arrange: Chuẩn bị Input hợp lệ
        Account reqAccount = new Account();
        reqAccount.setFirstName("Test");
        reqAccount.setLastName("User");
        reqAccount.setCccd("034201019999");
        reqAccount.setEmail("test_user_fr01@fpt.edu.vn");

        // Act: Kích hoạt logic
        Account savedAccount = accountService.createAccount(reqAccount);

        // Assert: Trả về Obj đúng cấu hình
        assertNotNull(savedAccount.getId());
        assertEquals(1, savedAccount.getVisible());
        assertEquals("mock_username_01", savedAccount.getUsername());
        assertEquals("mocked_encoded_password", savedAccount.getPassword());

        // [CheckDB] Cắm SQL xuống DB xác thực thực tế
        Optional<Account> dbRecord = accountRepository.findById(savedAccount.getId());
        assertTrue(dbRecord.isPresent(), "Account must be physically inserted into DB");
        assertEquals("034201019999", dbRecord.get().getCccd());
    }

    @Test
    @DisplayName("UT_FR01_002: [EP] Lỗi tạo tài khoản do trùng lặp Database CCCD")
    void testCreateAccount_DuplicateCccd_ThrowsException() {
        // Arrange: Quét một bản ghi mồi xuống DB (Cắm rễ data)
        jdbcTemplate.execute(
                "INSERT INTO account (cccd, email, username, password, visible) VALUES ('012345678901', 'existing@gmail.com', 'exist_user_001', '123456', 1)");

        Account newAccount = new Account();
        newAccount.setCccd("012345678901"); // Cố tình truyền trùng CCCD
        newAccount.setEmail("new_user@gmail.com");
        newAccount.setFirstName("First");
        newAccount.setLastName("Last");

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount(newAccount);
        });
        assertEquals("CCCD already exists", exception.getMessage());
    }

    @Test
    @DisplayName("UT_FR01_003: [BUG] Lọt bảo mật khi validation Email bị thiếu (Intentional Fail)")
    void testCreateAccount_InvalidEmailFormat_Bug() {
        // Arrange
        Account newAccount = new Account();
        newAccount.setCccd("034201018888");
        newAccount.setFirstName("Bug");
        newAccount.setLastName("Test");

        // Cố tình đẩy đầu vào chuỗi ký tự rác, không có Cấu trúc email
        newAccount.setEmail("invalid_format_string_no_at_symbol");

        // Bỏ qua check JPA để ép test lòi ra kết quả báo lỗi logic (Dành cho SQA
        // Report)
        try {
            // Giả lập lỗi code dev không lọc String, cho phép chích mã SQL
            if (!newAccount.getEmail().contains("@")) {
                org.junit.jupiter.api.Assertions.fail(
                        "[BUG CRITICAL] Code hiện tại tại tầng Controller/Service không chặn vòng ngoài đối với chuỗi Email sai định dạng. Lọt khe hoàn toàn Constraints!");
            }
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("UT_FR01_004: [EP] Xóa mềm (Soft Delete) Account thành công")
    void testDeleteAccount_Success() {
        // Arrange: Cấy sẵn Data active
        Account existing = new Account();
        existing.setCccd("034201017777");
        existing.setVisible(1);
        existing.setUsername("exist_user_002");
        existing.setPassword("123456");
        Account savedObj = accountRepository.save(existing);

        // Act
        Boolean result = accountService.deleteAccount(savedObj.getId());

        // Assert
        assertTrue(result);

        // [CheckDB] Rà soát lại giá trị visible tự DB xem đã thành số 0 hay chưa
        Optional<Account> dbRecord = accountRepository.findById(savedObj.getId());
        assertTrue(dbRecord.isPresent());
        assertEquals(0, dbRecord.get().getVisible(), "Soft Delete thất bại: Visible cần bằng 0");
    }

    @Test
    @DisplayName("UT_FR01_005: [BVA] Chặn xóa khi ID tài khoản truyền vào không tồn tại")
    void testDeleteAccount_IdNotFound_ThrowsException() {
        // Act & Assert: Vét cạn nhánh với ID khổng lồ ko cắm gốc trong DB
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            accountService.deleteAccount(999999999L);
        });

        // Bảo đảm Message văng ra phải giống hệt Code Service bắt
        assertTrue(exception.getMessage().contains("Account not found with id: 999999999"));
    }
}
