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
 * Tests for {@link TechnicalException}.
 */
@DisplayName("TechnicalException")
class TechnicalExceptionTest {

    @Nested
    @DisplayName("constructors")
    class ConstructorTests {

        @Test
        @DisplayName("should create with error code and message")
        void shouldCreateWithErrorCodeAndMessage() {
            // When
            TechnicalException exception = new TechnicalException(
                    CommonErrorCode.SERVICE_UNAVAILABLE,
                    "Database is down"
            );

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.SERVICE_UNAVAILABLE);
            assertThat(exception.getMessage()).isEqualTo("Database is down");
            assertThat(exception.getHttpStatus()).isEqualTo(503);
            assertThat(exception.getErrorId()).isNotNull();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("should create with error code, message and cause")
        void shouldCreateWithCause() {
            // Given
            Exception cause = new RuntimeException("Connection timeout");

            // When
            TechnicalException exception = new TechnicalException(
                    CommonErrorCode.EXTERNAL_SERVICE_TIMEOUT,
                    "API call failed",
                    cause
            );

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.EXTERNAL_SERVICE_TIMEOUT);
            assertThat(exception.getMessage()).isEqualTo("API call failed");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getHttpStatus()).isEqualTo(504);
        }
    }

    @Nested
    @DisplayName("wrap")
    class WrapTests {

        @Test
        @DisplayName("should wrap exception with INTERNAL_ERROR code")
        void shouldWrapException() {
            // Given
            RuntimeException original = new RuntimeException("Original error");

            // When
            TechnicalException wrapped = TechnicalException.wrap(original);

            // Then
            assertThat(wrapped.getErrorCode()).isEqualTo(CommonErrorCode.INTERNAL_ERROR);
            assertThat(wrapped.getMessage()).isEqualTo("Original error");
            assertThat(wrapped.getCause()).isEqualTo(original);
            assertThat(wrapped.getHttpStatus()).isEqualTo(500);
        }

        @Test
        @DisplayName("should return same exception if already TechnicalException")
        void shouldReturnSameIfAlreadyTechnical() {
            // Given
            TechnicalException original = new TechnicalException(
                    CommonErrorCode.SERVICE_UNAVAILABLE,
                    "Service down"
            );

            // When
            TechnicalException wrapped = TechnicalException.wrap(original);

            // Then
            assertThat(wrapped).isSameAs(original);
        }

        @Test
        @DisplayName("should wrap exception preserving message")
        void shouldPreserveOriginalDetails() {
            // Given
            RuntimeException original = new RuntimeException("Wrapper message");

            // When
            TechnicalException wrapped = TechnicalException.wrap(original);

            // Then
            assertThat(wrapped.getMessage()).isEqualTo("Wrapper message");
            assertThat(wrapped.getCause()).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("builder")
    class BuilderTests {

        @Test
        @DisplayName("should create with builder")
        void shouldCreateWithBuilder() {
            // When
            TechnicalException exception = TechnicalException.builder(CommonErrorCode.SERVICE_UNAVAILABLE)
                    .message("Custom message")
                    .build();

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.SERVICE_UNAVAILABLE);
            assertThat(exception.getMessage()).isEqualTo("Custom message");
        }

        @Test
        @DisplayName("should create with builder and formatted message")
        void shouldCreateWithFormattedMessage() {
            // When
            TechnicalException exception = TechnicalException.builder(CommonErrorCode.EXTERNAL_SERVICE_ERROR)
                    .message("Error calling %s: %d", "payment-service", 500)
                    .build();

            // Then
            assertThat(exception.getMessage()).isEqualTo("Error calling payment-service: 500");
        }

        @Test
        @DisplayName("should create with builder and context")
        void shouldCreateWithContext() {
            // When
            TechnicalException exception = TechnicalException.builder(CommonErrorCode.INTERNAL_ERROR)
                    .context("operation", "save")
                    .context("entity", "User")
                    .build();

            // Then
            assertThat(exception.getContext()).hasSize(2);
            assertThat(exception.getContext()).containsEntry("operation", "save");
            assertThat(exception.getContext()).containsEntry("entity", "User");
        }
    }

    @Nested
    @DisplayName("toLogString")
    class ToLogStringTests {

        @Test
        @DisplayName("should format log string correctly")
        void shouldFormatLogString() {
            // Given
            TechnicalException exception = new TechnicalException(
                    CommonErrorCode.INTERNAL_ERROR,
                    "System failure"
            );

            // When
            String logString = exception.toLogString();

            // Then
            assertThat(logString).contains("AIDJI-001");
            assertThat(logString).contains("An internal error occurred");
            assertThat(logString).contains("System failure");
            assertThat(logString).contains("errorId=");
        }
    }

    @Nested
    @DisplayName("inheritance")
    class InheritanceTests {

        @Test
        @DisplayName("should extend AidjiException")
        void shouldExtendAidjiException() {
            // Given
            TechnicalException exception = new TechnicalException(
                    CommonErrorCode.INTERNAL_ERROR,
                    "Test"
            );

            // When / Then
            assertThat(exception).isInstanceOf(AidjiException.class);
        }

        @Test
        @DisplayName("should be serializable")
        void shouldBeSerializable() {
            // Given
            TechnicalException exception = new TechnicalException(
                    CommonErrorCode.INTERNAL_ERROR,
                    "Test"
            );

            // When / Then
            assertThat(exception).isInstanceOf(java.io.Serializable.class);
        }
    }
}
