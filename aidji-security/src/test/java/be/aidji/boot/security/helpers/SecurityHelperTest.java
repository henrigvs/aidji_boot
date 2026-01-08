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

package be.aidji.boot.security.helpers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityHelper")
class SecurityHelperTest {

    @Nested
    @DisplayName("extractAuthorities")
    class ExtractAuthoritiesTests {

        @Test
        @DisplayName("should extract authorities from list of strings")
        void shouldExtractAuthoritiesFromListOfStrings() {
            // Given
            Claims claims = Jwts.claims()
                    .subject("testuser")
                    .add("authorities", List.of("ROLE_USER", "ROLE_ADMIN"))
                    .build();

            // When
            List<String> authorities = SecurityHelper.extractAuthorities(claims);

            // Then
            assertThat(authorities).containsExactly("ROLE_USER", "ROLE_ADMIN");
        }

        @Test
        @DisplayName("should return empty list when authorities claim is missing")
        void shouldReturnEmptyListWhenAuthoritiesClaimIsMissing() {
            // Given
            Claims claims = Jwts.claims()
                    .subject("testuser")
                    .build();

            // When
            List<String> authorities = SecurityHelper.extractAuthorities(claims);

            // Then
            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when authorities claim is empty list")
        void shouldReturnEmptyListWhenAuthoritiesClaimIsEmptyList() {
            // Given
            Claims claims = Jwts.claims()
                    .subject("testuser")
                    .add("authorities", List.of())
                    .build();

            // When
            List<String> authorities = SecurityHelper.extractAuthorities(claims);

            // Then
            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when authorities claim is not a list")
        void shouldReturnEmptyListWhenAuthoritiesClaimIsNotAList() {
            // Given
            Claims claims = Jwts.claims()
                    .subject("testuser")
                    .add("authorities", "ROLE_USER")
                    .build();

            // When
            List<String> authorities = SecurityHelper.extractAuthorities(claims);

            // Then
            assertThat(authorities).isEmpty();
        }
    }

    @Nested
    @DisplayName("extractAdditionalClaims")
    class ExtractAdditionalClaimsTests {

        @Test
        @DisplayName("should extract additional claims excluding standard claims")
        void shouldExtractAdditionalClaimsExcludingStandardClaims() {
            // Given
            Claims claims = Jwts.claims()
                    .subject("testuser")
                    .issuer("test-issuer")
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusSeconds(3600)))
                    .id("test-jti")
                    .add("authorities", List.of("ROLE_USER"))
                    .add("email", "test@example.com")
                    .add("department", "IT")
                    .add("customClaim", "customValue")
                    .build();

            // When
            Map<String, Object> additionalClaims = SecurityHelper.extractAdditionalClaims(claims);

            // Then
            assertThat(additionalClaims).hasSize(3);
            assertThat(additionalClaims).containsEntry("email", "test@example.com");
            assertThat(additionalClaims).containsEntry("department", "IT");
            assertThat(additionalClaims).containsEntry("customClaim", "customValue");
            assertThat(additionalClaims).doesNotContainKeys("sub", "iss", "iat", "exp", "jti", "authorities");
        }

        @Test
        @DisplayName("should return empty map when no additional claims")
        void shouldReturnEmptyMapWhenNoAdditionalClaims() {
            // Given
            Claims claims = Jwts.claims()
                    .subject("testuser")
                    .issuer("test-issuer")
                    .build();

            // When
            Map<String, Object> additionalClaims = SecurityHelper.extractAdditionalClaims(claims);

            // Then
            assertThat(additionalClaims).isEmpty();
        }

        @Test
        @DisplayName("should handle claims with only additional claims")
        void shouldHandleClaimsWithOnlyAdditionalClaims() {
            // Given
            Claims claims = Jwts.claims()
                    .add("customClaim1", "value1")
                    .add("customClaim2", "value2")
                    .build();

            // When
            Map<String, Object> additionalClaims = SecurityHelper.extractAdditionalClaims(claims);

            // Then
            assertThat(additionalClaims).hasSize(2);
            assertThat(additionalClaims).containsEntry("customClaim1", "value1");
            assertThat(additionalClaims).containsEntry("customClaim2", "value2");
        }
    }
}
