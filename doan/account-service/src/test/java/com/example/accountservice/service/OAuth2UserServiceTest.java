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
import org.springframework.test.context.ActiveProfiles;

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
    @DisplayName("UT_AS_006: [EP] First login scenario creating a new internal application profile.")
    void testProcessOAuth2User_FirstTimeLogin() {
        // Arrange: Mock OAuth2UserRequest containing email = "new_student@fpt.edu.vn" missing from DB
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "new_student@fpt.edu.vn");
        attributes.put("name", "New Student Nguyen");
        attributes.put("picture", "http://google.com/pic1.jpg");
        
        OAuth2User mockOAuth2User = new DefaultOAuth2User(null, attributes, "email");

        // Act
        ReflectionTestUtils.invokeMethod(oauth2UserService, "processOAuth2User", mockUserRequest, mockOAuth2User);

        // Assert: [CheckDB] SELECT role, username FROM account WHERE email = 'new_student@fpt.edu.vn';
        Optional<Account> dbAccount = accountRepository.findByEmailAndVisible("new_student@fpt.edu.vn", 1);
        assertTrue(dbAccount.isPresent(), "Account must be physically inserted into DB");
        assertEquals(Role.STUDENT, dbAccount.get().getRole());
        assertNotNull(dbAccount.get().getUsername(), "Method generates a unique username");
    }

    @Test
    @DisplayName("UT_AS_007: [EP] Repeat login scenario triggering an internal Sync update.")
    void testUpdateAccountFromOAuth2_RepeatLogin() {
        // Arrange
        Account existing = new Account();
        existing.setUsername("old_google_user");
        existing.setEmail("old@google.com");
        existing.setPicture("OLD_PIC_URL");
        existing.setVisible(1);
        accountRepository.save(existing);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "old@google.com");
        attributes.put("picture", "NEW_PIC_UPDATED_URL");
        OAuth2User mockOAuth2User = new DefaultOAuth2User(null, attributes, "email");

        // Act
        ReflectionTestUtils.invokeMethod(oauth2UserService, "processOAuth2User", mockUserRequest, mockOAuth2User);

        // Assert: [CheckDB] SELECT picture FROM account WHERE email = ?; reflects the new URL.
        Account updatedAccount = accountRepository.findByEmailAndVisible("old@google.com", 1).get();
        assertEquals("NEW_PIC_UPDATED_URL", updatedAccount.getPicture(), "Successfully updates picture.");
    }

    @Test
    @DisplayName("UT_AS_008: [BVA] Null boundary validation on Google OAuth Payload email extraction.")
    void testCreateAccountFromOAuth2_NullBoundary() {
        // Arrange: OAuth2UserInfo missing the email attribute (email=null).
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "1234"); 
        attributes.put("email", null); 
        
        OAuth2User mockOAuth2User = new DefaultOAuth2User(null, attributes, "sub");

        // Act & Assert
        try {
            ReflectionTestUtils.invokeMethod(oauth2UserService, "processOAuth2User", mockUserRequest, mockOAuth2User);
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("[BUG CRITICAL] Extremely brittle null-check logic at line userInfo.getEmail().split(\"@\"). Crashes server threads on deformed payloads.");
        }
    }
}
