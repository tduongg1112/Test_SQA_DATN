package com.example.accountservice.service;

import com.example.accountservice.enums.Role;
import com.example.accountservice.model.Account;
import com.example.accountservice.model.RedisTokenInfo;
import com.example.accountservice.repository.RedisTokenRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tác giả: SQA Team - NFR-01 Authentication
 * Test Isolated Redis Caching Mechanics.
 */
@ExtendWith(MockitoExtension.class)
public class RedisTokenServiceTest {

    @InjectMocks
    private RedisTokenService redisTokenService;

    @Mock
    private RedisTokenRepository redisTokenRepository;

    @Test
    @DisplayName("UT_AS_009: [EP] Successful persistence of JWT signatures inside cache.")
    void testSaveTokenInfo_Success() {
        // Arrange
        String mockJti = "f9a3-k20";
        Account userMock = new Account();
        userMock.setUsername("admin_hanoi");
        userMock.setRole(Role.ADMIN);

        // Act
        redisTokenService.saveTokenInfo(mockJti, userMock);

        // Assert: Successfully calls .save() on Redis cache
        verify(redisTokenRepository, times(1)).deleteById("admin_hanoi");
        verify(redisTokenRepository, times(1)).save(any(RedisTokenInfo.class));
    }

    @Test
    @DisplayName("UT_AS_010: [EP] Resiliency check: ensure cache network interruptions do not crash auth.")
    void testSaveTokenInfo_NetworkInterruption() {
        // Arrange
        String mockJti = "f9a3-k20";
        Account userMock = new Account();
        userMock.setUsername("student_dn");

        // Redis throws internal RedisConnectionFailureException upon save (using RuntimeException as proxy)
        when(redisTokenRepository.save(any(RedisTokenInfo.class))).thenThrow(RuntimeException.class);

        // Act & Assert
        try {
            redisTokenService.saveTokenInfo(mockJti, userMock);
            assertTrue(true, "Exception silently bypassed in catch block; method returns properly;");
        } catch (Exception e) {
            fail("Unit Test thất bại vì kiến trúc hệ thống đã lọt lỗi ra tới controller.");
        }
    }

    @Test
    @DisplayName("UT_AS_011: [EP] Normal Logout Flow destroying session JWT reference.")
    void testRevokeToken_Success() {
        // Arrange
        String username = "nguyen_admin";

        // Act
        redisTokenService.revokeToken(username);

        // Assert: Invokes deleteById() precisely one time.
        verify(redisTokenRepository, times(1)).deleteById(username);
    }

    @Test
    @DisplayName("UT_AS_012: [BVA] Erase Non-Existent Key handling (ghost sessions).")
    void testRevokeToken_EmptyBoundary() {
        // Arrange: username = "" (Empty boundary). Spy/Mock to simulate empty keyspaces.
        doNothing().when(redisTokenRepository).deleteById("");

        // Act & Assert
        assertDoesNotThrow(() -> {
            redisTokenService.revokeToken("");
        }, "No operational failures occurring inside Cache abstraction. Exits cleanly via void.");
    }
}
