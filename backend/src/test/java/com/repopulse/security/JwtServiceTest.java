package com.repopulse.security;

import com.repopulse.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JwtService}.
 * No Spring context required – instantiated directly.
 */
class JwtServiceTest {

    private JwtService jwtService;

    // HS256 requires a Base64-encoded key of >= 256 bits (32 bytes)
    private static final String TEST_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        // Inject private fields via reflection (avoids full Spring context)
        setField(jwtService, "secretKey",        TEST_SECRET);
        setField(jwtService, "jwtExpiration",    86_400_000L); // 1 day
        setField(jwtService, "refreshExpiration", 604_800_000L); // 7 days

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashed")
                .role(User.Role.ROLE_USER)
                .build();
    }

    // ── Access token ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateAccessToken: should embed subject and be valid")
    void accessToken_validForSameUser() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("testuser");
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("isRefreshToken: access token should return false")
    void accessToken_isNotRefreshToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    // ── Refresh token ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateRefreshToken: should have token_type=refresh claim")
    void refreshToken_hasCorrectTypeClaim() {
        String token = jwtService.generateRefreshToken(testUser);

        assertThat(jwtService.isRefreshToken(token)).isTrue();
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("isRefreshToken: access token must not pass as refresh")
    void accessToken_mustNotPassAsRefreshToken() {
        String accessToken = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.isRefreshToken(accessToken)).isFalse();
    }

    // ── Expired token ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid: expired token should be invalid")
    void expiredToken_isInvalid() throws Exception {
        // Create a service with 1 ms expiry
        JwtService shortLived = new JwtService();
        setField(shortLived, "secretKey",        TEST_SECRET);
        setField(shortLived, "jwtExpiration",    1L);
        setField(shortLived, "refreshExpiration", 1L);

        String token = shortLived.generateAccessToken(testUser);
        Thread.sleep(5);

        // Must throw because the token is expired
        assertThatThrownBy(() -> shortLived.isTokenValid(token, testUser))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
