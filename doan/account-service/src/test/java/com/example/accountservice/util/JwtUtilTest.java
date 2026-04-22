package com.example.accountservice.util;

import com.example.accountservice.enums.Role;
import com.example.accountservice.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "mySecretKeyForJwtTokenThatShouldBeAtLeast256BitsLongToEnsureSecurityAndProperFunctioning";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Inject values into @Value fields manually because we are not using Spring context for this unit test
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    @DisplayName("UT_JWT_001: Generate and validate token successfully")
    void testGenerateAndValidateToken() {
        Account account = new Account();
        account.setUsername("testuser");
        account.setRole(Role.STUDENT);

        String token = jwtUtil.generateToken(account);
        assertNotNull(token);

        String username = jwtUtil.getUsernameFromToken(token);
        assertEquals("testuser", username);

        // Check JWT ID extraction
        String jti = jwtUtil.getJwtIdFromToken(token);
        assertNotNull(jti);
    }

    @Test
    @DisplayName("UT_JWT_002: Extract domain from various URL formats")
    void testExtractDomain() {
        // Standard formats
        assertEquals("google.com", jwtUtil.extractDomain("https://google.com/search"));
        assertEquals("fpt.edu.vn", jwtUtil.extractDomain("http://www.fpt.edu.vn/home"));
        
        // Missing protocol
        assertEquals("example.org", jwtUtil.extractDomain("example.org/path"));
        
        // Subdomains
        assertEquals("sub.domain.com", jwtUtil.extractDomain("https://sub.domain.com/api"));
    }

    @Test
    @DisplayName("UT_JWT_003: Extract domain with invalid or edge case inputs")
    void testExtractDomain_EdgeCases() {
        assertNull(jwtUtil.extractDomain(null));
        assertNull(jwtUtil.extractDomain(""));
        
        // Invalid URI characters
        assertNull(jwtUtil.extractDomain("http://invalid^host.com"));
    }
}
