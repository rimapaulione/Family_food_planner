package org.example.planner_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-for-jwt-signing-must-be-at-least-32-bytes";
    private static final String OTHER_SECRET = "another-secret-key-for-jwt-signing-also-32-bytes-long-yes";
    private static final long EXPIRY_MINUTES = 60;
    private static final String EMAIL = "user@example.com";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRY_MINUTES);
    }

    @Test
    void generateToken_shouldProduceTokenContainingEmail() {
        String token = jwtService.generateToken(EMAIL);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo(EMAIL);
    }

    @Test
    void isValid_shouldReturnTrueForFreshToken() {
        String token = jwtService.generateToken(EMAIL);

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_shouldReturnFalseForMalformedToken() {
        assertThat(jwtService.isValid("not.a.jwt")).isFalse();
    }

    @Test
    void isValid_shouldReturnFalseForTokenSignedWithDifferentKey() {
        JwtService otherService = new JwtService(OTHER_SECRET, EXPIRY_MINUTES);
        String foreignToken = otherService.generateToken(EMAIL);

        assertThat(jwtService.isValid(foreignToken)).isFalse();
    }

    @Test
    void isValid_shouldReturnFalseForExpiredToken() {
        JwtService expiredService = new JwtService(SECRET, -1);
        String expiredToken = expiredService.generateToken(EMAIL);

        assertThat(jwtService.isValid(expiredToken)).isFalse();
    }

    @Test
    void isValid_shouldReturnFalseForNullOrEmptyToken() {
        assertThat(jwtService.isValid(null)).isFalse();
        assertThat(jwtService.isValid("")).isFalse();
    }
}
