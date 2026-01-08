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
 * <p>This package provides stateless authentication using JWT tokens with support
 * for asymmetric (RSA) key verification via JWKS endpoints:</p>
 *
 * <ul>
 *   <li>{@link be.aidji.boot.security.jwt.cipm.JwtTokenVerificatorCipm} - Validates JWT tokens signed with
 *       asymmetric keys (RS256) by fetching and caching public keys from a JWKS endpoint</li>
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
 * <h2>JWKS Integration</h2>
 * <p>The {@link be.aidji.boot.security.jwt.cipm.JwtTokenVerificatorCipm} fetches public keys from external
 * Identity Providers such as Keycloak, Auth0, or Okta. Features include:</p>
 * <ul>
 *   <li>Automatic key caching with configurable TTL</li>
 *   <li>Automatic cache refresh on key rotation (unknown kid)</li>
 *   <li>Thread-safe key storage</li>
 *   <li>No external JSON library required (native Java parsing)</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <pre>{@code
 * aidji:
 *   security:
 *     jwt:
 *       public-key-url: https://auth.example.com/.well-known/jwks.json
 *       public-key-cache-ttl-seconds: 3600
 *       cookie-based: true
 *       cookie-name: "auth-token"
 *       public-paths:
 *         - /api/auth/**
 *         - /api/public/**
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