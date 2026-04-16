package com.example.accountservice.util;

import com.example.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tác giả: SQA Team - Utility Layer Tests
 * Test thuần logic (không cần Spring Context), dùng @ExtendWith(MockitoExtension)
 * để khởi động nhanh, không cần load toàn bộ ApplicationContext.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UsernameGeneratorTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @BeforeEach
    void setUp() {
        // Mặc định: username chưa tồn tại trong DB
        when(accountRepository.existsByUsernameAndVisible(anyString(), anyInt())).thenReturn(false);
    }

    // =====================================================
    //  Tests cho UsernameGenerator
    // =====================================================

    @Test
    @DisplayName("UT_UTIL_001: [EP] Tạo username chuẩn từ họ tên tiếng Việt có dấu")
    void testGenerateUsername_VietnameseFullName_ReturnsCorrectUsername() {
        // "Nguyễn Hoàng" + "Hiệp" -> "hiepnh"
        String result = usernameGenerator.generateUsername("Hiệp", "Nguyễn Hoàng");
        assertEquals("hiepnh", result);
    }

    @Test
    @DisplayName("UT_UTIL_002: [EP] Tạo username từ tên tiếng Anh không dấu")
    void testGenerateUsername_EnglishName_ReturnsCorrectUsername() {
        // "Nguyen" + "Test" -> "testN"
        String result = usernameGenerator.generateUsername("Test", "Nguyen");
        assertEquals("testn", result);
    }

    @Test
    @DisplayName("UT_UTIL_003: [EP] Username trùng lặp thì thêm số vào cuối")
    void testGenerateUsername_DuplicateExists_AddsNumberSuffix() {
        // Giả lập: "hiepnh" đã tồn tại, "hiepnh1" thì chưa
        when(accountRepository.existsByUsernameAndVisible("hiepnh", 1)).thenReturn(true);
        when(accountRepository.existsByUsernameAndVisible("hiepnh1", 1)).thenReturn(false);

        String result = usernameGenerator.generateUsername("Hiệp", "Nguyễn Hoàng");
        assertEquals("hiepnh1", result);
    }

    @Test
    @DisplayName("UT_UTIL_004: [BVA] Ném IllegalArgumentException khi firstName null")
    void testGenerateUsername_NullFirstName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                usernameGenerator.generateUsername(null, "Nguyen"));
    }

    @Test
    @DisplayName("UT_UTIL_005: [BVA] Ném IllegalArgumentException khi lastName rỗng")
    void testGenerateUsername_EmptyLastName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                usernameGenerator.generateUsername("Test", "   "));
    }

    @Test
    @DisplayName("UT_UTIL_006: [EP] getDefaultPassword trả về đúng chuỗi mật khẩu mặc định")
    void testGetDefaultPassword_ReturnsCorrectValue() {
        assertEquals("123456Aa@", usernameGenerator.getDefaultPassword());
    }

    // =====================================================
    //  Tests cho ValidateUtil (static methods)
    // =====================================================

    @Test
    @DisplayName("UT_UTIL_007: [EP] validateKeyword với chuỗi hợp lệ trả về lowercase")
    void testValidateKeyword_ValidInput_ReturnsLowercase() {
        assertEquals("hello world", ValidateUtil.validateKeyword("  Hello World  "));
    }

    @Test
    @DisplayName("UT_UTIL_008: [BVA] validateKeyword với null trả về null")
    void testValidateKeyword_Null_ReturnsNull() {
        assertNull(ValidateUtil.validateKeyword(null));
    }

    @Test
    @DisplayName("UT_UTIL_009: [BVA] validateKeyword với chuỗi rỗng trả về null")
    void testValidateKeyword_BlankString_ReturnsNull() {
        assertNull(ValidateUtil.validateKeyword("   "));
    }

    @Test
    @DisplayName("UT_UTIL_010: [EP] cleanPositionIds loại bỏ ID âm và null")
    void testCleanPositionIds_WithInvalidIds_RemovesThem() {
        List<Long> input = new java.util.ArrayList<>(Arrays.asList(1L, -5L, null, 3L, 0L));
        List<Long> result = ValidateUtil.cleanPositionIds(input);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(3L));
    }

    @Test
    @DisplayName("UT_UTIL_011: [BVA] cleanPositionIds với list null trả về null")
    void testCleanPositionIds_NullList_ReturnsNull() {
        assertNull(ValidateUtil.cleanPositionIds(null));
    }

    @Test
    @DisplayName("UT_UTIL_012: [EP] validateSearchFields lọc chỉ giữ field hợp lệ")
    void testValidateSearchFields_WithMixedFields_ReturnsOnlyAllowed() {
        List<String> input = Arrays.asList("username", "invalidField", "email", null);
        List<String> result = ValidateUtil.validateSearchFields(input);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("username"));
        assertTrue(result.contains("email"));
    }

    @Test
    @DisplayName("UT_UTIL_013: [BVA] validateSearchFields với list rỗng trả về null")
    void testValidateSearchFields_EmptyList_ReturnsNull() {
        assertNull(ValidateUtil.validateSearchFields(List.of()));
    }
}
