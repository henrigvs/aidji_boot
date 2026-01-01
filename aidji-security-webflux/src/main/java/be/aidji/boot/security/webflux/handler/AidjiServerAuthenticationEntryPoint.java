package be.aidji.boot.security.webflux.handler;

import be.aidji.boot.core.dto.ApiResponse;
import be.aidji.boot.core.dto.ApiResponse.ApiError;
import be.aidji.boot.core.exception.SecurityErrorCode;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

/**
 * Handles authentication failures (401 Unauthorized) for WebFlux.
 * Returns a standardized JSON error response.
 */
public class AidjiServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public @NonNull Mono<Void> commence(@NonNull ServerWebExchange exchange, @NonNull AuthenticationException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiError error = ApiError.of(
                SecurityErrorCode.UNAUTHORIZED.getCode(),
                "Authentication required"
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(ApiResponse.failure(error));
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
