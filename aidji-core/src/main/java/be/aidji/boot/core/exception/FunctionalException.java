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

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception for functional logic errors.
 * These are expected errors that occur during normal application flow,
 * such as validation failures, resource not found, or business rule violations.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * throw new FunctionalException(FunctionalErrorCode.USER_NOT_FOUND, "User with id 123 not found");
 *
 * // With context
 * throw FunctionalException.builder(FunctionalErrorCode.USER_ALREADY_EXISTS)
 *     .message("Email already registered")
 *     .context("email", email)
 *     .build();
 * }</pre>
 */
public class FunctionalException extends AidjiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public FunctionalException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FunctionalException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public FunctionalException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public FunctionalException(ErrorCode errorCode, String message, Throwable cause, Map<String, Object> context) {
        super(errorCode, message, cause, context);
    }

    /**
     * Creates a builder for fluent exception construction.
     */
    public static Builder builder(ErrorCode errorCode) {
        return new Builder(errorCode);
    }

    /**
     * Fluent builder for FunctionalException.
     */
    public static class Builder {
        private final ErrorCode errorCode;
        private String message;
        private Throwable cause;
        private final Map<String, Object> context = new HashMap<>();

        private Builder(ErrorCode errorCode) {
            this.errorCode = errorCode;
            this.message = errorCode.getDefaultMessage();
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder message(String format, Object... args) {
            this.message = String.format(format, args);
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public Builder context(String key, Object value) {
            this.context.put(key, value);
            return this;
        }

        public Builder context(Map<String, Object> context) {
            this.context.putAll(context);
            return this;
        }

        public FunctionalException build() {
            return new FunctionalException(errorCode, message, cause, context);
        }
    }
}
