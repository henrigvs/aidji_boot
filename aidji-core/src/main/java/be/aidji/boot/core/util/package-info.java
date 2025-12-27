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
 * Utility classes for common operations and validations.
 *
 * <p>This package provides helper classes for common tasks:</p>
 *
 * <ul>
 *   <li>{@link be.aidji.boot.core.util.Preconditions} - Validation helpers for method preconditions</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public void processUser(User user) {
 *     Preconditions.checkNotNull(user, "User cannot be null");
 *     Preconditions.checkArgument(user.getAge() >= 18, "User must be 18 or older");
 *     // Process user...
 * }
 * }</pre>
 *
 * @see be.aidji.boot.core.util.Preconditions
 */
package be.aidji.boot.core.util;