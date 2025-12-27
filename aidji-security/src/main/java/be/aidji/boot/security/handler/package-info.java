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
 * Security error handlers for standardized error responses.
 *
 * <p>This package provides custom error handlers that return JSON responses for security errors:</p>
 *
 * <ul>
 *   <li>{@link be.aidji.boot.security.handler.AidjiAuthenticationEntryPoint} - Handles 401 Unauthorized errors
 *       when authentication fails or is missing</li>
 *   <li>{@link be.aidji.boot.security.handler.AidjiAccessDeniedHandler} - Handles 403 Forbidden errors
 *       when the user lacks required permissions</li>
 * </ul>
 *
 * <p>Both handlers return JSON responses in the following format:</p>
 * <pre>{@code
 * {
 *   "success": false,
 *   "error": {
 *     "code": "SEC-001",
 *     "message": "Authentication required"
 *   }
 * }
 * }</pre>
 *
 * <p>These handlers are automatically configured by {@link be.aidji.boot.security.config.AidjiSecurityAutoConfiguration}
 * and integrate with the framework's exception hierarchy.</p>
 *
 * @see be.aidji.boot.security.handler.AidjiAuthenticationEntryPoint
 * @see be.aidji.boot.security.handler.AidjiAccessDeniedHandler
 * @see be.aidji.boot.core.dto.ApiResponse
 */
package be.aidji.boot.security.handler;