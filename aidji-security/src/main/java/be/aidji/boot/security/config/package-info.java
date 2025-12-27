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
 * Security auto-configuration and customization interfaces.
 *
 * <p>This package provides Spring Boot auto-configuration for security:</p>
 *
 * <ul>
 *   <li>{@link be.aidji.boot.security.config.AidjiSecurityAutoConfiguration} - Auto-configures Spring Security
 *       with JWT authentication, CORS, and sensible defaults</li>
 *   <li>{@link be.aidji.boot.security.config.AidjiSecurityCustomizer} - Functional interface for customizing
 *       the default security configuration</li>
 * </ul>
 *
 * <h2>Default Configuration</h2>
 * <p>The auto-configuration sets up:</p>
 * <ul>
 *   <li>Stateless session management (no JSESSIONID)</li>
 *   <li>CSRF disabled (for REST APIs)</li>
 *   <li>JWT authentication filter</li>
 *   <li>Custom error handlers (401/403)</li>
 *   <li>BCrypt password encoder</li>
 *   <li>Method-level security enabled</li>
 * </ul>
 *
 * <h2>Customization Example</h2>
 * <pre>{@code
 * @Bean
 * public AidjiSecurityCustomizer customSecurityRules() {
 *     return http -> http
 *         .authorizeHttpRequests(auth -> auth
 *             .requestMatchers("/admin/**").hasRole("ADMIN")
 *             .requestMatchers("/api/**").authenticated()
 *         );
 * }
 * }</pre>
 *
 * @see be.aidji.boot.security.config.AidjiSecurityAutoConfiguration
 * @see be.aidji.boot.security.config.AidjiSecurityCustomizer
 */
package be.aidji.boot.security.config;