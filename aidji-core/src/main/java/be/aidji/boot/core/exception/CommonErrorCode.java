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
package be.aidji.boot.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Common error codes provided by the framework.
 * Applications should define their own error codes by implementing {@link ErrorCode}.
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // Generic errors
    INTERNAL_ERROR("AIDJI-001", "An internal error occurred", 500),
    VALIDATION_ERROR("AIDJI-002", "Validation failed", 400),
    NOT_FOUND("AIDJI-003", "Resource not found", 404),
    CONFLICT("AIDJI-004", "Resource conflict", 409),
    FORBIDDEN("AIDJI-005", "Access denied", 403),
    UNAUTHORIZED("AIDJI-006", "Authentication required", 401),
    BAD_REQUEST("AIDJI-007", "Bad request", 400),
    SERVICE_UNAVAILABLE("AIDJI-008", "Service temporarily unavailable", 503),

    // External service errors
    EXTERNAL_SERVICE_ERROR("AIDJI-010", "External service error", 502),
    EXTERNAL_SERVICE_TIMEOUT("AIDJI-011", "External service timeout", 504);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;
}
