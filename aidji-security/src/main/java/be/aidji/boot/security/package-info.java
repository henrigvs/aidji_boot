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

/**
 * Aidji Boot Security Module.
 *
 * <p>This module provides a pre-configured, opinionated security setup for Spring Boot applications
 * with two JWT modes:</p>
 *
 * <ul>
 *   <li><b>CIPM Mode</b> - JWT generation and validation via external CIPM service backed by HashiCorp Vault
 *       ({@link be.aidji.boot.security.jwt.cipm})</li>
 *   <li><b>Standalone Mode</b> - Self-contained JWT generation and validation using RSA key pairs
 *       ({@link be.aidji.boot.security.jwt.stand_alone})</li>
 *   <li><b>Autoconfiguration</b> - Zero-config security with sensible defaults
 *       ({@link be.aidji.boot.security.config})</li>
 *   <li><b>Error Handlers</b> - Standardized JSON responses for 401/403 errors
 *       ({@link be.aidji.boot.security.handler})</li>
 * </ul>
 *
 * <h2>Quick Start - CIPM Mode</h2>
 * <pre>{@code
 * aidji:
 *   security:
 *     enabled: true
 *     public-paths:
 *       - /api/auth/**
 *       - /api/public/**
 *     jwt:
 *       mode: cipm
 *       generation-enabled: true
 *       cookie-based: true
 *       cookie-name: jwt-security-principal
 *       cipm-properties:
 *         base-url: https://cipm.example.com
 *         public-key-uri: /.well-known/jwks.json
 *         sign-token-uri: /api/sign-token
 *         api-token: ${CIPM_API_TOKEN}
 *         issuer: cipm-issuer
 * }</pre>
 *
 * <h2>Quick Start - Standalone Mode</h2>
 * <pre>{@code
 * aidji:
 *   security:
 *     enabled: true
 *     public-paths:
 *       - /api/auth/**
 *       - /api/public/**
 *     jwt:
 *       mode: standalone
 *       cookie-based: true
 *       cookie-name: jwt-security-principal
 *       standalone:
 *         issuer: my-app
 *         private-key: ${JWT_PRIVATE_KEY}
 *         public-key: ${JWT_PUBLIC_KEY}
 * }</pre>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>JWT token validation via JWKS (RS256)</li>
 *   <li>Automatic public key caching and rotation</li>
 *   <li>Cookie-based or header-based authentication</li>
 *   <li>Configurable public paths (no auth required)</li>
 *   <li>CSRF disabled (stateless)</li>
 *   <li>Method-level security (@Secured, @RolesAllowed)</li>
 *   <li>Standardized error responses using {@link be.aidji.boot.core.dto.ApiResponse}</li>
 * </ul>
 *
 * <h2>Supported Identity Providers</h2>
 * <ul>
 *   <li>Keycloak</li>
 *   <li>Auth0</li>
 *   <li>Okta</li>
 *   <li>Azure AD</li>
 *   <li>Any OIDC-compliant provider with JWKS endpoint</li>
 * </ul>
 *
 * <h2>Customization</h2>
 * <p>Extend the default configuration using {@link be.aidji.boot.security.config.AidjiSecurityCustomizer}:</p>
 * <pre>{@code
 * @Bean
 * public AidjiSecurityCustomizer adminSecurityCustomizer() {
 *     return http -> http
 *         .authorizeHttpRequests(auth -> auth
 *             .requestMatchers("/admin/**").hasRole("ADMIN")
 *         );
 * }
 * }</pre>
 *
 * <h2>Required Beans</h2>
 * <p>Your application must provide:</p>
 * <ul>
 *   <li>{@link org.springframework.security.core.userdetails.UserDetailsService} -
 *       for loading user details during authentication</li>
 * </ul>
 *
 * @see be.aidji.boot.security.config.AidjiSecurityAutoConfiguration
 * @see be.aidji.boot.security.config.AidjiSecurityCustomizer
 * @see be.aidji.boot.security.jwt.cipm.JwtTokenVerificatorCipm
 * @see be.aidji.boot.security.jwt.JwtAuthenticationFilter
 * @see be.aidji.boot.security.AidjiSecurityProperties
 */
package be.aidji.boot.security;