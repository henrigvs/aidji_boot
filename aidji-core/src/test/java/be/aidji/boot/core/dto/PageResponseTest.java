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

import be.aidji.boot.core.dto.PageResponse.PageInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PageResponse}.
 */
@DisplayName("PageResponse")
class PageResponseTest {

    @Nested
    @DisplayName("of with pagination params")
    class OfWithParamsTests {

        @Test
        @DisplayName("should create page response with calculated pagination")
        void shouldCreateWithCalculatedPagination() {
            // Given
            List<String> content = List.of("item1", "item2", "item3");

            // When
            PageResponse<String> response = PageResponse.of(content, 0, 10, 25);

            // Then
            assertThat(response.content()).hasSize(3);
            assertThat(response.page().number()).isZero();
            assertThat(response.page().size()).isEqualTo(10);
            assertThat(response.page().totalElements()).isEqualTo(25);
            assertThat(response.page().totalPages()).isEqualTo(3);
            assertThat(response.page().first()).isTrue();
            assertThat(response.page().last()).isFalse();
        }

        @Test
        @DisplayName("should create first page correctly")
        void shouldCreateFirstPage() {
            // Given
            List<Integer> content = List.of(1, 2, 3);

            // When
            PageResponse<Integer> response = PageResponse.of(content, 0, 3, 10);

            // Then
            assertThat(response.page().first()).isTrue();
            assertThat(response.page().last()).isFalse();
            assertThat(response.hasPrevious()).isFalse();
            assertThat(response.hasNext()).isTrue();
        }

        @Test
        @DisplayName("should create last page correctly")
        void shouldCreateLastPage() {
            // Given
            List<Integer> content = List.of(10);

            // When
            PageResponse<Integer> response = PageResponse.of(content, 3, 3, 10);

            // Then
            assertThat(response.page().first()).isFalse();
            assertThat(response.page().last()).isTrue();
            assertThat(response.hasPrevious()).isTrue();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("should create middle page correctly")
        void shouldCreateMiddlePage() {
            // Given
            List<String> content = List.of("a", "b", "c");

            // When
            PageResponse<String> response = PageResponse.of(content, 1, 3, 10);

            // Then
            assertThat(response.page().number()).isEqualTo(1);
            assertThat(response.page().first()).isFalse();
            assertThat(response.page().last()).isFalse();
            assertThat(response.hasPrevious()).isTrue();
            assertThat(response.hasNext()).isTrue();
        }

        @Test
        @DisplayName("should handle single page correctly")
        void shouldHandleSinglePage() {
            // Given
            List<String> content = List.of("only");

            // When
            PageResponse<String> response = PageResponse.of(content, 0, 10, 1);

            // Then
            assertThat(response.page().totalPages()).isEqualTo(1);
            assertThat(response.page().first()).isTrue();
            assertThat(response.page().last()).isTrue();
            assertThat(response.hasPrevious()).isFalse();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("should handle empty page correctly")
        void shouldHandleEmptyPage() {
            // Given
            List<String> content = List.of();

            // When
            PageResponse<String> response = PageResponse.of(content, 0, 10, 0);

            // Then
            assertThat(response.content()).isEmpty();
            assertThat(response.page().totalElements()).isZero();
            assertThat(response.page().totalPages()).isZero();
            assertThat(response.page().first()).isTrue();
            assertThat(response.page().last()).isTrue();
        }

        @Test
        @DisplayName("should calculate total pages correctly with remainder")
        void shouldCalculateTotalPagesWithRemainder() {
            // Given
            List<Integer> content = List.of(1, 2, 3);

            // When
            PageResponse<Integer> response = PageResponse.of(content, 0, 3, 10);

            // Then
            // 10 items with page size 3 = 4 pages (3+3+3+1)
            assertThat(response.page().totalPages()).isEqualTo(4);
        }

        @Test
        @DisplayName("should handle zero page size")
        void shouldHandleZeroPageSize() {
            // Given
            List<String> content = List.of();

            // When
            PageResponse<String> response = PageResponse.of(content, 0, 0, 10);

            // Then
            assertThat(response.page().totalPages()).isZero();
        }
    }

    @Nested
    @DisplayName("of with PageInfo")
    class OfWithPageInfoTests {

        @Test
        @DisplayName("should create page response with provided PageInfo")
        void shouldCreateWithPageInfo() {
            // Given
            List<String> content = List.of("a", "b");
            PageInfo pageInfo = new PageInfo(1, 2, 10, 5, false, false);

            // When
            PageResponse<String> response = PageResponse.of(content, pageInfo);

            // Then
            assertThat(response.content()).hasSize(2);
            assertThat(response.page()).isEqualTo(pageInfo);
            assertThat(response.page().number()).isEqualTo(1);
            assertThat(response.page().size()).isEqualTo(2);
            assertThat(response.page().totalElements()).isEqualTo(10);
            assertThat(response.page().totalPages()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("map")
    class MapTests {

        @Test
        @DisplayName("should map content to different type")
        void shouldMapContent() {
            // Given
            List<Integer> content = List.of(1, 2, 3);
            PageResponse<Integer> response = PageResponse.of(content, 0, 3, 3);

            // When
            PageResponse<String> mapped = response.map(i -> "Item-" + i);

            // Then
            assertThat(mapped.content()).containsExactly("Item-1", "Item-2", "Item-3");
            assertThat(mapped.page()).isEqualTo(response.page());
        }

        @Test
        @DisplayName("should preserve page info when mapping")
        void shouldPreservePageInfoWhenMapping() {
            // Given
            List<String> content = List.of("a", "b");
            PageResponse<String> response = PageResponse.of(content, 2, 2, 10);

            // When
            PageResponse<Integer> mapped = response.map(String::length);

            // Then
            assertThat(mapped.content()).containsExactly(1, 1);
            assertThat(mapped.page().number()).isEqualTo(2);
            assertThat(mapped.page().size()).isEqualTo(2);
            assertThat(mapped.page().totalElements()).isEqualTo(10);
            assertThat(mapped.page().totalPages()).isEqualTo(5);
        }

        @Test
        @DisplayName("should map empty page")
        void shouldMapEmptyPage() {
            // Given
            PageResponse<String> response = PageResponse.of(List.of(), 0, 10, 0);

            // When
            PageResponse<Integer> mapped = response.map(String::length);

            // Then
            assertThat(mapped.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasNext")
    class HasNextTests {

        @Test
        @DisplayName("should return true when not on last page")
        void shouldReturnTrueWhenNotOnLastPage() {
            // Given
            PageResponse<String> response = PageResponse.of(List.of("a"), 0, 1, 5);

            // When / Then
            assertThat(response.hasNext()).isTrue();
        }

        @Test
        @DisplayName("should return false when on last page")
        void shouldReturnFalseWhenOnLastPage() {
            // Given
            PageResponse<String> response = PageResponse.of(List.of("a"), 4, 1, 5);

            // When / Then
            assertThat(response.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPrevious")
    class HasPreviousTests {

        @Test
        @DisplayName("should return false when on first page")
        void shouldReturnFalseWhenOnFirstPage() {
            // Given
            PageResponse<String> response = PageResponse.of(List.of("a"), 0, 1, 5);

            // When / Then
            assertThat(response.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("should return true when not on first page")
        void shouldReturnTrueWhenNotOnFirstPage() {
            // Given
            PageResponse<String> response = PageResponse.of(List.of("a"), 1, 1, 5);

            // When / Then
            assertThat(response.hasPrevious()).isTrue();
        }
    }

    @Nested
    @DisplayName("PageInfo")
    class PageInfoTests {

        @Test
        @DisplayName("should create page info with all fields")
        void shouldCreatePageInfo() {
            // When
            PageInfo pageInfo = new PageInfo(2, 10, 100, 10, false, false);

            // Then
            assertThat(pageInfo.number()).isEqualTo(2);
            assertThat(pageInfo.size()).isEqualTo(10);
            assertThat(pageInfo.totalElements()).isEqualTo(100);
            assertThat(pageInfo.totalPages()).isEqualTo(10);
            assertThat(pageInfo.first()).isFalse();
            assertThat(pageInfo.last()).isFalse();
        }
    }
}
