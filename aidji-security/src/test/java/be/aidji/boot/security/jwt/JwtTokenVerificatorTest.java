/*
 * Copyright 2025 Henri GEVENOIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.aidji.boot.security.jwt;

import be.aidji.boot.core.exception.SecurityException;
import be.aidji.boot.core.exception.TechnicalException;
import be.aidji.boot.security.AidjiSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtTokenVerificator")
class JwtTokenVerificatorTest {

    private ClientAndServer mockServer;
    private KeyPair keyPair;
    private final String kid = "test-key-1";
    private JwtTokenVerificator verificator;

    @BeforeEach
    void setUp() throws Exception {
        // Start mock server
        mockServer = ClientAndServer.startClientAndServer(0);
        int port = mockServer.getLocalPort();

        // Generate RSA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();

        // Setup JWKS endpoint
        String jwksJson = buildJwksResponse(keyPair.getPublic(), kid);
        mockServer
                .when(HttpRequest.request().withPath("/.well-known/jwks.json"))
                .respond(HttpResponse.response().withStatusCode(200).withBody(jwksJson));

        // Create verificator
        AidjiSecurityProperties.JwtProperties jwtProperties = new AidjiSecurityProperties.JwtProperties(
                "http://localhost:" + port + "/.well-known/jwks.json",
                3600L,
                true,
                "auth-token"
        );

        verificator = new JwtTokenVerificator(jwtProperties);
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateTokenTests {

        @Test
        @DisplayName("should validate valid token and return claims")
        void shouldValidateValidToken() {
            // Given
            String token = buildToken(keyPair.getPrivate(), kid, "testuser", Map.of("role", "USER"));

            // When
            Claims claims = verificator.validateToken(token);

            // Then
            assertThat(claims).isNotNull();
            assertThat(claims.getSubject()).isEqualTo("testuser");
            assertThat(claims.get("role")).isEqualTo("USER");
        }

        @Test
        @DisplayName("should throw SecurityException for expired token")
        void shouldThrowExceptionForExpiredToken() {
            // Given
            String token = buildExpiredToken(keyPair.getPrivate());

            // When / Then
            assertThatThrownBy(() -> verificator.validateToken(token))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Token expired");
        }

        @Test
        @DisplayName("should throw SecurityException for token with invalid signature")
        void shouldThrowExceptionForInvalidSignature() throws Exception {
            // Given - token signed with different key
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair wrongKeyPair = keyGen.generateKeyPair();
            String token = buildToken(wrongKeyPair.getPrivate(), kid, "testuser", Map.of());

            // When / Then
            assertThatThrownBy(() -> verificator.validateToken(token))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Invalid token");
        }

        @Test
        @DisplayName("should throw SecurityException for token without kid")
        void shouldThrowExceptionForTokenWithoutKid() {
            // Given - token without kid in header
            String token = Jwts.builder()
                    .subject("testuser")
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusSeconds(3600)))
                    .signWith(keyPair.getPrivate())
                    .compact();

            // When / Then
            assertThatThrownBy(() -> verificator.validateToken(token))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("kid");
        }

        @Test
        @DisplayName("should throw SecurityException for unknown kid")
        void shouldThrowExceptionForUnknownKid() {
            // Given
            String token = buildToken(keyPair.getPrivate(), "unknown-kid", "testuser", Map.of());

            // When / Then
            assertThatThrownBy(() -> verificator.validateToken(token))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Unknown key ID");
        }

        @Test
        @DisplayName("should throw SecurityException for malformed token")
        void shouldThrowExceptionForMalformedToken() {
            // Given
            String token = "not.a.valid.jwt.token";

            // When / Then
            assertThatThrownBy(() -> verificator.validateToken(token))
                    .isInstanceOf(SecurityException.class);
        }
    }

    @Nested
    @DisplayName("isValid")
    class IsValidTests {

        @Test
        @DisplayName("should return true for valid token")
        void shouldReturnTrueForValidToken() {
            // Given
            String token = buildToken(keyPair.getPrivate(), kid, "testuser", Map.of());

            // When
            boolean valid = verificator.isValid(token);

            // Then
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("should return false for expired token")
        void shouldReturnFalseForExpiredToken() {
            // Given
            String token = buildExpiredToken(keyPair.getPrivate());

            // When
            boolean valid = verificator.isValid(token);

            // Then
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("should return false for malformed token")
        void shouldReturnFalseForMalformedToken() {
            // Given
            String token = "invalid-token";

            // When
            boolean valid = verificator.isValid(token);

            // Then
            assertThat(valid).isFalse();
        }
    }

    @Nested
    @DisplayName("Key Caching")
    class KeyCachingTests {

        @Test
        @DisplayName("should cache public keys and reuse them")
        void shouldCachePublicKeys() {
            // Given
            String token1 = buildToken(keyPair.getPrivate(), kid, "user1", Map.of());
            String token2 = buildToken(keyPair.getPrivate(), kid, "user2", Map.of());

            // When
            verificator.validateToken(token1);
            mockServer.reset(); // Clear mock server - if cache doesn't work, next call will fail
            Claims claims2 = verificator.validateToken(token2);

            // Then - should still work because key is cached
            assertThat(claims2.getSubject()).isEqualTo("user2");
        }

        @Test
        @DisplayName("should refresh cache when encountering unknown kid")
        void shouldRefreshCacheForUnknownKid() throws Exception {
            // Given - validate first token to populate cache
            String token1 = buildToken(keyPair.getPrivate(), kid, "user1", Map.of());
            verificator.validateToken(token1);

            // Generate new key pair for key rotation scenario
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair newKeyPair = keyGen.generateKeyPair();
            String newKid = "test-key-2";

            // Update mock server with new JWKS including both keys
            String newJwksJson = buildJwksResponseWithMultipleKeys(
                    List.of(
                            new KeyInfo(keyPair.getPublic(), kid),
                            new KeyInfo(newKeyPair.getPublic(), newKid)
                    )
            );
            mockServer.reset();
            mockServer
                    .when(HttpRequest.request().withPath("/.well-known/jwks.json"))
                    .respond(HttpResponse.response().withStatusCode(200).withBody(newJwksJson));

            // When - validate token with new kid
            String token2 = buildToken(newKeyPair.getPrivate(), newKid, "user2", Map.of());
            Claims claims2 = verificator.validateToken(token2);

            // Then - should successfully validate after cache refresh
            assertThat(claims2.getSubject()).isEqualTo("user2");
        }
    }

    @Nested
    @DisplayName("JWKS Fetching")
    class JwksFetchingTests {

        @Test
        @DisplayName("should throw TechnicalException when JWKS endpoint returns 404")
        void shouldThrowExceptionForJwks404() {
            // Given
            mockServer.reset();
            mockServer
                    .when(HttpRequest.request().withPath("/.well-known/jwks.json"))
                    .respond(HttpResponse.response().withStatusCode(404));

            String token = buildToken(keyPair.getPrivate(), kid, "testuser", Map.of());

            // When / Then
            assertThatThrownBy(() -> verificator.validateToken(token))
                    .isInstanceOf(TechnicalException.class)
                    .hasMessageContaining("Failed to fetch JWKS");
        }

        @Test
        @DisplayName("should throw TechnicalException when JWKS endpoint returns 500")
        void shouldThrowExceptionForJwks500() {
            // Given
            mockServer.reset();
            mockServer
                    .when(HttpRequest.request().withPath("/.well-known/jwks.json"))
                    .respond(HttpResponse.response().withStatusCode(500));

            String token = buildToken(keyPair.getPrivate(), kid, "testuser", Map.of());

            // When / Then
            assertThatThrownBy(() -> verificator.validateToken(token))
                    .isInstanceOf(TechnicalException.class)
                    .hasMessageContaining("Failed to fetch JWKS");
        }
    }

    // ========== Helper Methods ==========

    private String buildToken(PrivateKey privateKey, String kid, String subject, Map<String, Object> claims) {
        return Jwts.builder()
                .header()
                .keyId(kid)
                .and()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private String buildExpiredToken(PrivateKey privateKey) {
        return Jwts.builder()
                .header()
                .keyId("test-key-1")
                .and()
                .subject("testuser")
                .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .expiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private String buildJwksResponse(PublicKey publicKey, String kid) {
        return buildJwksResponseWithMultipleKeys(List.of(new KeyInfo(publicKey, kid)));
    }

    private String buildJwksResponseWithMultipleKeys(List<KeyInfo> keys) {
        StringBuilder keysJson = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) keysJson.append(",");
            KeyInfo keyInfo = keys.get(i);
            keysJson.append(buildJwkJson(keyInfo.publicKey(), keyInfo.kid()));
        }

        return """
                {
                  "keys": [
                    %s
                  ]
                }
                """.formatted(keysJson.toString());
    }

    private String buildJwkJson(PublicKey publicKey, String kid) {
        java.security.interfaces.RSAPublicKey rsaPublicKey =
                (java.security.interfaces.RSAPublicKey) publicKey;

        String n = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rsaPublicKey.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rsaPublicKey.getPublicExponent().toByteArray());

        return """
                {
                  "kty": "RSA",
                  "use": "sig",
                  "kid": "%s",
                  "alg": "RS256",
                  "n": "%s",
                  "e": "%s"
                }
                """.formatted(kid, n, e);
    }

    private record KeyInfo(PublicKey publicKey, String kid) {
    }
}
