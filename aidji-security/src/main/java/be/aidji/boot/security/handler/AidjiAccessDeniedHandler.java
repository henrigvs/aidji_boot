package be.aidji.boot.security.handler;

import be.aidji.boot.core.dto.ApiResponse;
import be.aidji.boot.core.dto.ApiResponse.ApiError;
import be.aidji.boot.core.exception.SecurityErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Handles access denied exceptions (403 Forbidden).
 * Returns a standardized JSON error response.
 */
public class AidjiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            @NonNull AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError error = ApiError.of(
                SecurityErrorCode.ACCESS_DENIED.getCode(),
                "Access denied: insufficient permissions"
        );

        objectMapper.writeValue(response.getOutputStream(), ApiResponse.failure(error));
    }
}