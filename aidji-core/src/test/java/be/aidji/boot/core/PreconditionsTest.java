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

package be.aidji.boot.core;

import be.aidji.boot.core.exception.CommonErrorCode;
import be.aidji.boot.core.exception.FunctionalException;
import be.aidji.boot.core.util.Preconditions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Preconditions")
public class PreconditionsTest {

    @Nested
    @DisplayName("requireNonNull")
    class RequireNonNullTests {

        @Test
        @DisplayName("should return object when non-null")
        void shouldReturnNonNullObject() {
            // Given
            String value = "test";

            // When
            String result = Preconditions.requireNonNull(value, "test");

            // Then
            assertThat(result).isEqualTo(value);
        }

        @Test
        @DisplayName("should throw FunctionalException with VALIDATION_ERROR when null")
        void shouldThrowExceptionWhenNull() {
            // When
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.requireNonNull(null, "test")
            );

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("should throw FunctionalException with custom error code when null")
        void shouldThrowExceptionWithCustomErrorCode() {
            // When
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.requireNonNull(null, CommonErrorCode.BAD_REQUEST, "test")
            );

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("requireNonEmpty (String)")
    class RequireNonEmptyStringTests {

        @Test
        @DisplayName("should return string when non-empty")
        void shouldReturnNonEmptyString() {
            // Given
            String value = "test";

            // When
            String result = Preconditions.requireNonEmpty(value, "test");

            // Then
            assertThat(result).isEqualTo(value);
        }

        @Test
        @DisplayName("should throw FunctionalException when empty string")
        void shouldThrowExceptionWhenEmpty() {
            // When
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.requireNonEmpty("", CommonErrorCode.VALIDATION_ERROR, "test")
            );

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("should throw FunctionalException when blank string")
        void shouldThrowExceptionWhenBlank() {
            // When
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.requireNonEmpty("   ", "test")
            );

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("should throw FunctionalException when null string")
        void shouldThrowExceptionWhenNull() {
            // When
            String nullString = null;
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.requireNonEmpty(nullString, CommonErrorCode.VALIDATION_ERROR, "test")
            );

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.VALIDATION_ERROR);
        }
    }

    @Nested
    @DisplayName("requireNonEmpty (Collection)")
    class RequireNonEmptyCollectionTests {

        @Test
        @DisplayName("should return collection when non-empty")
        void shouldReturnNonEmptyCollection() {
            // Given
            List<String> collection = List.of("item1", "item2");

            // When
            List<String> result = Preconditions.requireNonEmpty(collection, "Collection cannot be empty");

            // Then
            assertThat(result).isEqualTo(collection);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should throw FunctionalException when null collection")
        void shouldThrowExceptionWhenNullCollection() {
            // When
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.requireNonEmpty((List<String>) null, "Collection cannot be null")
            );

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("should throw FunctionalException when empty collection")
        void shouldThrowExceptionWhenEmptyCollection() {
            // When
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.requireNonEmpty(List.of(), "Collection cannot be empty")
            );

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.VALIDATION_ERROR);
        }
    }

    @Nested
    @DisplayName("require (boolean condition)")
    class RequireConditionTests {

        @Test
        @DisplayName("should not throw when condition is true")
        void shouldNotThrowWhenConditionTrue() {
            // When / Then
            assertDoesNotThrow(() -> Preconditions.require(true, "Condition failed"));
        }

        @Test
        @DisplayName("should throw FunctionalException with VALIDATION_ERROR when condition is false")
        void shouldThrowExceptionWhenConditionFalse() {
            // When
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.require(false, "Condition failed")
            );

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.VALIDATION_ERROR);
            assertThat(exception.getMessage()).contains("Condition failed");
        }

        @Test
        @DisplayName("should throw FunctionalException with custom error code when condition is false")
        void shouldThrowExceptionWithCustomErrorCode() {
            // When
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.require(false, CommonErrorCode.BAD_REQUEST, "Custom condition failed")
            );

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("Custom condition failed");
        }

        @Test
        @DisplayName("should support lazy message evaluation with Supplier")
        void shouldSupportLazyMessageEvaluation() {
            // Given
            final boolean[] supplierCalled = {false};

            // When - condition is true, supplier should NOT be called
            Preconditions.require(true, CommonErrorCode.VALIDATION_ERROR, () -> {
                supplierCalled[0] = true;
                return "Expensive message";
            });

            // Then
            assertThat(supplierCalled[0]).isFalse();
        }

        @Test
        @DisplayName("should evaluate Supplier when condition is false")
        void shouldEvaluateSupplierWhenConditionFalse() {
            // Given
            final boolean[] supplierCalled = {false};

            // When
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.require(false, CommonErrorCode.VALIDATION_ERROR, () -> {
                    supplierCalled[0] = true;
                    return "Expensive message";
                })
            );

            // Then
            assertThat(supplierCalled[0]).isTrue();
            assertThat(exception.getMessage()).contains("Expensive message");
        }
    }

    @Nested
    @DisplayName("requireFound")
    class RequireFoundTests {

        @Test
        @DisplayName("should return object when non-null")
        void shouldReturnNonNullObject() {
            // Given
            String value = "found";

            // When
            String result = Preconditions.requireFound(value, "Object not found");

            // Then
            assertThat(result).isEqualTo(value);
        }

        @Test
        @DisplayName("should throw FunctionalException with NOT_FOUND when null")
        void shouldThrowNotFoundExceptionWhenNull() {
            // When
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.requireFound(null, "User not found")
            );

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.NOT_FOUND);
            assertThat(exception.getMessage()).contains("User not found");
        }

        @Test
        @DisplayName("should throw FunctionalException with custom error code when null")
        void shouldThrowExceptionWithCustomErrorCode() {
            // When
            FunctionalException exception = assertThrows(
                FunctionalException.class,
                () -> Preconditions.requireFound(null, CommonErrorCode.BAD_REQUEST, "Resource not found")
            );

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("Resource not found");
        }
    }
}
