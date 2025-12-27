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
 * <p>This package provides stateless authentication using JWT tokens:</p>
 *
 * <ul>
 *   <li>{@link be.aidji.boot.security.jwt.JwtTokenProvider} - Generates and validates JWT tokens,
 *       extracts user information from tokens</li>
 *   <li>{@link be.aidji.boot.security.jwt.JwtAuthenticationFilter} - Servlet filter that intercepts
 *       requests, extracts JWT from cookies or Authorization header, and sets up Spring Security context</li>
 * </ul>
 *
 * <h2>Token Sources</h2>
 * <p>JWT tokens can be provided via:</p>
 * <ul>
 *   <li><b>HTTP-only Cookie</b> - More secure, prevents XSS attacks (cookie name: {@code auth_token})</li>
 *   <li><b>Authorization Header</b> - Standard for APIs ({@code Authorization: Bearer <token>})</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <pre>{@code
 * aidji:
 *   security:
 *     jwt:
 *       secret-key: "your-base64-encoded-256-bit-secret"
 *       expiration: 86400000  # 24 hours in milliseconds
 *       public-paths:
 *         - /api/auth/**
 *         - /api/public/**
 * }</pre>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/auth")
 * public class AuthController {
 *
 *     private final JwtTokenProvider tokenProvider;
 *
 *     @PostMapping("/login")
 *     public ResponseEntity<?> login(@RequestBody LoginRequest request) {
 *         // Authenticate user...
 *         Authentication auth = authenticationManager.authenticate(...);
 *
 *         // Generate JWT
 *         String token = tokenProvider.generateToken(auth);
 *
 *         // Return token in cookie or response body
 *         return ResponseEntity.ok()
 *             .header(HttpHeaders.SET_COOKIE, createAuthCookie(token))
 *             .body(new LoginResponse(token));
 *     }
 * }
 * }</pre>
 *
 * @see be.aidji.boot.security.jwt.JwtTokenProvider
 * @see be.aidji.boot.security.jwt.JwtAuthenticationFilter
 * @see be.aidji.boot.security.AidjiSecurityProperties
 */
package be.aidji.boot.security.jwt;