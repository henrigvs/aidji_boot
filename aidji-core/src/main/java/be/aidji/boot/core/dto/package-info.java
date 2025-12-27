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

/**
 * Data Transfer Objects (DTOs) for standardized API responses.
 *
 * <p>This package provides wrapper classes for consistent API responses:</p>
 *
 * <ul>
 *   <li>{@link be.aidji.boot.core.dto.ApiResponse} - Generic wrapper for single-item responses</li>
 *   <li>{@link be.aidji.boot.core.dto.PageResponse} - Wrapper for paginated responses with metadata</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @GetMapping("/users/{id}")
 * public ApiResponse<User> getUser(@PathVariable Long id) {
 *     User user = userService.findById(id);
 *     return ApiResponse.success(user);
 * }
 *
 * @GetMapping("/users")
 * public PageResponse<User> getUsers(Pageable pageable) {
 *     Page<User> page = userService.findAll(pageable);
 *     return PageResponse.of(page);
 * }
 * }</pre>
 *
 * @see be.aidji.boot.core.dto.ApiResponse
 * @see be.aidji.boot.core.dto.PageResponse
 */
package be.aidji.boot.core.dto;