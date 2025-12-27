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
import be.aidji.boot.core.dto.ApiResponse.ApiError;
import be.aidji.boot.core.dto.ApiResponse.ApiMetadata;
import be.aidji.boot.core.exception.AidjiException;
import be.aidji.boot.core.exception.FunctionalException;
import be.aidji.boot.core.exception.CommonErrorCode;
import be.aidji.boot.core.exception.TechnicalException;
import be.aidji.boot.web.AidjiWebProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Global exception handler that converts exceptions to standardized API responses.
 * 
 * <p>Handles:</p>
 * <ul>
 *   <li>Aidji exceptions (FunctionalException, TechnicalException)</li>
 *   <li>Spring validation exceptions</li>
 *   <li>Common HTTP exceptions (404, 405, 415)</li>
 *   <li>Unexpected exceptions (500)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final AidjiWebProperties.ExceptionHandlingProperties config;

    public GlobalExceptionHandler(AidjiWebProperties properties) {
        this.config = properties.exceptionHandling();
    }

    // ========== Aidji Exceptions ==========

    @ExceptionHandler(FunctionalException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            FunctionalException ex, HttpServletRequest request) {
        
        log.warn("Business exception: {}", ex.toLogString());
        
        return buildResponse(ex, request);
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ApiResponse<Void>> handleTechnicalException(
            TechnicalException ex, HttpServletRequest request) {
        
        log.error("Technical exception: {}", ex.toLogString(), ex);
        
        return buildResponse(ex, request);
    }

    // ========== Validation Exceptions ==========

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        List<ApiError> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String field = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
                    String message = error.getDefaultMessage();
                    return ApiError.of(CommonErrorCode.VALIDATION_ERROR.getCode(), message, field);
                })
                .toList();

        log.warn("Validation failed: {} errors", errors.size());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(null, buildMetadata(request, null), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        List<ApiError> errors = ex.getConstraintViolations().stream()
                .map(violation -> ApiError.of(
                        CommonErrorCode.VALIDATION_ERROR.getCode(),
                        violation.getMessage(),
                        extractFieldName(violation)
                ))
                .toList();

        log.warn("Constraint violation: {} errors", errors.size());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(null, buildMetadata(request, null), errors));
    }

    // ========== HTTP Exceptions ==========

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        
        log.debug("Resource not found: {}", request.getRequestURI());
        
        ApiError error = ApiError.of(
                CommonErrorCode.NOT_FOUND.getCode(),
                "Resource not found: " + request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(error));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        ApiError error = ApiError.of(
                CommonErrorCode.BAD_REQUEST.getCode(),
                "Method " + ex.getMethod() + " not supported"
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.failure(error));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        
        ApiError error = ApiError.of(
                CommonErrorCode.BAD_REQUEST.getCode(),
                "Media type not supported: " + ex.getContentType()
        );

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.failure(error));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        log.warn("Message not readable: {}", ex.getMessage());
        
        ApiError error = ApiError.of(
                CommonErrorCode.BAD_REQUEST.getCode(),
                "Malformed request body"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(error));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        ApiError error = ApiError.of(
                CommonErrorCode.VALIDATION_ERROR.getCode(),
                "Missing required parameter: " + ex.getParameterName(),
                ex.getParameterName()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(error));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(),
                Optional.ofNullable(ex.getRequiredType()).map(Class::getSimpleName).orElse("unknown"));
        
        ApiError error = ApiError.of(
                CommonErrorCode.VALIDATION_ERROR.getCode(),
                message,
                ex.getName()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(error));
    }

    // ========== Catch-all ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception ex, HttpServletRequest request) {
        
        String errorId = java.util.UUID.randomUUID().toString();
        log.error("Unexpected exception [errorId={}]: {}", errorId, ex.getMessage(), ex);

        ApiError error = ApiError.of(
                CommonErrorCode.INTERNAL_ERROR.getCode(),
                "An unexpected error occurred",
                null,
                config.includeErrorId() ? errorId : null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, buildMetadata(request, errorId), List.of(error)));
    }

    // ========== Helper Methods ==========

    private ResponseEntity<ApiResponse<Void>> buildResponse(AidjiException ex, HttpServletRequest request) {
        ApiError error = ApiError.of(
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                null,
                config.includeErrorId() ? ex.getErrorId() : null
        );

        ApiMetadata metadata = buildMetadata(request, ex.getErrorId());

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(new ApiResponse<>(null, metadata, List.of(error)));
    }

    private ApiMetadata buildMetadata(HttpServletRequest request, String errorId) {
        String traceId = request.getHeader("X-Trace-Id");
        return new ApiMetadata(Instant.now(), traceId, request.getRequestURI());
    }

    private String extractFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(lastDot + 1) : path;
    }
}
