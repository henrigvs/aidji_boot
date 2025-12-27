package be.aidji.boot.security.handler;

import be.aidji.boot.core.dto.ApiResponse;
import be.aidji.boot.core.dto.ApiResponse.ApiError;
import be.aidji.boot.core.exception.SecurityErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Handles authentication failures (401 Unauthorized).
 * Returns a standardized JSON error response.
 */
public class AidjiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            @NonNull AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError error = ApiError.of(
                SecurityErrorCode.UNAUTHORIZED.getCode(),
                "Authentication required"
        );

        objectMapper.writeValue(response.getOutputStream(), ApiResponse.failure(error));
    }
}