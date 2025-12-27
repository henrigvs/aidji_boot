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
 * Global exception handling for REST APIs.
 *
 * <p>This package provides centralized exception handling:</p>
 *
 * <ul>
 *   <li>{@link be.aidji.boot.web.exception.GlobalExceptionHandler} - {@code @RestControllerAdvice} that intercepts
 *       exceptions and returns standardized {@link be.aidji.boot.core.dto.ApiResponse} error responses</li>
 * </ul>
 *
 * <h2>Handled Exception Types</h2>
 * <ul>
 *   <li>{@link be.aidji.boot.core.exception.AidjiException} - Framework exceptions with error codes</li>
 *   <li>{@link be.aidji.boot.core.exception.FunctionalException} - Business logic errors (HTTP 400)</li>
 *   <li>{@link be.aidji.boot.core.exception.TechnicalException} - Technical errors (HTTP 500)</li>
 *   <li>{@link be.aidji.boot.core.exception.SecurityException} - Security errors (HTTP 401/403)</li>
 *   <li>{@link org.springframework.web.bind.MethodArgumentNotValidException} - Validation errors (HTTP 400)</li>
 *   <li>{@link java.lang.Exception} - Catch-all for unhandled exceptions (HTTP 500)</li>
 * </ul>
 *
 * <h2>Error Response Format</h2>
 * <p>All exceptions are converted to this standardized JSON format:</p>
 * <pre>{@code
 * {
 *   "success": false,
 *   "error": {
 *     "code": "ERR-001",
 *     "message": "User not found",
 *     "details": {
 *       "userId": 123,
 *       "timestamp": "2025-01-15T10:30:00Z"
 *     }
 *   }
 * }
 * }</pre>
 *
 * <h2>Validation Errors</h2>
 * <p>Bean validation failures return field-specific errors:</p>
 * <pre>{@code
 * {
 *   "success": false,
 *   "error": {
 *     "code": "VALIDATION_ERROR",
 *     "message": "Validation failed",
 *     "details": {
 *       "email": "must be a valid email address",
 *       "age": "must be greater than or equal to 18"
 *     }
 *   }
 * }
 * }</pre>
 *
 * @see be.aidji.boot.web.exception.GlobalExceptionHandler
 * @see be.aidji.boot.core.dto.ApiResponse
 * @see be.aidji.boot.core.exception.AidjiException
 */
package be.aidji.boot.web.exception;