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

package be.aidji.boot.security.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtClaims")
class JwtClaimsTest {

    @Nested
    @DisplayName("isExpired")
    class IsExpiredTests {

        @Test
        @DisplayName("should return false for non-expired token")
        void shouldReturnFalseForNonExpiredToken() {
            // Given
            JwtClaims claims = JwtClaims.builder()
                    .jti("test-jti")
                    .subject("testuser")
                    .issuer("test-issuer")
                    .issuedAt(Instant.now())
                    .expiration(Instant.now().plusSeconds(3600))
                    .authorities(List.of("ROLE_USER"))
                    .additionalClaims(Map.of())
                    .build();

            // When
            boolean expired = claims.isExpired();

            // Then
            assertThat(expired).isFalse();
        }

        @Test
        @DisplayName("should return true for expired token")
        void shouldReturnTrueForExpiredToken() {
            // Given
            JwtClaims claims = JwtClaims.builder()
                    .jti("test-jti")
                    .subject("testuser")
                    .issuer("test-issuer")
                    .issuedAt(Instant.now().minusSeconds(7200))
                    .expiration(Instant.now().minusSeconds(3600))
                    .authorities(List.of("ROLE_USER"))
                    .additionalClaims(Map.of())
                    .build();

            // When
            boolean expired = claims.isExpired();

            // Then
            assertThat(expired).isTrue();
        }

        @Test
        @DisplayName("should return true for token expiring exactly now")
        void shouldReturnTrueForTokenExpiringExactlyNow() {
            // Given - Token expiring 1 second ago to account for test execution time
            JwtClaims claims = JwtClaims.builder()
                    .jti("test-jti")
                    .subject("testuser")
                    .issuer("test-issuer")
                    .issuedAt(Instant.now().minusSeconds(3600))
                    .expiration(Instant.now().minusSeconds(1))
                    .authorities(List.of("ROLE_USER"))
                    .additionalClaims(Map.of())
                    .build();

            // When
            boolean expired = claims.isExpired();

            // Then
            assertThat(expired).isTrue();
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should build complete JwtClaims")
        void shouldBuildCompleteJwtClaims() {
            // Given
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(3600);
            List<String> authorities = List.of("ROLE_USER", "ROLE_ADMIN");
            Map<String, Object> additionalClaims = Map.of("email", "test@example.com", "department", "IT");

            // When
            JwtClaims claims = JwtClaims.builder()
                    .jti("test-jti")
                    .subject("testuser")
                    .issuer("test-issuer")
                    .issuedAt(now)
                    .expiration(expiration)
                    .authorities(authorities)
                    .additionalClaims(additionalClaims)
                    .build();

            // Then
            assertThat(claims.jti()).isEqualTo("test-jti");
            assertThat(claims.subject()).isEqualTo("testuser");
            assertThat(claims.issuer()).isEqualTo("test-issuer");
            assertThat(claims.issuedAt()).isEqualTo(now);
            assertThat(claims.expiration()).isEqualTo(expiration);
            assertThat(claims.authorities()).isEqualTo(authorities);
            assertThat(claims.additionalClaims()).isEqualTo(additionalClaims);
        }

        @Test
        @DisplayName("should build minimal JwtClaims")
        void shouldBuildMinimalJwtClaims() {
            // When
            JwtClaims claims = JwtClaims.builder()
                    .subject("testuser")
                    .expiration(Instant.now().plusSeconds(3600))
                    .build();

            // Then
            assertThat(claims.subject()).isEqualTo("testuser");
            assertThat(claims.expiration()).isNotNull();
        }

        @Test
        @DisplayName("should support toBuilder for immutable updates")
        void shouldSupportToBuilderForImmutableUpdates() {
            // Given
            JwtClaims original = JwtClaims.builder()
                    .jti("test-jti")
                    .subject("testuser")
                    .issuer("test-issuer")
                    .issuedAt(Instant.now())
                    .expiration(Instant.now().plusSeconds(3600))
                    .authorities(List.of("ROLE_USER"))
                    .additionalClaims(Map.of())
                    .build();

            // When
            JwtClaims updated = original.toBuilder()
                    .authorities(List.of("ROLE_USER", "ROLE_ADMIN"))
                    .build();

            // Then
            assertThat(original.authorities()).hasSize(1);
            assertThat(updated.authorities()).hasSize(2);
            assertThat(updated.subject()).isEqualTo(original.subject());
            assertThat(updated.issuer()).isEqualTo(original.issuer());
        }
    }
}
