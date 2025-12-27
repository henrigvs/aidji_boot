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
 * Structured exception handling framework with error codes.
 *
 * <p>This package provides a hierarchical exception structure for consistent error handling:</p>
 *
 * <h2>Exception Hierarchy</h2>
 * <ul>
 *   <li>{@link be.aidji.boot.core.exception.AidjiException} - Base exception for all framework exceptions</li>
 *   <li>{@link be.aidji.boot.core.exception.TechnicalException} - For technical/infrastructure errors</li>
 *   <li>{@link be.aidji.boot.core.exception.FunctionalException} - For business logic errors</li>
 *   <li>{@link be.aidji.boot.core.exception.SecurityException} - For security-related errors</li>
 * </ul>
 *
 * <h2>Error Codes</h2>
 * <ul>
 *   <li>{@link be.aidji.boot.core.exception.ErrorCode} - Interface for defining error codes</li>
 *   <li>{@link be.aidji.boot.core.exception.CommonErrorCode} - Common error codes (validation, not found, etc.)</li>
 *   <li>{@link be.aidji.boot.core.exception.SecurityErrorCode} - Security-specific error codes</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Throwing a functional exception with error code
 * throw new FunctionalException(CommonErrorCode.ENTITY_NOT_FOUND, "User not found");
 *
 * // Defining custom error codes
 * public enum OrderErrorCode implements ErrorCode {
 *     INVALID_ORDER_STATUS("ORDER-001", "Invalid order status"),
 *     INSUFFICIENT_STOCK("ORDER-002", "Insufficient stock");
 *
 *     private final String code;
 *     private final String message;
 *
 *     OrderErrorCode(String code, String message) {
 *         this.code = code;
 *         this.message = message;
 *     }
 *
 *     @Override
 *     public String getCode() { return code; }
 *
 *     @Override
 *     public String getMessage() { return message; }
 * }
 * }</pre>
 *
 * @see be.aidji.boot.core.exception.AidjiException
 * @see be.aidji.boot.core.exception.ErrorCode
 */
package be.aidji.boot.core.exception;
