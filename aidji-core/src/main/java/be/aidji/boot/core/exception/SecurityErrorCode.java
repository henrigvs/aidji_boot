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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SecurityErrorCode implements ErrorCode {

    BEARER_TOKEN_EXPIRED("SECU-001", "Bearer token expired", 401),
    BEARER_TOKEN_NOT_VALID("SECU-002", "Bearer token not valid", 401),
    ACCESS_DENIED("SECU-003", "Access denied", 403),
    UNAUTHORIZED("SECU-004", "Unauthorized", 403);


    private final String code;
    private final String defaultMessage;
    private final int httpStatus;
}
