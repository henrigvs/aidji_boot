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

package be.aidji.boot.security.jwt;

import io.jsonwebtoken.Claims;

public interface JwtTokenVerificator {

    /**
     * Validates a JWT token and returns claims if valid.
     *
     * @param token the JWT token to validate
     * @return claims from the token
     * @throws SecurityException if token is invalid or expired
     */
    Claims validateToken(String token);

    /**
     * Checks if a token is valid without throwing exceptions.
     *
     * @param token the JWT token to check
     * @return true if valid, false otherwise
     */
    default boolean isValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
