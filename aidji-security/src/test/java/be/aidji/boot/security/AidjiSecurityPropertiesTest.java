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

package be.aidji.boot.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AidjiSecurityProperties")
class AidjiSecurityPropertiesTest {

    @Nested
    @DisplayName("Root properties")
    class RootPropertiesTests {

        @Test
        @DisplayName("should apply default enabled=false when null")
        void shouldApplyDefaultEnabledFalse() {
            // Given / When
            var properties = new AidjiSecurityProperties(null, null, null);

            // Then
            assertThat(properties.enabled()).isFalse();
        }

        @Test
        @DisplayName("should keep provided enabled value")
        void shouldKeepProvidedEnabledValue() {
            // Given / When
            var properties = new AidjiSecurityProperties(true, null, null);

            // Then
            assertThat(properties.enabled()).isTrue();
        }

        @Test
        @DisplayName("should apply default public paths when null")
        void shouldApplyDefaultPublicPaths() {
            // Given / When
            var properties = new AidjiSecurityProperties(null, null, null);

            // Then
            assertThat(properties.publicPaths())
                    .containsExactly("/api/auth/**", "/actuator/health");
        }

        @Test
        @DisplayName("should keep provided public paths")
        void shouldKeepProvidedPublicPaths() {
            // Given
            var customPaths = List.of("/custom/**", "/public/**");

            // When
            var properties = new AidjiSecurityProperties(true, customPaths, null);

            // Then
            assertThat(properties.publicPaths()).isEqualTo(customPaths);
        }
    }

    @Nested
    @DisplayName("JwtProperties")
    class JwtPropertiesTests {

        @Test
        @DisplayName("should apply default cookie name when null")
        void shouldApplyDefaultCookieName() {
            // Given / When
            var jwtProps = new AidjiSecurityProperties.JwtProperties(
                    "cipm", true, true, null, null, null, null
            );

            // Then
            assertThat(jwtProps.cookieName()).isEqualTo("jwt-security-principal");
        }

        @Test
        @DisplayName("should apply default cookie name when blank")
        void shouldApplyDefaultCookieNameWhenBlank() {
            // Given / When
            var jwtProps = new AidjiSecurityProperties.JwtProperties(
                    "cipm", true, true, "  ", null, null, null
            );

            // Then
            assertThat(jwtProps.cookieName()).isEqualTo("jwt-security-principal");
        }

        @Test
        @DisplayName("should keep provided cookie name")
        void shouldKeepProvidedCookieName() {
            // Given / When
            var jwtProps = new AidjiSecurityProperties.JwtProperties(
                    "cipm", true, true, "custom-cookie", null, null, null
            );

            // Then
            assertThat(jwtProps.cookieName()).isEqualTo("custom-cookie");
        }

        @Test
        @DisplayName("should apply default maxAge=3600 when null")
        void shouldApplyDefaultMaxAge() {
            // Given / When
            var jwtProps = new AidjiSecurityProperties.JwtProperties(
                    "cipm", true, true, "jwt", null, null, null
            );

            // Then
            assertThat(jwtProps.maxAge()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("should keep provided maxAge")
        void shouldKeepProvidedMaxAge() {
            // Given / When
            var jwtProps = new AidjiSecurityProperties.JwtProperties(
                    "cipm", true, true, "jwt", 7200L, null, null
            );

            // Then
            assertThat(jwtProps.maxAge()).isEqualTo(7200L);
        }
    }

    @Nested
    @DisplayName("StandaloneProperties")
    class StandalonePropertiesTests {

        @Test
        @DisplayName("should apply default issuer when null")
        void shouldApplyDefaultIssuer() {
            // Given / When
            var standalone = new AidjiSecurityProperties.StandaloneProperties(
                    null, 0, null, null
            );

            // Then
            assertThat(standalone.issuer()).isEqualTo("aidji-boot-app");
        }

        @Test
        @DisplayName("should apply default issuer when blank")
        void shouldApplyDefaultIssuerWhenBlank() {
            // Given / When
            var standalone = new AidjiSecurityProperties.StandaloneProperties(
                    "  ", 0, null, null
            );

            // Then
            assertThat(standalone.issuer()).isEqualTo("aidji-boot-app");
        }

        @Test
        @DisplayName("should keep provided issuer")
        void shouldKeepProvidedIssuer() {
            // Given / When
            var standalone = new AidjiSecurityProperties.StandaloneProperties(
                    "my-custom-issuer", 0, null, null
            );

            // Then
            assertThat(standalone.issuer()).isEqualTo("my-custom-issuer");
        }

        @Test
        @DisplayName("should apply default keySize=2048 when zero or negative")
        void shouldApplyDefaultKeySize() {
            // Given / When
            var standalone1 = new AidjiSecurityProperties.StandaloneProperties(
                    "issuer", 0, null, null
            );
            var standalone2 = new AidjiSecurityProperties.StandaloneProperties(
                    "issuer", -1, null, null
            );

            // Then
            assertThat(standalone1.keySize()).isEqualTo(2048);
            assertThat(standalone2.keySize()).isEqualTo(2048);
        }

        @Test
        @DisplayName("should keep provided keySize")
        void shouldKeepProvidedKeySize() {
            // Given / When
            var standalone = new AidjiSecurityProperties.StandaloneProperties(
                    "issuer", 4096, null, null
            );

            // Then
            assertThat(standalone.keySize()).isEqualTo(4096);
        }

        @Test
        @DisplayName("hasKeys should return false when keys are null")
        void hasKeysShouldReturnFalseWhenNull() {
            // Given
            var standalone = new AidjiSecurityProperties.StandaloneProperties(
                    "issuer", 2048, null, null
            );

            // When / Then
            assertThat(standalone.hasKeys()).isFalse();
        }

        @Test
        @DisplayName("hasKeys should return false when keys are blank")
        void hasKeysShouldReturnFalseWhenBlank() {
            // Given
            var standalone = new AidjiSecurityProperties.StandaloneProperties(
                    "issuer", 2048, "  ", "  "
            );

            // When / Then
            assertThat(standalone.hasKeys()).isFalse();
        }

        @Test
        @DisplayName("hasKeys should return false when only private key is provided")
        void hasKeysShouldReturnFalseWhenOnlyPrivateKey() {
            // Given
            var standalone = new AidjiSecurityProperties.StandaloneProperties(
                    "issuer", 2048, "private-key-content", null
            );

            // When / Then
            assertThat(standalone.hasKeys()).isFalse();
        }

        @Test
        @DisplayName("hasKeys should return false when only public key is provided")
        void hasKeysShouldReturnFalseWhenOnlyPublicKey() {
            // Given
            var standalone = new AidjiSecurityProperties.StandaloneProperties(
                    "issuer", 2048, null, "public-key-content"
            );

            // When / Then
            assertThat(standalone.hasKeys()).isFalse();
        }

        @Test
        @DisplayName("hasKeys should return true when both keys are provided")
        void hasKeysShouldReturnTrueWhenBothKeysProvided() {
            // Given
            var standalone = new AidjiSecurityProperties.StandaloneProperties(
                    "issuer", 2048, "private-key-content", "public-key-content"
            );

            // When / Then
            assertThat(standalone.hasKeys()).isTrue();
        }
    }

    @Nested
    @DisplayName("CipmProperties")
    class CipmPropertiesTests {

        @Test
        @DisplayName("should apply default jwksCacheTtlSeconds=3600 when zero or negative")
        void shouldApplyDefaultCacheTtl() {
            // Given / When
            var cipm1 = new AidjiSecurityProperties.CipmProperties(
                    "https://cipm.example.com", "/jwks", "/sign", "token", "issuer", 0
            );
            var cipm2 = new AidjiSecurityProperties.CipmProperties(
                    "https://cipm.example.com", "/jwks", "/sign", "token", "issuer", -1
            );

            // Then
            assertThat(cipm1.jwksCacheTtlSeconds()).isEqualTo(3600L);
            assertThat(cipm2.jwksCacheTtlSeconds()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("should keep provided jwksCacheTtlSeconds")
        void shouldKeepProvidedCacheTtl() {
            // Given / When
            var cipm = new AidjiSecurityProperties.CipmProperties(
                    "https://cipm.example.com", "/jwks", "/sign", "token", "issuer", 7200
            );

            // Then
            assertThat(cipm.jwksCacheTtlSeconds()).isEqualTo(7200L);
        }

        @Test
        @DisplayName("getPublicKeyUrl should concatenate baseUrl and publicKeyUri")
        void getPublicKeyUrlShouldConcatenate() {
            // Given
            var cipm = new AidjiSecurityProperties.CipmProperties(
                    "https://cipm.example.com", "/.well-known/jwks.json",
                    "/api/sign", "token", "issuer", 3600
            );

            // When
            var url = cipm.getPublicKeyUrl();

            // Then
            assertThat(url).isEqualTo("https://cipm.example.com/.well-known/jwks.json");
        }

        @Test
        @DisplayName("getSignTokenUrl should concatenate baseUrl and signTokenUri")
        void getSignTokenUrlShouldConcatenate() {
            // Given
            var cipm = new AidjiSecurityProperties.CipmProperties(
                    "https://cipm.example.com", "/.well-known/jwks.json",
                    "/api/sign-token", "token", "issuer", 3600
            );

            // When
            var url = cipm.getSignTokenUrl();

            // Then
            assertThat(url).isEqualTo("https://cipm.example.com/api/sign-token");
        }
    }
}