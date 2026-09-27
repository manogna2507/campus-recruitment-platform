package com.manogna.recruitment.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // @Value fields aren't populated outside a Spring context, so set them directly.
        ReflectionTestUtils.setField(jwtUtil, "secret", "TestOnlySigningKeyThatIsLongEnoughForHS256!");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600000L); // 1 hour
    }

    @Test
    void generatesATokenThatValidatesAndRoundTripsClaims() {
        String token = jwtUtil.generateToken("student@example.com", "STUDENT");

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("student@example.com", jwtUtil.extractEmail(token));
        assertEquals("STUDENT", jwtUtil.extractRole(token));
    }

    @Test
    void rejectsAnExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1000L); // already expired the instant it's issued
        String token = jwtUtil.generateToken("student@example.com", "STUDENT");

        assertFalse(jwtUtil.isTokenValid(token));
    }

    @Test
    void rejectsAGarbageToken() {
        assertFalse(jwtUtil.isTokenValid("not-a-real-jwt"));
    }
}
