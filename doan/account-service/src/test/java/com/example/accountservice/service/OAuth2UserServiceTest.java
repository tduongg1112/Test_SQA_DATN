package com.example.accountservice.service;

import com.example.accountservice.enums.Role;
import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tác giả: SQA Team - NFR-01 Authentication
 * Test Google OAuth2 Payload extraction and Database Synchronization.
 */
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class OAuth2UserServiceTest {

    @Autowired
    private OAuth2UserService oauth2UserService;

    @Autowired
    private AccountRepository accountRepository;

    private OAuth2UserRequest mockUserRequest;

    @BeforeEach
    void setUp() {
        // Giả lập Payload chuẩn chỉnh từ Google Client Registration
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("test-client")
                .authorizationUri("uri")
                .tokenUri("uri")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .build();
        
        mockUserRequest = mock(OAuth2UserRequest.class);
        when(mockUserRequest.getClientRegistration()).thenReturn(clientRegistration);
    }

    @Test
    @DisplayName("UT_FR01_006: [EP] User Google Login Lần Đầu (Tạo mới Account STUDENT)")
    void testProcessOAuth2User_FirstTimeLogin_CreatesNewAccount() {
        // Arrange: Cấu hình Payload Google trả về
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "new_student@fpt.edu.vn");
        attributes.put("name", "New Student Nguyen");
        attributes.put("picture", "http://google.com/pic1.jpg");
        
        OAuth2User mockOAuth2User = new DefaultOAuth2User(null, attributes, "email");

        // Act: Bắn Reflection để bypass Internet Request của hệ thống Spring
        ReflectionTestUtils.invokeMethod(oauth2UserService, "processOAuth2User", mockUserRequest, mockOAuth2User);

        // Assert/CheckDB: Tự động gen tài khoản STUDENT dưới DB
        Optional<Account> dbAccount = accountRepository.findByEmailAndVisible("new_student@fpt.edu.vn", 1);
        assertTrue(dbAccount.isPresent(), "Account phải được Insert vào DB ngay lập tức");
        assertEquals(Role.STUDENT, dbAccount.get().getRole());
        assertNotNull(dbAccount.get().getUsername(), "Unique Username Auto-Generator hoạt động");
    }

    @Test
    @DisplayName("UT_FR01_007: [EP] User Login Lại (Trigger Cập nhật Google Ảnh Đại Diện)")
    void testProcessOAuth2User_ReturningUser_UpdatesAccountDetails() {
        // Arrange: Cắm rễ Data một tài khoản từng tồn tại
        Account existing = new Account();
        existing.setUsername("old_google_user");
        existing.setEmail("old@google.com");
        existing.setPicture("OLD_PIC_URL");
        existing.setVisible(1);
        accountRepository.save(existing);

        // Chuẩn bị User Auth giả với ID Ảnh Đại Diện thay đổi
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "old@google.com");
        attributes.put("picture", "NEW_PIC_UPDATED_URL");
        OAuth2User mockOAuth2User = new DefaultOAuth2User(null, attributes, "email");

        // Act
        ReflectionTestUtils.invokeMethod(oauth2UserService, "processOAuth2User", mockUserRequest, mockOAuth2User);

        // Assert/CheckDB
        Account updatedAccount = accountRepository.findByEmailAndVisible("old@google.com", 1).get();
        assertEquals("NEW_PIC_UPDATED_URL", updatedAccount.getPicture(), "Service thất bại trong việc Sync Google Image");
    }

    @Test
    @DisplayName("UT_FR01_008: [BUG][BVA] Crash hệ thống do Server văng NullPointerException ở Payload rỗng")
    void testCreateAccountFromOAuth2_NullEmail_ThrowsNullPointerException() {
        // Arrange: Lấy Payload lỗi từ Google (Tài khoản Google Cổ hoặc không có Email)
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "1234"); // Key mặc định của Google 
        attributes.put("email", null); // Hoặc bị thiếu key
        
        OAuth2User mockOAuth2User = new DefaultOAuth2User(null, attributes, "sub");

        // Act & Assert (Intentional Report Fail)
        try {
            ReflectionTestUtils.invokeMethod(oauth2UserService, "processOAuth2User", mockUserRequest, mockOAuth2User);
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("[BUG CRITICAL] Hệ thống sập rớt luồng do NullPointerException khi Google Email rỗng. Dev quên check Null tại Service! Chi tiết: " + e.getCause());
        }
    }
}
