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
 * Web layer auto-configuration and CORS setup.
 *
 * <p>This package provides Spring Boot auto-configuration for web layer components:</p>
 *
 * <ul>
 *   <li>{@link be.aidji.boot.web.config.AidjiWebAutoConfiguration} - Configures global exception handler,
 *       request logging filter, and RestClient factory</li>
 *   <li>{@link be.aidji.boot.web.config.AidjiCorsConfiguration} - Configures Cross-Origin Resource Sharing (CORS)
 *       based on application properties</li>
 * </ul>
 *
 * <h2>Auto-Configuration Features</h2>
 * <ul>
 *   <li>Global exception handler for consistent error responses</li>
 *   <li>Request logging with configurable verbosity</li>
 *   <li>RestClient factory with default configuration</li>
 *   <li>CORS support for cross-origin requests</li>
 * </ul>
 *
 * <h2>CORS Configuration Example</h2>
 * <pre>{@code
 * aidji:
 *   web:
 *     cors:
 *       enabled: true
 *       allowed-origins:
 *         - https://myapp.com
 *         - https://staging.myapp.com
 *       allowed-methods:
 *         - GET
 *         - POST
 *         - PUT
 *         - DELETE
 *       allowed-headers: "*"
 *       allow-credentials: true
 *       max-age: 3600
 * }</pre>
 *
 * <h2>Request Logging Configuration</h2>
 * <pre>{@code
 * aidji:
 *   web:
 *     request-logging:
 *       enabled: true
 *       include-headers: true
 *       include-payload: false
 *       max-payload-length: 1000
 * }</pre>
 *
 * @see be.aidji.boot.web.config.AidjiWebAutoConfiguration
 * @see be.aidji.boot.web.config.AidjiCorsConfiguration
 * @see be.aidji.boot.web.AidjiWebProperties
 */
package be.aidji.boot.web.config;