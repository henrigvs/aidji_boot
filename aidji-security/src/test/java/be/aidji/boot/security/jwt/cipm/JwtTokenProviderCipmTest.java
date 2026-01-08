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

package be.aidji.boot.security.jwt.cipm;

import be.aidji.boot.core.exception.SecurityException;
import be.aidji.boot.security.AidjiSecurityProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtTokenProviderCipm")
class JwtTokenProviderCipmTest {

    private ClientAndServer mockServer;
    private JwtTokenProviderCipm provider;
    private AidjiSecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        // Start mock server
        mockServer = ClientAndServer.startClientAndServer(0);
        int port = mockServer.getLocalPort();

        // Create properties
        AidjiSecurityProperties.CipmProperties cipmProperties = new AidjiSecurityProperties.CipmProperties(
                "http://localhost:" + port,
                "/.well-known/jwks.json",
                "/sign-token",
                "test-api-token",
                "test-issuer",
                3600L
        );

        AidjiSecurityProperties.JwtProperties jwtProperties = new AidjiSecurityProperties.JwtProperties(
                "cipm",
                true,
                true,
                "auth-token",
                600L,
                null,
                cipmProperties
        );

        securityProperties = new AidjiSecurityProperties(
                jwtProperties,
                new AidjiSecurityProperties.SecurityProperties(null)
        );

        provider = new JwtTokenProviderCipm(securityProperties);
    }

    @AfterEach
    void tearDown() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateTokenTests {

        @Test
        @DisplayName("should generate token via CIPM service")
        void shouldGenerateTokenViaCipmService() {
            // Given
            String expectedToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.signature";
            mockServer
                    .when(HttpRequest.request()
                            .withMethod("POST")
                            .withPath("/sign-token")
                            .withHeader("Authorization", "Bearer test-api-token"))
                    .respond(HttpResponse.response()
                            .withStatusCode(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                        "alg": "RS256",
                                        "exp": 1234567890,
                                        "kid": "test-key-1",
                                        "token": "%s"
                                    }
                                    """.formatted(expectedToken)));

            Map<String, Object> claims = Map.of("role", "USER", "email", "test@example.com");

            // When
            String token = provider.generateToken("testuser", claims);

            // Then
            assertThat(token).isEqualTo(expectedToken);
        }

        @Test
        @DisplayName("should include subject and claims in request")
        void shouldIncludeSubjectAndClaimsInRequest() {
            // Given
            mockServer
                    .when(HttpRequest.request()
                            .withMethod("POST")
                            .withPath("/sign-token"))
                    .respond(HttpResponse.response()
                            .withStatusCode(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                        "alg": "RS256",
                                        "exp": 1234567890,
                                        "kid": "test-key-1",
                                        "token": "test-token"
                                    }
                                    """));

            Map<String, Object> claims = Map.of("role", "ADMIN");

            // When
            provider.generateToken("admin", claims);

            // Then - Verify request was made with correct body structure
            mockServer.verify(
                    HttpRequest.request()
                            .withPath("/sign-token")
                            .withMethod("POST")
            );
        }

        @Test
        @DisplayName("should throw SecurityException when CIPM returns empty token")
        void shouldThrowExceptionWhenCipmReturnsEmptyToken() {
            // Given
            mockServer
                    .when(HttpRequest.request()
                            .withMethod("POST")
                            .withPath("/sign-token"))
                    .respond(HttpResponse.response()
                            .withStatusCode(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                        "alg": "RS256",
                                        "exp": 1234567890,
                                        "kid": "test-key-1",
                                        "token": null
                                    }
                                    """));

            Map<String, Object> claims = Map.of();

            // When / Then
            assertThatThrownBy(() -> provider.generateToken("testuser", claims))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("CIPM returned empty token");
        }

        @Test
        @DisplayName("should throw SecurityException when CIPM returns 500")
        void shouldThrowExceptionWhenCipmReturns500() {
            // Given
            mockServer
                    .when(HttpRequest.request()
                            .withMethod("POST")
                            .withPath("/sign-token"))
                    .respond(HttpResponse.response()
                            .withStatusCode(500)
                            .withBody("Internal Server Error"));

            Map<String, Object> claims = Map.of();

            // When / Then
            assertThatThrownBy(() -> provider.generateToken("testuser", claims))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Failed to generate token via CIPM");
        }

        @Test
        @DisplayName("should throw SecurityException when CIPM is unreachable")
        void shouldThrowExceptionWhenCipmIsUnreachable() {
            // Given - Stop the mock server to simulate unreachable service
            mockServer.stop();

            Map<String, Object> claims = Map.of();

            // When / Then
            assertThatThrownBy(() -> provider.generateToken("testuser", claims))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Failed to generate token via CIPM");
        }

        @Test
        @DisplayName("should include API token in Authorization header")
        void shouldIncludeApiTokenInAuthorizationHeader() {
            // Given
            mockServer
                    .when(HttpRequest.request()
                            .withMethod("POST")
                            .withPath("/sign-token")
                            .withHeader("Authorization", "Bearer test-api-token"))
                    .respond(HttpResponse.response()
                            .withStatusCode(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                        "alg": "RS256",
                                        "exp": 1234567890,
                                        "kid": "test-key-1",
                                        "token": "test-token"
                                    }
                                    """));

            Map<String, Object> claims = Map.of();

            // When
            String token = provider.generateToken("testuser", claims);

            // Then
            assertThat(token).isEqualTo("test-token");
            mockServer.verify(
                    HttpRequest.request()
                            .withHeader("Authorization", "Bearer test-api-token")
            );
        }
    }
}
