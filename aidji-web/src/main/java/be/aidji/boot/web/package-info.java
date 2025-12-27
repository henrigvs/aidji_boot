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
 * Aidji Framework Web Module.
 *
 * <p>This module provides web layer components for building REST APIs:</p>
 *
 * <ul>
 *   <li><b>Exception handling</b> - Global exception handler with standardized responses ({@link be.aidji.boot.web.exception})</li>
 *   <li><b>RestClient</b> - Pre-configured HTTP client with logging and error handling ({@link be.aidji.boot.web.client})</li>
 *   <li><b>Filters</b> - Request logging and trace ID propagation ({@link be.aidji.boot.web.filter})</li>
 *   <li><b>Configuration</b> - Auto-configuration and properties ({@link be.aidji.boot.web.config})</li>
 * </ul>
 *
 * <p>All components are autoconfigured and can be customized via properties:</p>
 * <pre>{@code
 * aidji:
 *   web:
 *     cors:
 *       enabled: true
 *       allowed-origins:
 *         - https://myapp.com
 *     request-logging:
 *       enabled: true
 *       include-headers: false
 * }</pre>
 *
 * @see be.aidji.boot.web.config.AidjiWebAutoConfiguration
 * @see be.aidji.boot.web.AidjiWebProperties
 */
package be.aidji.boot.web;
