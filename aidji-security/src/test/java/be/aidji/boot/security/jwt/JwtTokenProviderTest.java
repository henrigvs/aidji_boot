package be.aidji.boot.security.jwt;

import be.aidji.boot.core.exception.SecurityErrorCode;
import be.aidji.boot.core.exception.SecurityException;
import be.aidji.boot.security.AidjiSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET_KEY = Base64.getEncoder().encodeToString(
            "my-super-secret-key-for-testing-purposes-only-32bytes!".getBytes()
    );
    private static final long VALIDITY_DURATION_MS = 3600000; // 1 hour

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        var jwtProperties = new AidjiSecurityProperties.JsonWebTokenProperties(
                SECRET_KEY,
                VALIDITY_DURATION_MS,
                true,
                null,
                null
        );
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);

        testUser = User.builder()
                .username("john.doe@example.com")
                .password("password")
                .authorities(
                        new SimpleGrantedAuthority("USER"),
                        new SimpleGrantedAuthority("ADMIN")
                )
                .build();
    }

    // ========== Token Generation Tests ==========

    @Nested
    @DisplayName("generateToken")
    class GenerateTokenTests {

        @Test
        @DisplayName("should generate valid token with user details")
        void shouldGenerateValidToken() {
            String token = jwtTokenProvider.generateToken(testUser);

            assertThat(token)
                    .isNotNull()
                    .isNotBlank()
                    .contains("."); // JWT format: header.payload.signature
        }

        @Test
        @DisplayName("should include username as subject")
        void shouldIncludeUsernameAsSubject() {
            String token = jwtTokenProvider.generateToken(testUser);

            String username = jwtTokenProvider.extractUsername(token);

            assertThat(username).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("should include authorities in token")
        void shouldIncludeAuthorities() {
            String token = jwtTokenProvider.generateToken(testUser);

            Claims claims = jwtTokenProvider.extractAllClaims(token);
            List<String> authorities = claims.get("authorities", List.class);

            assertThat(authorities)
                    .containsExactlyInAnyOrder("USER", "ADMIN");
        }

        @Test
        @DisplayName("should include custom claims when provided")
        void shouldIncludeCustomClaims() {
            Map<String, Object> customClaims = Map.of(
                    "tenantId", "tenant-123",
                    "sessionId", "session-456"
            );

            String token = jwtTokenProvider.generateToken(testUser, customClaims);

            Claims claims = jwtTokenProvider.extractAllClaims(token);
            assertThat(claims.get("tenantId", String.class)).isEqualTo("tenant-123");
            assertThat(claims.get("sessionId", String.class)).isEqualTo("session-456");
        }

        @Test
        @DisplayName("should set correct expiration time")
        void shouldSetCorrectExpiration() {
            long beforeGeneration = System.currentTimeMillis();

            String token = jwtTokenProvider.generateToken(testUser);

            Date expiration = jwtTokenProvider.extractExpiration(token);
            long expectedExpiration = beforeGeneration + VALIDITY_DURATION_MS;

            // Allow 1-second tolerance
            assertThat(expiration.getTime())
                    .isCloseTo(expectedExpiration, within(1000L));
        }

        @Test
        @DisplayName("should handle null custom claims")
        void shouldHandleNullCustomClaims() {
            String token = jwtTokenProvider.generateToken(testUser, null);

            assertThat(token).isNotNull();
            assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should handle empty custom claims")
        void shouldHandleEmptyCustomClaims() {
            String token = jwtTokenProvider.generateToken(testUser, Map.of());

            assertThat(token).isNotNull();
            assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
        }
    }

    // ========== Token Validation Tests ==========

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValidTests {

        @Test
        @DisplayName("should return true for valid token")
        void shouldReturnTrueForValidToken() {
            String token = jwtTokenProvider.generateToken(testUser);

            assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should return true when token matches user")
        void shouldReturnTrueWhenTokenMatchesUser() {
            String token = jwtTokenProvider.generateToken(testUser);

            assertThat(jwtTokenProvider.isTokenValid(token, testUser)).isTrue();
        }

        @Test
        @DisplayName("should return false when token does not match user")
        void shouldReturnFalseWhenTokenDoesNotMatchUser() {
            String token = jwtTokenProvider.generateToken(testUser);

            UserDetails otherUser = User.builder()
                    .username("other@example.com")
                    .password("password")
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                    .build();

            assertThat(jwtTokenProvider.isTokenValid(token, otherUser)).isFalse();
        }

        @Test
        @DisplayName("should throw SecurityException for expired token")
        void shouldThrowExceptionForExpiredToken() {
            String expiredToken = jwtTokenProvider.generateExpiredToken();

            assertThatThrownBy(() -> jwtTokenProvider.isTokenValid(expiredToken))
                    .isInstanceOf(SecurityException.class)
                    .satisfies(ex -> {
                        SecurityException se = (SecurityException) ex;
                        assertThat(se.getErrorCode()).isEqualTo(SecurityErrorCode.BEARER_TOKEN_EXPIRED);
                    });
        }

        @Test
        @DisplayName("should throw SecurityException for malformed token")
        void shouldThrowExceptionForMalformedToken() {
            String malformedToken = "not.a.valid.jwt.token";

            assertThatThrownBy(() -> jwtTokenProvider.isTokenValid(malformedToken))
                    .isInstanceOf(SecurityException.class)
                    .satisfies(ex -> {
                        SecurityException se = (SecurityException) ex;
                        assertThat(se.getErrorCode()).isEqualTo(SecurityErrorCode.BEARER_TOKEN_NOT_VALID);
                    });
        }

        @Test
        @DisplayName("should throw SecurityException for token with invalid signature")
        void shouldThrowExceptionForInvalidSignature() {
            String token = jwtTokenProvider.generateToken(testUser);
            String tamperedToken = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsignature";

            assertThatThrownBy(() -> jwtTokenProvider.isTokenValid(tamperedToken))
                    .isInstanceOf(SecurityException.class)
                    .satisfies(ex -> {
                        SecurityException se = (SecurityException) ex;
                        assertThat(se.getErrorCode()).isEqualTo(SecurityErrorCode.BEARER_TOKEN_NOT_VALID);
                    });
        }

        @Test
        @DisplayName("should throw SecurityException for token signed with different key")
        void shouldThrowExceptionForWrongKey() {
            // Generate a token with a different key
            String differentKey = Base64.getEncoder().encodeToString(
                    "another-secret-key-for-testing-32bytes!!".getBytes()
            );
            SecretKey wrongKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(differentKey));

            String tokenWithWrongKey = Jwts.builder()
                    .subject(testUser.getUsername())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + VALIDITY_DURATION_MS))
                    .signWith(wrongKey)
                    .compact();

            assertThatThrownBy(() -> jwtTokenProvider.isTokenValid(tokenWithWrongKey))
                    .isInstanceOf(SecurityException.class)
                    .satisfies(ex -> {
                        SecurityException se = (SecurityException) ex;
                        assertThat(se.getErrorCode()).isEqualTo(SecurityErrorCode.BEARER_TOKEN_NOT_VALID);
                    });
        }
    }

    // ========== Token Expiration Tests ==========

    @Nested
    @DisplayName("isTokenExpired")
    class IsTokenExpiredTests {

        @Test
        @DisplayName("should return false for valid non-expired token")
        void shouldReturnFalseForValidToken() {
            String token = jwtTokenProvider.generateToken(testUser);

            assertThat(jwtTokenProvider.isTokenExpired(token)).isFalse();
        }

        @Test
        @DisplayName("should return true for expired token")
        void shouldReturnTrueForExpiredToken() {
            String expiredToken = jwtTokenProvider.generateExpiredToken();

            assertThat(jwtTokenProvider.isTokenExpired(expiredToken)).isTrue();
        }

        @Test
        @DisplayName("should return true for malformed token")
        void shouldReturnTrueForMalformedToken() {
            assertThat(jwtTokenProvider.isTokenExpired("invalid.token")).isTrue();
        }
    }

    // ========== Expired Token Generation Tests ==========

    @Nested
    @DisplayName("generateExpiredToken")
    class GenerateExpiredTokenTests {

        @Test
        @DisplayName("should generate token that is already expired")
        void shouldGenerateExpiredToken() {
            String token = jwtTokenProvider.generateExpiredToken();

            assertThat(jwtTokenProvider.isTokenExpired(token)).isTrue();
        }

        @Test
        @DisplayName("should generate valid JWT format")
        void shouldGenerateValidJwtFormat() {
            String token = jwtTokenProvider.generateExpiredToken();

            assertThat(token)
                    .isNotNull()
                    .contains(".");
        }
    }

    // ========== Claim Extraction Tests ==========

    @Nested
    @DisplayName("extractClaim")
    class ExtractClaimTests {

        @Test
        @DisplayName("should extract username")
        void shouldExtractUsername() {
            String token = jwtTokenProvider.generateToken(testUser);

            String username = jwtTokenProvider.extractUsername(token);

            assertThat(username).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("should extract expiration date")
        void shouldExtractExpiration() {
            String token = jwtTokenProvider.generateToken(testUser);

            Date expiration = jwtTokenProvider.extractExpiration(token);

            assertThat(expiration).isAfter(new Date());
        }

        @Test
        @DisplayName("should extract custom claim")
        void shouldExtractCustomClaim() {
            Map<String, Object> customClaims = Map.of("userId", 12345);
            String token = jwtTokenProvider.generateToken(testUser, customClaims);

            Integer userId = jwtTokenProvider.extractClaim(token, claims -> claims.get("userId", Integer.class));

            assertThat(userId).isEqualTo(12345);
        }

        @Test
        @DisplayName("should throw SecurityException for invalid token")
        void shouldThrowExceptionForInvalidToken() {
            assertThatThrownBy(() -> jwtTokenProvider.extractUsername("invalid"))
                    .isInstanceOf(SecurityException.class);
        }
    }

    // ========== Edge Cases ==========

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle user with no authorities")
        void shouldHandleUserWithNoAuthorities() {
            UserDetails userWithNoAuthorities = User.builder()
                    .username("noauth@example.com")
                    .password("password")
                    .authorities(List.of())
                    .build();

            String token = jwtTokenProvider.generateToken(userWithNoAuthorities);

            assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();

            Claims claims = jwtTokenProvider.extractAllClaims(token);
            List<String> authorities = claims.get("authorities", List.class);
            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("should handle user with special characters in username")
        void shouldHandleSpecialCharactersInUsername() {
            UserDetails userWithSpecialChars = User.builder()
                    .username("user+special@example.com")
                    .password("password")
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                    .build();

            String token = jwtTokenProvider.generateToken(userWithSpecialChars);

            String extractedUsername = jwtTokenProvider.extractUsername(token);
            assertThat(extractedUsername).isEqualTo("user+special@example.com");
        }

        @Test
        @DisplayName("should handle empty string token")
        void shouldHandleEmptyToken() {
            assertThatThrownBy(() -> jwtTokenProvider.extractAllClaims(""))
                    .isInstanceOf(SecurityException.class);
        }
    }
}