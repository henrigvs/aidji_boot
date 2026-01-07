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

package be.aidji.boot.security.webflux.handler;

import be.aidji.boot.core.dto.ApiResponse;
import be.aidji.boot.core.dto.ApiResponse.ApiError;
import be.aidji.boot.core.exception.SecurityErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

/**
 * Handles authentication failures (401 Unauthorized) for WebFlux.
 * Returns a standardized JSON error response.
 */
@Slf4j
public class AidjiServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public @NonNull Mono<Void> commence(@NonNull ServerWebExchange exchange, @NonNull AuthenticationException ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            log.warn("Response already committed, cannot send 401 error for: {}", ex.getMessage());
            return Mono.empty();
        }

        return Mono.defer(() -> {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            } catch (UnsupportedOperationException e) {
                log.warn("Headers are read-only, could not set Content-Type: {}", e.getMessage());
            }

            ApiError error = ApiError.of(
                    SecurityErrorCode.UNAUTHORIZED.getCode(),
                    "Authentication required"
            );

            try {
                byte[] bytes = objectMapper.writeValueAsBytes(ApiResponse.failure(error));
                return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
            } catch (Exception e) {
                return Mono.error(e);
            }
        }).then(Mono.empty());
    }
}
