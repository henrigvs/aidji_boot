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
 * Exception for technical/infrastructure errors.
 * These are unexpected errors such as database failures, network issues,
 * or external service unavailability.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * try {
 *     externalService.call();
 * } catch (IOException e) {
 *     throw new TechnicalException(CommonErrorCode.EXTERNAL_SERVICE_ERROR, "Failed to call payment service", e);
 * }
 * }</pre>
 */
public class TechnicalException extends AidjiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TechnicalException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TechnicalException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public TechnicalException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public TechnicalException(ErrorCode errorCode, String message, Throwable cause, Map<String, Object> context) {
        super(errorCode, message, cause, context);
    }

    /**
     * Creates a builder for fluent exception construction.
     */
    public static Builder builder(ErrorCode errorCode) {
        return new Builder(errorCode);
    }

    /**
     * Wraps any throwable as a TechnicalException with INTERNAL_ERROR code.
     */
    public static TechnicalException wrap(Throwable cause) {
        if (cause instanceof TechnicalException te) {
            return te;
        }
        return new TechnicalException(CommonErrorCode.INTERNAL_ERROR, cause.getMessage(), cause);
    }

    /**
     * Fluent builder for TechnicalException.
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

        public TechnicalException build() {
            return new TechnicalException(errorCode, message, cause, context);
        }
    }
}
