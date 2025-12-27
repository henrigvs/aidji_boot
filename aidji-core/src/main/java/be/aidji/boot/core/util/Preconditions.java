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
package be.aidji.boot.core.util;

import be.aidji.boot.core.exception.FunctionalException;
import be.aidji.boot.core.exception.CommonErrorCode;
import be.aidji.boot.core.exception.ErrorCode;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Utility class for common validation checks that throw appropriate exceptions.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Preconditions.requireNonNull(user, "User cannot be null");
 * Preconditions.requireNonEmpty(userId, "User ID is required");
 * Preconditions.require(age >= 18, UserErrorCode.UNDERAGE, "Must be 18 or older");
 * }</pre>
 */
public final class Preconditions {

    private Preconditions() {
        // Utility class
    }

    /**
     * Ensures that an object is not null.
     *
     * @throws FunctionalException with VALIDATION_ERROR if the object is null
     */
    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new FunctionalException(CommonErrorCode.VALIDATION_ERROR, message);
        }
        return obj;
    }

    /**
     * Ensures that an object is not null, using custom error code.
     */
    public static <T> T requireNonNull(T obj, ErrorCode errorCode, String message) {
        if (obj == null) {
            throw new FunctionalException(errorCode, message);
        }
        return obj;
    }

    /**
     * Ensures that a string is not null or empty.
     *
     * @throws FunctionalException with VALIDATION_ERROR if the string is null or empty
     */
    public static String requireNonEmpty(String str, String message) {
        if (str == null || str.isBlank()) {
            throw new FunctionalException(CommonErrorCode.VALIDATION_ERROR, message);
        }
        return str;
    }

    /**
     * Ensures that a string is not null or empty, using custom error code.
     */
    public static String requireNonEmpty(String str, ErrorCode errorCode, String message) {
        if (str == null || str.isBlank()) {
            throw new FunctionalException(errorCode, message);
        }
        return str;
    }

    /**
     * Ensures that a collection is not null or empty.
     *
     * @throws FunctionalException with VALIDATION_ERROR if the collection is null or empty
     */
    public static <T extends Collection<?>> T requireNonEmpty(T collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new FunctionalException(CommonErrorCode.VALIDATION_ERROR, message);
        }
        return collection;
    }

    /**
     * Ensures that a condition is true.
     *
     * @throws FunctionalException with VALIDATION_ERROR if the condition is false
     */
    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new FunctionalException(CommonErrorCode.VALIDATION_ERROR, message);
        }
    }

    /**
     * Ensures that a condition is true, using custom error code.
     */
    public static void require(boolean condition, ErrorCode errorCode, String message) {
        if (!condition) {
            throw new FunctionalException(errorCode, message);
        }
    }

    /**
     * Ensures that a condition is true, with lazy message evaluation.
     */
    public static void require(boolean condition, ErrorCode errorCode, Supplier<String> messageSupplier) {
        if (!condition) {
            throw new FunctionalException(errorCode, messageSupplier.get());
        }
    }

    /**
     * Throws NOT_FOUND exception if the object is null.
     */
    public static <T> T requireFound(T obj, String message) {
        if (obj == null) {
            throw new FunctionalException(CommonErrorCode.NOT_FOUND, message);
        }
        return obj;
    }

    /**
     * Throws NOT_FOUND exception if the object is null, with custom error code.
     */
    public static <T> T requireFound(T obj, ErrorCode errorCode, String message) {
        if (obj == null) {
            throw new FunctionalException(errorCode, message);
        }
        return obj;
    }
}
