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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FunctionalException}.
 */
@DisplayName("FunctionalException")
class FunctionalExceptionTest {

    @Nested
    @DisplayName("builder")
    class BuilderTests {

        @Test
        @DisplayName("should create exception with error code only")
        void shouldCreateWithErrorCodeOnly() {
            // When
            FunctionalException exception = FunctionalException.builder(CommonErrorCode.NOT_FOUND)
                    .build();

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.NOT_FOUND);
            assertThat(exception.getMessage()).isEqualTo("Resource not found");
            assertThat(exception.getHttpStatus()).isEqualTo(404);
            assertThat(exception.getErrorId()).isNotNull();
            assertThat(exception.getContext()).isEmpty();
        }

        @Test
        @DisplayName("should create exception with custom message")
        void shouldCreateWithCustomMessage() {
            // When
            FunctionalException exception = FunctionalException.builder(CommonErrorCode.VALIDATION_ERROR)
                    .message("Email is invalid")
                    .build();

            // Then
            assertThat(exception.getMessage()).isEqualTo("Email is invalid");
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("should create exception with formatted message")
        void shouldCreateWithFormattedMessage() {
            // When
            FunctionalException exception = FunctionalException.builder(CommonErrorCode.NOT_FOUND)
                    .message("User with id %d not found", 123)
                    .build();

            // Then
            assertThat(exception.getMessage()).isEqualTo("User with id 123 not found");
        }

        @Test
        @DisplayName("should create exception with cause")
        void shouldCreateWithCause() {
            // Given
            RuntimeException cause = new RuntimeException("Original cause");

            // When
            FunctionalException exception = FunctionalException.builder(CommonErrorCode.INTERNAL_ERROR)
                    .cause(cause)
                    .build();

            // Then
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("should create exception with context")
        void shouldCreateWithContext() {
            // When
            FunctionalException exception = FunctionalException.builder(CommonErrorCode.CONFLICT)
                    .context("userId", 456)
                    .context("email", "test@example.com")
                    .build();

            // Then
            assertThat(exception.getContext()).hasSize(2);
            assertThat(exception.getContext()).containsEntry("userId", 456);
            assertThat(exception.getContext()).containsEntry("email", "test@example.com");
        }

        @Test
        @DisplayName("should create exception with all fields")
        void shouldCreateWithAllFields() {
            // Given
            RuntimeException cause = new RuntimeException("Root cause");

            // When
            FunctionalException exception = FunctionalException.builder(CommonErrorCode.FORBIDDEN)
                    .message("Access denied for user %s", "john")
                    .cause(cause)
                    .context("userId", 789)
                    .context("resource", "/admin")
                    .build();

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.FORBIDDEN);
            assertThat(exception.getMessage()).isEqualTo("Access denied for user john");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getHttpStatus()).isEqualTo(403);
            assertThat(exception.getContext()).hasSize(2);
            assertThat(exception.getErrorId()).isNotNull();
            assertThat(exception.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toLogString")
    class ToLogStringTests {

        @Test
        @DisplayName("should format log string with all fields")
        void shouldFormatLogString() {
            // Given
            FunctionalException exception = FunctionalException.builder(CommonErrorCode.VALIDATION_ERROR)
                    .message("Invalid input")
                    .context("field", "email")
                    .build();

            // When
            String logString = exception.toLogString();

            // Then
            assertThat(logString).contains("AIDJI-002");
            assertThat(logString).contains("Validation failed");
            assertThat(logString).contains("Invalid input");
            assertThat(logString).contains("errorId=");
        }
    }

    @Nested
    @DisplayName("serialization")
    class SerializationTests {

        @Test
        @DisplayName("should have serialVersionUID")
        void shouldHaveSerialVersionUID() {
            // Given
            FunctionalException exception = FunctionalException.builder(CommonErrorCode.NOT_FOUND)
                    .build();

            // When / Then
            assertThat(exception).isInstanceOf(java.io.Serializable.class);
        }
    }
}
