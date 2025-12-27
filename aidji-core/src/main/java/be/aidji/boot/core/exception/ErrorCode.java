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

/**
 * Contract for error codes used throughout the application.
 * Implement this interface in an enum to define your application-specific error codes.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public enum UserErrorCode implements ErrorCode {
 *     USER_NOT_FOUND("USER-001", "User not found", 404),
 *     USER_ALREADY_EXISTS("USER-002", "User already exists", 409);
 *
 *     private final String code;
 *     private final String defaultMessage;
 *     private final int httpStatus;
 *
 *     // constructor and getters...
 * }
 * }</pre>
 */
public interface ErrorCode {

    /**
     * Returns the unique error code identifier.
     * Convention: PREFIX-XXX (e.g., "USER-001", "AUTH-003")
     */
    String getCode();

    /**
     * Returns the default human-readable error message.
     */
    String getDefaultMessage();

    /**
     * Returns the suggested HTTP status code for this error.
     */
    int getHttpStatus();
}
