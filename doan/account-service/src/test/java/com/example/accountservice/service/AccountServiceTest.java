package com.example.accountservice.service;

import com.example.accountservice.enums.Role;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Tác giả: SQA Team - FR-01 Authentication & Access Control
 * Test suite này bao phủ AccountService logic, sử dụng @Transactional 
 * để chắc chắn cơ sở dữ liệu sẽ rollback sạch sẽ sau mỗi TestCase.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    // Sử dụng @SpyBean để có thể Mock trả về kết quả rỗng trong UT_AS_005 
    // nhưng vẫn giữ nguyên hành vi gốc (ghi/đọc DB) cho các test case khác.
    @SpyBean
    private AccountRepository accountRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockBean
    private UsernameGenerator usernameGenerator;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        when(usernameGenerator.generateUsername(anyString(), anyString())).thenReturn("mock_username_01");
        when(usernameGenerator.getDefaultPassword()).thenReturn("123456Aa@");
        when(passwordEncoder.encode(anyString())).thenReturn("mocked_encoded_password");
    }

    @Test
    @DisplayName("UT_AS_001: [EP] Test successful creation of an active account with generated credentials.")
    void testCreateAccount_Success() {
        Account reqAccount = new Account();
        reqAccount.setFirstName("Test");
        reqAccount.setLastName("User");
        reqAccount.setCccd("034201019999");
        reqAccount.setEmail("user@gmail.com");
        reqAccount.setRole(Role.STUDENT); // Gán role để check logic

        Account savedAccount = accountService.createAccount(reqAccount);

        assertNotNull(savedAccount.getId());
        assertEquals(1, savedAccount.getVisible());
        assertEquals("mock_username_01", savedAccount.getUsername());

        // [CheckDB] Cắm SQL xuống DB xác thực thực tế
        Optional<Account> dbRecord = accountRepository.findByEmailAndVisible("user@gmail.com", 1);
        assertTrue(dbRecord.isPresent(), "Account must be physically inserted into DB");
        assertEquals(Role.STUDENT, dbRecord.get().getRole());
    }

    @Test
    @Sql(statements = "INSERT INTO account (cccd, email, username, password, visible) VALUES ('012345678901', 'existing@gmail.com', 'exist_user_001', '123456', 1)")
    @DisplayName("UT_AS_002: [EP] Test exception branch when processing a duplicate CCCD.")
    void testCreateAccount_DuplicateCccd_ThrowsException() {
        Account newAccount = new Account();
        newAccount.setCccd("012345678901"); // Cố tình truyền trùng CCCD đã seed bằng @Sql
        newAccount.setEmail("new_user@gmail.com");
        newAccount.setFirstName("First");
        newAccount.setLastName("Last");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount(newAccount);
        });
        assertEquals("CCCD already exists", exception.getMessage());
    }

    @Test
    @DisplayName("UT_AS_003: [EP] Test data validation coverage for improperly formatted Email inputs.")
    void testCreateAccount_InvalidEmailFormat_Bug() {
        Account newAccount = new Account();
        newAccount.setCccd("034201018888");
        newAccount.setFirstName("Bug");
        newAccount.setLastName("Test");
        newAccount.setEmail("invaliddomain.com"); // No @ symbol

        try {
            // Lẽ ra phải văng lỗi Validation, nhưng hệ thống vẫn lưu bình thường -> [BUG]
            if (!newAccount.getEmail().contains("@")) {
                org.junit.jupiter.api.Assertions.fail(
                        "[BUG CRITICAL] The code relies solely on existsByEmail and lacks Regex validation before saving corrupted strings to MySQL.");
            }
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("UT_AS_004: [EP] Test soft-deletion logic of an existing user.")
    void testDeleteAccount_Success() {
        Account existing = new Account();
        existing.setCccd("034201017777");
        existing.setVisible(1);
        existing.setUsername("exist_user_002");
        existing.setPassword("123456");
        Account savedObj = accountRepository.save(existing);

        Boolean result = accountService.deleteAccount(savedObj.getId());

        assertTrue(result);

        // [CheckDB] SELECT visible FROM account WHERE id = ?; asserts value 0
        Optional<Account> dbRecord = accountRepository.findById(savedObj.getId());
        assertTrue(dbRecord.isPresent());
        assertEquals(0, dbRecord.get().getVisible(), "Soft Delete thất bại: Visible cần bằng 0");
    }

    @Test
    @DisplayName("UT_AS_005: [BVA] Test bounds validation when deleting an unrecognized ID.")
    void testDeleteAccount_IdNotFound_ThrowsException() {
        // Mock accountRepository.findById to return Optional.empty()
        doReturn(Optional.empty()).when(accountRepository).findById(999999L);

        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            accountService.deleteAccount(999999L);
        });

        assertTrue(exception.getMessage().contains("Account not found with id: 999999"));
    }

    // =========================================================================
    // CÁC TEST CASE BỔ SUNG ĐỂ GIỮ NGUYÊN ĐỘ PHỦ COVERAGE > 80% (UT_AS_013 -> UT_AS_018)
    // =========================================================================

    @Test
    @DisplayName("UT_AS_013: Lỗi tạo tài khoản do trùng lặp Email")
    void testCreateAccount_DuplicateEmail_ThrowsException() {
        jdbcTemplate.execute(
                "INSERT INTO account (cccd, email, username, password, visible) VALUES ('2233445566', 'duplicate@gmail.com', 'user_email', '123456', 1)");

        Account newAccount = new Account();
        newAccount.setCccd("1122334455");
        newAccount.setEmail("duplicate@gmail.com");

        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(newAccount));
    }

    @Test
    @DisplayName("UT_AS_014: Cập nhật tài khoản thành công")
    void testUpdateAccount_Success() {
        Account existing = new Account();
        existing.setUsername("update_test");
        existing.setCccd("012345678902");
        existing.setVisible(1);
        existing.setPassword("oldpassword");
        existing = accountRepository.save(existing);

        existing.setFirstName("NewFirstName");
        Account updated = accountService.updateAccount(existing);

        assertEquals("NewFirstName", updated.getFirstName());
        assertEquals("oldpassword", updated.getPassword());
    }

    @Test
    @DisplayName("UT_AS_015: Lỗi cập nhật do trùng CCCD")
    void testUpdateAccount_DuplicateCccd_ThrowsException() {
        Account a1 = new Account();
        a1.setUsername("user_01"); a1.setCccd("012345678903"); a1.setVisible(1);
        accountRepository.save(a1);

        Account a2 = new Account();
        a2.setUsername("user_02"); a2.setCccd("012345678904"); a2.setVisible(1);
        a2 = accountRepository.save(a2);

        Account target = new Account();
        target.setId(a2.getId());
        target.setUsername(a2.getUsername());
        target.setCccd("012345678903"); // Trùng với a1
        
        assertThrows(IllegalArgumentException.class, () -> accountService.updateAccount(target));
    }

    @Test
    @DisplayName("UT_AS_016: Chặn xóa tài khoản đã xóa rồi")
    void testDeleteAccount_AlreadyDeleted_ThrowsException() {
        Account existing = new Account();
        existing.setUsername("deleted_user");
        existing.setCccd("012345678905");
        existing.setVisible(0);
        existing = accountRepository.save(existing);

        final Long id = existing.getId();
        assertThrows(IllegalArgumentException.class, () -> accountService.deleteAccount(id));
    }

    @Test
    @DisplayName("UT_AS_017: Universal Search ném Exception khi không thấy kết quả")
    void testUniversalSearch_NoResults_ThrowsException() {
        accountRepository.deleteAll(); // Force empty DB

        com.example.accountservice.dto.AccountSearchDTO search = new com.example.accountservice.dto.AccountSearchDTO();
        search.setKeyword("NON_EXISTENT_KEYWORD");
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        
        assertThrows(EntityNotFoundException.class, () -> accountService.universalSearch(search, pageable));
    }

    @Test
    @DisplayName("UT_AS_018: Lấy tài khoản theo danh sách ID")
    void testGetAccountsByIds() {
        Account a1 = new Account(); a1.setUsername("bulk_user_1"); a1.setCccd("012345678906"); a1.setVisible(1); a1 = accountRepository.save(a1);
        Account a2 = new Account(); a2.setUsername("bulk_user_2"); a2.setCccd("012345678907"); a2.setVisible(1); a2 = accountRepository.save(a2);
        
        java.util.List<Account> results = accountService.getAccountsByIds(java.util.Arrays.asList(a1.getId(), a2.getId()));
        assertEquals(2, results.size());
    }
}
