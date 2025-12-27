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

import java.util.List;
import java.util.function.Function;

/**
 * Paginated response wrapper providing consistent pagination structure.
 *
 * @param <T> the type of the content items
 */
public record PageResponse<T>(
        List<T> content,
        PageInfo page
) {

    /**
     * Creates a PageResponse from content and pagination info.
     */
    public static <T> PageResponse<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
        PageInfo pageInfo = new PageInfo(
                pageNumber,
                pageSize,
                totalElements,
                totalPages,
                pageNumber == 0,
                pageNumber >= totalPages - 1
        );
        return new PageResponse<>(content, pageInfo);
    }

    /**
     * Creates a PageResponse from a Spring Data Page.
     * Use this when you have access to Spring Data.
     */
    public static <T> PageResponse<T> of(List<T> content, PageInfo pageInfo) {
        return new PageResponse<>(content, pageInfo);
    }

    /**
     * Maps the content to another type.
     */
    public <R> PageResponse<R> map(Function<T, R> mapper) {
        List<R> mappedContent = content.stream().map(mapper).toList();
        return new PageResponse<>(mappedContent, page);
    }

    /**
     * Returns true if there is a next page.
     */
    public boolean hasNext() {
        return !page.last();
    }

    /**
     * Returns true if there is a previous page.
     */
    public boolean hasPrevious() {
        return !page.first();
    }

    /**
     * Pagination metadata.
     */
    public record PageInfo(
            int number,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
    }
}
