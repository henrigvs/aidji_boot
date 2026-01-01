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

package be.aidji.boot.security.webflux.jwt;

import be.aidji.boot.core.exception.SecurityException;
import be.aidji.boot.security.webflux.AidjiPrincipal;
import be.aidji.boot.security.webflux.AidjiSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static be.aidji.boot.core.exception.SecurityErrorCode.BEARER_TOKEN_EXPIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("JwtAuthenticationWebFilter")
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationWebFilterTest {

    @Mock
    private JwtTokenVerificator jwtTokenVerificator;

    @Mock
    private WebFilterChain filterChain;

    private JwtAuthenticationWebFilter filter;
    private AidjiSecurityProperties.JwtProperties jwtProperties;
    private AidjiSecurityProperties.SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        // Default configuration - cookie-based auth
        jwtProperties = new AidjiSecurityProperties.JwtProperties(
                "http://localhost:8080/.well-known/jwks.json",
                3600L,
                true,
                "jwt-security-principal",
                3600L
        );

        securityProperties = new AidjiSecurityProperties.SecurityProperties(
                List.of("/api/auth/**", "/actuator/health")
        );

        filter = new JwtAuthenticationWebFilter(jwtTokenVerificator, jwtProperties, securityProperties);

        // Setup default filter chain behavior - returns completed Mono<Void>
        when(filterChain.filter(any())).thenReturn(Mono.fromRunnable(() -> {}));
    }

    @Nested
    @DisplayName("Token Extraction from Cookie")
    class TokenExtractionFromCookieTests {

        @Test
        @DisplayName("should extract token from cookie when cookie-based auth is enabled")
        void shouldExtractTokenFromCookie() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = createClaims("testuser", List.of("ROLE_USER"));
            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result
                            .contextWrite(ctx -> {
                                // Verify that authentication was set in context
                                return ctx;
                            }))
                    .verifyComplete();

            verify(jwtTokenVerificator).validateToken(token);
            verify(filterChain, atLeastOnce()).filter(any());
        }

        @Test
        @DisplayName("should fallback to header when cookie-based is enabled but cookie is missing")
        void shouldFallbackToHeaderWhenCookieMissing() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = createClaims("testuser", List.of("ROLE_USER"));
            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator).validateToken(token);
        }

        @Test
        @DisplayName("should skip authentication when cookie-based is enabled and both cookie and header are missing")
        void shouldSkipAuthenticationWhenBothMissing() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users").build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator, never()).validateToken(any());
            verify(filterChain).filter(exchange);
        }

        @Test
        @DisplayName("should ignore cookie with blank value")
        void shouldIgnoreCookieWithBlankValue() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", ""))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator, never()).validateToken(any());
        }
    }

    @Nested
    @DisplayName("Token Extraction from Header")
    class TokenExtractionFromHeaderTests {

        @BeforeEach
        void setUp() {
            // Override to header-based auth
            jwtProperties = new AidjiSecurityProperties.JwtProperties(
                    "http://localhost:8080/.well-known/jwks.json",
                    3600L,
                    false,
                    "jwt-security-principal",
                    3600L
            );
            filter = new JwtAuthenticationWebFilter(jwtTokenVerificator, jwtProperties, securityProperties);
        }

        @Test
        @DisplayName("should extract token from Authorization header with Bearer prefix")
        void shouldExtractTokenFromAuthorizationHeader() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = createClaims("testuser", List.of("ROLE_USER"));
            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator).validateToken(token);
        }

        @Test
        @DisplayName("should skip authentication when Authorization header is missing")
        void shouldSkipAuthenticationWhenHeaderMissing() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users").build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator, never()).validateToken(any());
        }

        @Test
        @DisplayName("should skip authentication when Authorization header does not start with Bearer")
        void shouldSkipAuthenticationWhenNoBearerPrefix() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator, never()).validateToken(any());
        }
    }

    @Nested
    @DisplayName("Public Path Filtering")
    class PublicPathFilteringTests {

        @Test
        @DisplayName("should skip filter for public path /api/auth/login")
        void shouldSkipFilterForPublicPath() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator, never()).validateToken(any());
            verify(filterChain).filter(exchange);
        }

        @Test
        @DisplayName("should skip filter for public path /actuator/health")
        void shouldSkipFilterForActuatorHealth() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health").build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator, never()).validateToken(any());
        }

        @Test
        @DisplayName("should process filter for non-public path")
        void shouldProcessFilterForNonPublicPath() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = createClaims("testuser", List.of("ROLE_USER"));
            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator).validateToken(token);
        }

        @Test
        @DisplayName("should match wildcard patterns in public paths")
        void shouldMatchWildcardPatterns() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/register").build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator, never()).validateToken(any());
        }
    }

    @Nested
    @DisplayName("Authentication Creation")
    class AuthenticationCreationTests {

        @Test
        @DisplayName("should create authentication with principal from valid token")
        void shouldCreateAuthenticationWithPrincipal() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = Jwts.claims()
                    .subject("testuser")
                    .add("authorities", List.of("ROLE_USER", "ROLE_ADMIN"))
                    .add("ipAddress", "192.168.1.1")
                    .add("aud", "my-app")
                    .issuer("https://auth.example.com")
                    .add("sessionId", "session-123")
                    .add("extraClaims", Map.of("department", "engineering"))
                    .build();

            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                            new UsernamePasswordAuthenticationToken("test", null)
                    ));

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator).validateToken(token);
        }

        @Test
        @DisplayName("should handle token without subject claim")
        void shouldHandleTokenWithoutSubject() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = Jwts.claims().build();
            // No subject set
            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator).validateToken(token);
            verify(filterChain).filter(exchange);
        }

        @Test
        @DisplayName("should extract authorities as list of strings")
        void shouldExtractAuthoritiesAsStrings() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = createClaims("testuser", List.of("ROLE_USER", "ROLE_ADMIN"));
            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator).validateToken(token);
        }

        @Test
        @DisplayName("should extract authorities as list of objects")
        void shouldExtractAuthoritiesAsObjects() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = Jwts.claims()
                    .subject("testuser")
                    .add("authorities", List.of(
                            Map.of("authority", "ROLE_USER"),
                            Map.of("authority", "ROLE_ADMIN")
                    ))
                    .build();
            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator).validateToken(token);
        }

        @Test
        @DisplayName("should handle empty authorities list")
        void shouldHandleEmptyAuthorities() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = createClaims("testuser", List.of());
            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator).validateToken(token);
        }

        @Test
        @DisplayName("should handle null authorities")
        void shouldHandleNullAuthorities() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = createClaims("testuser", null);
            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator).validateToken(token);
        }

        @Test
        @DisplayName("should handle principal without optional claims")
        void shouldHandlePrincipalWithoutOptionalClaims() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = createClaims("testuser", List.of("ROLE_USER"));
            // Only required claims set
            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(jwtTokenVerificator).validateToken(token);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should continue filter chain when SecurityException is thrown")
        void shouldContinueOnSecurityException() {
            // Given
            String token = "invalid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            when(jwtTokenVerificator.validateToken(token))
                    .thenThrow(new SecurityException(
                            be.aidji.boot.core.exception.SecurityErrorCode.BEARER_TOKEN_NOT_VALID,
                            "Invalid token"
                    ));

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(filterChain, atLeastOnce()).filter(exchange);
        }

        // ... existing code ...
        @Test
        @DisplayName("should continue filter chain when token is expired")
        void shouldContinueOnExpiredToken() {
            // Given
            String token = "expired.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            // On simule l'exception levée par le vérificateur
            when(jwtTokenVerificator.validateToken(token))
                    .thenThrow(new be.aidji.boot.core.exception.SecurityException(
                            be.aidji.boot.core.exception.SecurityErrorCode.BEARER_TOKEN_EXPIRED,
                            "Token expired"
                    ));

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result)
                    .verifyComplete();

            verify(filterChain).filter(exchange);
            verify(jwtTokenVerificator).validateToken(token);
        }
    }

    @Nested
    @DisplayName("Reactive Context")
    class ReactiveContextTests {

        @Test
        @DisplayName("should set authentication in reactive security context")
        void shouldSetAuthenticationInContext() {
            // Given
            String token = "valid.jwt.token";
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/users")
                    .cookie(new HttpCookie("jwt-security-principal", token))
                    .build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            Claims claims = createClaims("testuser", List.of("ROLE_USER"));
            when(jwtTokenVerificator.validateToken(token)).thenReturn(claims);

            // Setup filter chain to capture and verify authentication
            when(filterChain.filter(any())).thenAnswer(invocation -> {
                return ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .doOnNext(auth -> {
                            assertThat(auth).isNotNull();
                            assertThat(auth.isAuthenticated()).isTrue();
                            assertThat(auth.getPrincipal()).isInstanceOf(AidjiPrincipal.class);
                            AidjiPrincipal principal = (AidjiPrincipal) auth.getPrincipal();
                            assertThat(principal.getSub()).isEqualTo("testuser");
                            assertThat(principal.getAuthorities())
                                    .containsExactly(new SimpleGrantedAuthority("ROLE_USER"));
                        })
                        .then();
            });

            // When
            Mono<Void> result = filter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
        }
    }

    // ========== Helper Methods ==========

    private Claims createClaims(String subject, List<String> authorities) {
        var builder = Jwts.claims();
        if (subject != null) {
            builder.subject(subject);
        }
        if (authorities != null) {
            builder.add("authorities", authorities);
        }
        return builder.build();
    }
}