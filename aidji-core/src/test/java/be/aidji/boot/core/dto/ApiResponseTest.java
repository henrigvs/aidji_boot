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

import be.aidji.boot.core.dto.ApiResponse.ApiError;
import be.aidji.boot.core.dto.ApiResponse.ApiMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ApiResponse}.
 */
@DisplayName("ApiResponse")
class ApiResponseTest {

    @Nested
    @DisplayName("success")
    class SuccessTests {

        @Test
        @DisplayName("should create success response with data")
        void shouldCreateSuccessWithData() {
            // Given
            String data = "test-data";

            // When
            ApiResponse<String> response = ApiResponse.success(data);

            // Then
            assertThat(response.data()).isEqualTo("test-data");
            assertThat(response.metadata()).isNotNull();
            assertThat(response.metadata().timestamp()).isNotNull();
            assertThat(response.errors()).isNull();
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("should create success response with custom metadata")
        void shouldCreateSuccessWithCustomMetadata() {
            // Given
            String data = "test-data";
            ApiMetadata metadata = ApiMetadata.of("trace-123", "/api/users");

            // When
            ApiResponse<String> response = ApiResponse.success(data, metadata);

            // Then
            assertThat(response.data()).isEqualTo("test-data");
            assertThat(response.metadata()).isEqualTo(metadata);
            assertThat(response.metadata().traceId()).isEqualTo("trace-123");
            assertThat(response.metadata().path()).isEqualTo("/api/users");
            assertThat(response.errors()).isNull();
            assertThat(response.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("failure")
    class FailureTests {

        @Test
        @DisplayName("should create failure response with single error")
        void shouldCreateFailureWithSingleError() {
            // Given
            ApiError error = ApiError.of("ERR-001", "Test error");

            // When
            ApiResponse<Void> response = ApiResponse.failure(error);

            // Then
            assertThat(response.data()).isNull();
            assertThat(response.metadata()).isNotNull();
            assertThat(response.errors()).hasSize(1);
            assertThat(response.errors().get(0)).isEqualTo(error);
            assertThat(response.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("should create failure response with multiple errors")
        void shouldCreateFailureWithMultipleErrors() {
            // Given
            List<ApiError> errors = List.of(
                    ApiError.of("ERR-001", "First error"),
                    ApiError.of("ERR-002", "Second error")
            );

            // When
            ApiResponse<Void> response = ApiResponse.failure(errors);

            // Then
            assertThat(response.data()).isNull();
            assertThat(response.metadata()).isNotNull();
            assertThat(response.errors()).hasSize(2);
            assertThat(response.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSuccess")
    class IsSuccessTests {

        @Test
        @DisplayName("should return true when errors is null")
        void shouldReturnTrueWhenErrorsNull() {
            // Given
            ApiResponse<String> response = new ApiResponse<>("data", ApiMetadata.now(), null);

            // When / Then
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("should return true when errors is empty")
        void shouldReturnTrueWhenErrorsEmpty() {
            // Given
            ApiResponse<String> response = new ApiResponse<>("data", ApiMetadata.now(), List.of());

            // When / Then
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("should return false when errors exist")
        void shouldReturnFalseWhenErrorsExist() {
            // Given
            ApiResponse<String> response = ApiResponse.failure(ApiError.of("ERR", "Error"));

            // When / Then
            assertThat(response.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("ApiMetadata")
    class ApiMetadataTests {

        @Test
        @DisplayName("should create metadata with now timestamp")
        void shouldCreateMetadataWithNow() {
            // When
            ApiMetadata metadata = ApiMetadata.now();

            // Then
            assertThat(metadata.timestamp()).isNotNull();
            assertThat(metadata.timestamp()).isBeforeOrEqualTo(Instant.now());
            assertThat(metadata.traceId()).isNull();
            assertThat(metadata.path()).isNull();
        }

        @Test
        @DisplayName("should create metadata with traceId and path")
        void shouldCreateMetadataWithTraceIdAndPath() {
            // When
            ApiMetadata metadata = ApiMetadata.of("trace-456", "/api/products");

            // Then
            assertThat(metadata.timestamp()).isNotNull();
            assertThat(metadata.traceId()).isEqualTo("trace-456");
            assertThat(metadata.path()).isEqualTo("/api/products");
        }

        @Test
        @DisplayName("should create full metadata")
        void shouldCreateFullMetadata() {
            // Given
            Instant now = Instant.now();

            // When
            ApiMetadata metadata = new ApiMetadata(now, "trace-789", "/api/orders");

            // Then
            assertThat(metadata.timestamp()).isEqualTo(now);
            assertThat(metadata.traceId()).isEqualTo("trace-789");
            assertThat(metadata.path()).isEqualTo("/api/orders");
        }
    }

    @Nested
    @DisplayName("ApiError")
    class ApiErrorTests {

        @Test
        @DisplayName("should create error with code and message")
        void shouldCreateErrorWithCodeAndMessage() {
            // When
            ApiError error = ApiError.of("ERR-001", "Error message");

            // Then
            assertThat(error.code()).isEqualTo("ERR-001");
            assertThat(error.message()).isEqualTo("Error message");
            assertThat(error.field()).isNull();
            assertThat(error.errorId()).isNull();
        }

        @Test
        @DisplayName("should create error with field")
        void shouldCreateErrorWithField() {
            // When
            ApiError error = ApiError.of("ERR-002", "Validation failed", "email");

            // Then
            assertThat(error.code()).isEqualTo("ERR-002");
            assertThat(error.message()).isEqualTo("Validation failed");
            assertThat(error.field()).isEqualTo("email");
            assertThat(error.errorId()).isNull();
        }

        @Test
        @DisplayName("should create error with all fields")
        void shouldCreateErrorWithAllFields() {
            // When
            ApiError error = ApiError.of("ERR-003", "Server error", "userId", "err-id-123");

            // Then
            assertThat(error.code()).isEqualTo("ERR-003");
            assertThat(error.message()).isEqualTo("Server error");
            assertThat(error.field()).isEqualTo("userId");
            assertThat(error.errorId()).isEqualTo("err-id-123");
        }

        @Test
        @DisplayName("should create error with record constructor")
        void shouldCreateErrorWithRecordConstructor() {
            // When
            ApiError error = new ApiError("CODE", "Message", "field", "id");

            // Then
            assertThat(error.code()).isEqualTo("CODE");
            assertThat(error.message()).isEqualTo("Message");
            assertThat(error.field()).isEqualTo("field");
            assertThat(error.errorId()).isEqualTo("id");
        }
    }
}
