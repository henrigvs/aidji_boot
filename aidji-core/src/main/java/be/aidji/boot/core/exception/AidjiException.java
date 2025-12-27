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

import java.io.Serial;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Base exception for all Aidji framework exceptions.
 * Provides structured error information including error codes, context, and traceability.
 */
@Getter
public abstract class AidjiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this exception instance, useful for log correlation.
     */
    private final String errorId;

    /**
     * The error code providing categorization and HTTP status mapping.
     */
    private final ErrorCode errorCode;

    /**
     * Timestamp when this exception was created.
     */
    private final Instant timestamp;

    /**
     * Additional context information about the error.
     */
    private final Map<String, Object> context;

    protected AidjiException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null, Map.of());
    }

    protected AidjiException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, null, cause, Map.of());
    }
    protected AidjiException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, Map.of());
    }

    protected AidjiException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, Map.of());
    }

    protected AidjiException(ErrorCode errorCode, String message, Throwable cause, Map<String, Object> context) {
        super(message, cause);
        this.errorId = UUID.randomUUID().toString();
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.context = context != null ? Collections.unmodifiableMap(new HashMap<>(context)) : Map.of();
    }

    /**
     * Returns the HTTP status code associated with this exception.
     */
    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    /**
     * Returns a formatted string representation for logging.
     */
    public String toLogString() {
        return String.format("[%s] %s - %s (errorId=%s)",
                errorCode.getCode(),
                errorCode.getDefaultMessage(),
                getMessage(),
                errorId);
    }
}
