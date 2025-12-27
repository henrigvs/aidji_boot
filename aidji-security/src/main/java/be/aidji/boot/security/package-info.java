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
 * <p>This module provides a pre-configured, opinionated security setup for Spring Boot applications:</p>
 *
 * <ul>
 *   <li><b>JWT Authentication</b> - Stateless authentication via HTTP-only cookies or Authorization header
 *       ({@link be.aidji.boot.security.jwt})</li>
 *   <li><b>Autoconfiguration</b> - Zero-config security with sensible defaults
 *       ({@link be.aidji.boot.security.config})</li>
 *   <li><b>Error Handlers</b> - Standardized JSON responses for 401/403 errors
 *       ({@link be.aidji.boot.security.handler})</li>
 * </ul>
 *
 * <h2>Quick Start</h2>
 * <p>Add the dependency and configure minimal properties:</p>
 * <pre>{@code
 * aidji:
 *   security:
 *     jwt:
 *       secret-key: "your-base64-encoded-secret-key"
 *       public-paths:
 *         - /api/auth/**
 *         - /api/public/**
 * }</pre>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>JWT token generation and validation</li>
 *   <li>Cookie-based or header-based authentication</li>
 *   <li>Configurable public paths (no auth required)</li>
 *   <li>CORS configuration</li>
 *   <li>CSRF disabled (stateless)</li>
 *   <li>Method-level security (@Secured, @RolesAllowed)</li>
 *   <li>BCrypt password encoding</li>
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
 * @see be.aidji.boot.security.config.AidjiSecurityAutoConfiguration
 * @see be.aidji.boot.security.config.AidjiSecurityCustomizer
 * @see be.aidji.boot.security.jwt.JwtTokenProvider
 * @see be.aidji.boot.security.AidjiSecurityProperties
 */
package be.aidji.boot.security;