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
package be.aidji.boot.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard API response wrapper providing consistent response structure.
 *
 * @param <T> the type of the response data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        T data,
        ApiMetadata metadata,
        List<ApiError> errors
) {

    /**
     * Creates a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, ApiMetadata.now(), null);
    }

    /**
     * Creates a successful response with data and custom metadata.
     */
    public static <T> ApiResponse<T> success(T data, ApiMetadata metadata) {
        return new ApiResponse<>(data, metadata, null);
    }

    /**
     * Creates a failure response with a single error.
     */
    public static <T> ApiResponse<T> failure(ApiError error) {
        return new ApiResponse<>(null, ApiMetadata.now(), List.of(error));
    }

    /**
     * Creates a failure response with multiple errors.
     */
    public static <T> ApiResponse<T> failure(List<ApiError> errors) {
        return new ApiResponse<>(null, ApiMetadata.now(), errors);
    }

    /**
     * Returns true if this response represents a success (no errors).
     */
    public boolean isSuccess() {
        return errors == null || errors.isEmpty();
    }

    /**
     * Metadata about the API response.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ApiMetadata(
            Instant timestamp,
            String traceId,
            String path
    ) {
        public static ApiMetadata now() {
            return new ApiMetadata(Instant.now(), null, null);
        }

        public static ApiMetadata of(String traceId, String path) {
            return new ApiMetadata(Instant.now(), traceId, path);
        }
    }

    /**
     * Represents a single error in the response.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ApiError(
            String code,
            String message,
            String field,
            String errorId
    ) {
        public static ApiError of(String code, String message) {
            return new ApiError(code, message, null, null);
        }

        public static ApiError of(String code, String message, String field) {
            return new ApiError(code, message, field, null);
        }

        public static ApiError of(String code, String message, String field, String errorId) {
            return new ApiError(code, message, field, errorId);
        }
    }
}
