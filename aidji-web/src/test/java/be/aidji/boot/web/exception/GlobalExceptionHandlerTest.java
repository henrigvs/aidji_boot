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
package be.aidji.boot.web.exception;

import be.aidji.boot.core.dto.ApiResponse;
import be.aidji.boot.core.exception.CommonErrorCode;
import be.aidji.boot.core.exception.FunctionalException;
import be.aidji.boot.core.exception.TechnicalException;
import be.aidji.boot.web.AidjiWebProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link GlobalExceptionHandler}.
 */
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;
    private AidjiWebProperties properties;

    @BeforeEach
    void setUp() {
        // Create properties with default config
        properties = new AidjiWebProperties(
                new AidjiWebProperties.ExceptionHandlingProperties(false, true),
                new AidjiWebProperties.RestClientProperties(Duration.ofSeconds(5), Duration.ofSeconds(30), false),
                new AidjiWebProperties.CorsProperties(false, List.of(), List.of(), List.of(), false, Duration.ofHours(1)),
                new AidjiWebProperties.RequestLoggingProperties(false, false, false, List.of())
        );

        handler = new GlobalExceptionHandler(properties);

        // Mock request
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("X-Trace-Id")).thenReturn("test-trace-id");
    }

    @Nested
    @DisplayName("handleBusinessException")
    class HandleBusinessExceptionTests {

        @Test
        @DisplayName("should handle FunctionalException with correct status and error code")
        void shouldHandleFunctionalException() {
            // Given
            FunctionalException exception = FunctionalException.builder(CommonErrorCode.NOT_FOUND)
                    .message("User not found")
                    .build();

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).hasSize(1);
            assertThat(response.getBody().errors().get(0).code()).isEqualTo("AIDJI-003");
            assertThat(response.getBody().errors().get(0).message()).isEqualTo("User not found");
            assertThat(response.getBody().metadata().traceId()).isEqualTo("test-trace-id");
            assertThat(response.getBody().metadata().path()).isEqualTo("/api/test");
        }

        @Test
        @DisplayName("should include errorId when configured")
        void shouldIncludeErrorId() {
            // Given
            FunctionalException exception = FunctionalException.builder(CommonErrorCode.VALIDATION_ERROR)
                    .message("Validation failed")
                    .build();

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors().get(0).errorId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("handleTechnicalException")
    class HandleTechnicalExceptionTests {

        @Test
        @DisplayName("should handle TechnicalException with 500 status")
        void shouldHandleTechnicalException() {
            // Given
            TechnicalException exception = new TechnicalException(
                    CommonErrorCode.INTERNAL_ERROR,
                    "Database connection failed"
            );

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleTechnicalException(exception, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).hasSize(1);
            assertThat(response.getBody().errors().get(0).code()).isEqualTo("AIDJI-001");
            assertThat(response.getBody().errors().get(0).message()).isEqualTo("Database connection failed");
        }
    }

    @Nested
    @DisplayName("handleValidationException")
    class HandleValidationExceptionTests {

        @Test
        @DisplayName("should handle MethodArgumentNotValidException with field errors")
        void shouldHandleMethodArgumentNotValidException() {
            // Given
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError1 = new FieldError("user", "email", "must be a valid email");
            FieldError fieldError2 = new FieldError("user", "name", "must not be blank");
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                    null,
                    bindingResult
            );

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleValidationException(exception, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).hasSize(2);
            assertThat(response.getBody().errors().get(0).code()).isEqualTo("AIDJI-002");
            assertThat(response.getBody().errors().get(0).field()).isEqualTo("email");
            assertThat(response.getBody().errors().get(1).field()).isEqualTo("name");
        }

        @Test
        @DisplayName("should handle ConstraintViolationException")
        void shouldHandleConstraintViolationException() {
            // Given
            @SuppressWarnings("unchecked")
            ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("must be positive");
            when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
            when(violation.getPropertyPath().toString()).thenReturn("user.age");

            ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleConstraintViolation(exception, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).hasSize(1);
            assertThat(response.getBody().errors().get(0).code()).isEqualTo("AIDJI-002");
            assertThat(response.getBody().errors().get(0).message()).isEqualTo("must be positive");
            assertThat(response.getBody().errors().get(0).field()).isEqualTo("age");
        }
    }

    @Nested
    @DisplayName("HTTP Exception Handlers")
    class HttpExceptionHandlerTests {

        @Test
        @DisplayName("should handle NoResourceFoundException with 404")
        void shouldHandleNotFound() {
            // Given
            NoResourceFoundException exception = mock(NoResourceFoundException.class);

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(exception, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).hasSize(1);
            assertThat(response.getBody().errors().get(0).code()).isEqualTo("AIDJI-003");
            assertThat(response.getBody().errors().get(0).message()).contains("/api/test");
        }

        @Test
        @DisplayName("should handle HttpRequestMethodNotSupportedException with 405")
        void shouldHandleMethodNotAllowed() {
            // Given
            HttpRequestMethodNotSupportedException exception =
                    new HttpRequestMethodNotSupportedException("POST");

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleMethodNotAllowed(exception, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).hasSize(1);
            assertThat(response.getBody().errors().get(0).message()).contains("POST");
        }

        @Test
        @DisplayName("should handle HttpMediaTypeNotSupportedException with 415")
        void shouldHandleMediaTypeNotSupported() {
            // Given
            HttpMediaTypeNotSupportedException exception =
                    new HttpMediaTypeNotSupportedException("Unsupported media type");

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleMediaTypeNotSupported(exception, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).hasSize(1);
        }

        @Test
        @DisplayName("should handle HttpMessageNotReadableException with 400")
        void shouldHandleMessageNotReadable() {
            // Given
            HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleMessageNotReadable(exception, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors().get(0).message()).contains("Malformed request body");
        }

        @Test
        @DisplayName("should handle MissingServletRequestParameterException")
        void shouldHandleMissingParameter() {
            // Given
            MissingServletRequestParameterException exception =
                    new MissingServletRequestParameterException("userId", "String");

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleMissingParameter(exception, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors().get(0).field()).isEqualTo("userId");
            assertThat(response.getBody().errors().get(0).message()).contains("userId");
        }

        @Test
        @DisplayName("should handle MethodArgumentTypeMismatchException")
        void shouldHandleTypeMismatch() {
            // Given
            MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
            when(exception.getName()).thenReturn("age");
            when(exception.getRequiredType()).thenReturn((Class) Integer.class);

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleTypeMismatch(exception, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors().get(0).field()).isEqualTo("age");
            assertThat(response.getBody().errors().get(0).message()).contains("Integer");
        }
    }

    @Nested
    @DisplayName("handleUnexpectedException")
    class HandleUnexpectedExceptionTests {

        @Test
        @DisplayName("should handle unexpected Exception with 500 status")
        void shouldHandleUnexpectedException() {
            // Given
            Exception exception = new RuntimeException("Unexpected error");

            // When
            ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpectedException(exception, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).hasSize(1);
            assertThat(response.getBody().errors().get(0).code()).isEqualTo("AIDJI-001");
            assertThat(response.getBody().errors().get(0).message()).isEqualTo("An unexpected error occurred");
            assertThat(response.getBody().errors().get(0).errorId()).isNotNull(); // Should generate errorId
        }

        @Test
        @DisplayName("should not include errorId when disabled")
        void shouldNotIncludeErrorIdWhenDisabled() {
            // Given
            AidjiWebProperties propsWithoutErrorId = new AidjiWebProperties(
                    new AidjiWebProperties.ExceptionHandlingProperties(false, false),
                    properties.restClient(),
                    properties.cors(),
                    properties.requestLogging()
            );
            GlobalExceptionHandler handlerWithoutErrorId = new GlobalExceptionHandler(propsWithoutErrorId);

            FunctionalException exception = FunctionalException.builder(CommonErrorCode.NOT_FOUND)
                    .message("Not found")
                    .build();

            // When
            ResponseEntity<ApiResponse<Void>> response =
                    handlerWithoutErrorId.handleBusinessException(exception, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors().get(0).errorId()).isNull();
        }
    }
}
