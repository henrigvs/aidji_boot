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
 * JWT (JSON Web Token) authentication implementation.
 *
 * <p>This package provides stateless authentication using JWT tokens with two modes:</p>
 *
 * <ul>
 *   <li><b>CIPM Mode</b> ({@link be.aidji.boot.security.jwt.cipm}) - JWT generation via external CIPM service
 *       and validation using JWKS endpoint for public key retrieval</li>
 *   <li><b>Standalone Mode</b> ({@link be.aidji.boot.security.jwt.stand_alone}) - Self-contained JWT generation
 *       and validation using application-managed RSA key pairs</li>
 *   <li>{@link be.aidji.boot.security.jwt.JwtAuthenticationFilter} - Servlet filter that intercepts
 *       requests, extracts JWT from cookies or Authorization header, and sets up Spring Security context</li>
 * </ul>
 *
 * <h2>Token Sources</h2>
 * <p>JWT tokens can be provided via:</p>
 * <ul>
 *   <li><b>HTTP-only Cookie</b> (recommended) - More secure, prevents XSS attacks</li>
 *   <li><b>Authorization Header</b> - Standard for APIs ({@code Authorization: Bearer <token>})</li>
 * </ul>
 *
 * <h2>Mode Selection</h2>
 * <p>Choose your JWT mode based on your requirements:</p>
 * <ul>
 *   <li><b>CIPM Mode</b> - Use when you have a centralized identity management service backed by HashiCorp Vault.
 *       Provides centralized key management and token signing across multiple applications.</li>
 *   <li><b>Standalone Mode</b> - Use for self-contained applications that manage their own JWT keys.
 *       Simpler setup but requires application-level key management.</li>
 * </ul>
 *
 * <h2>Configuration - CIPM Mode</h2>
 * <pre>{@code
 * aidji:
 *   security:
 *     enabled: true
 *     public-paths:
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
 * <h2>Configuration - Standalone Mode</h2>
 * <pre>{@code
 * aidji:
 *   security:
 *     enabled: true
 *     public-paths:
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
 * <h2>Usage - Validating Tokens</h2>
 * <pre>{@code
 * @Service
 * public class TokenService {
 *
 *     private final JwtTokenVerificator verificator;
 *
 *     public Claims validate(String token) {
 *         return verificator.validateToken(token);
 *     }
 *
 *     public boolean isValid(String token) {
 *         return verificator.isValid(token);
 *     }
 * }
 * }</pre>
 *
 * <h2>Supported Identity Providers</h2>
 * <table border="1">
 *   <tr><th>Provider</th><th>JWKS URL Format</th></tr>
 *   <tr><td>Keycloak</td><td>{@code https://{host}/realms/{realm}/protocol/openid-connect/certs}</td></tr>
 *   <tr><td>Auth0</td><td>{@code https://{tenant}.auth0.com/.well-known/jwks.json}</td></tr>
 *   <tr><td>Okta</td><td>{@code https://{domain}/oauth2/default/v1/keys}</td></tr>
 *   <tr><td>Azure AD</td><td>{@code https://login.microsoftonline.com/{tenant}/discovery/v2.0/keys}</td></tr>
 * </table>
 *
 * @see be.aidji.boot.security.jwt.cipm.JwtTokenVerificatorCipm
 * @see be.aidji.boot.security.jwt.JwtAuthenticationFilter
 * @see be.aidji.boot.security.AidjiSecurityProperties
 */
package be.aidji.boot.security.jwt;