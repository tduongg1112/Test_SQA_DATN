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
    @DisplayName("UT_FR01_009: [EP] Lưu thông tin JWT thành công xuống Redis (Happy Path)")
    void testSaveTokenInfo_Success() {
        // Arrange
        String mockJti = "a2bd-48ff-992a";
        Account userMock = new Account();
        userMock.setUsername("admin_hanoi");
        userMock.setRole(Role.ADMIN);

        // Act
        redisTokenService.saveTokenInfo(mockJti, userMock);

        // Assert: Xác thực hàm đã gọi gỡ Session cũ và cắm Session mới thành công chưa
        verify(redisTokenRepository, times(1)).deleteById("admin_hanoi");
        verify(redisTokenRepository, times(1)).save(any(RedisTokenInfo.class));
    }

    @Test
    @DisplayName("UT_FR01_010: [EP] Phụ tải mạng chặn Crash: Mất kết nối DB Redis trong lúc Đăng Nhập")
    void testSaveTokenInfo_RedisConnectionFailure_SilentlyBypassed() {
        // Arrange
        String mockJti = "b689-10ee";
        Account userMock = new Account();
        userMock.setUsername("student_dn");

        // Giả lập Redis hỏng, Socket Timeout / Connection Refused
        doThrow(new RuntimeException("Redis Socket Timed Out"))
            .when(redisTokenRepository).save(any(RedisTokenInfo.class));

        // Act & Assert
        try {
            // Hệ thống thiết kế để NUỐT Exception (Swallow) bên trong khối Try-Catch và cho user đăng nhập bình thường
            redisTokenService.saveTokenInfo(mockJti, userMock);
            
            // Xùy ra Test Case này MÀU XANH (PASS). Mặc dù có lỗi cache, backend không ngất (Status 500) mà vẫn trả token JWT cho Frontend.
            assertTrue(true);
        } catch (Exception e) {
            fail("Unit Test thất bại vì kiến trúc hệ thống đã lọt lỗi ra tới controller.");
        }
    }

    @Test
    @DisplayName("UT_FR01_011: [EP] Thu hồi Session trên Redis (Chạy Logout Flow)")
    void testRevokeToken_Success() {
        // Act
        redisTokenService.revokeToken("target_student_session");

        // Assert
        verify(redisTokenRepository, times(1)).deleteById("target_student_session");
    }

    @Test
    @DisplayName("UT_FR01_012: [BVA] Rà lỗi khi yêu cầu thu hồi ID rỗng (Ghost Sessions)")
    void testRevokeToken_BoundaryEmptyValue_DoesNotCrash() {
        // Arrange (Test biên Rỗng, hoặc Username chưa bao giờ thiết lập Token)
        doNothing().when(redisTokenRepository).deleteById("");

        // Act
        assertDoesNotThrow(() -> {
            redisTokenService.revokeToken("");
        }, "Hành động cưỡng chế xoá Session ảo phải kết thúc an toàn, không được ném Lỗi Redis.");
    }
}
