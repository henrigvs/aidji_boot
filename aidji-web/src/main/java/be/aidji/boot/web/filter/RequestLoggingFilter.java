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
package be.aidji.boot.web.filter;

import be.aidji.boot.web.AidjiWebProperties.RequestLoggingProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that logs HTTP requests and adds correlation IDs.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Generates trace ID if not present in headers</li>
 *   <li>Logs request method, path, and duration</li>
 *   <li>Adds trace ID to MDC for log correlation</li>
 *   <li>Configurable path exclusions</li>
 * </ul>
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_TRACE_ID = "traceId";

    private final RequestLoggingProperties config;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RequestLoggingFilter(RequestLoggingProperties config) {
        this.config = config;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String traceId = extractOrGenerateTraceId(request);
        long startTime = System.currentTimeMillis();

        try {
            MDC.put(MDC_TRACE_ID, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);

            if (config.enabled()) {
                logRequest(request, traceId);
            }

            filterChain.doFilter(request, response);

        } finally {
            if (config.enabled()) {
                long duration = System.currentTimeMillis() - startTime;
                logResponse(request, response, traceId, duration);
            }
            MDC.remove(MDC_TRACE_ID);
        }
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String path = request.getRequestURI();
        return config.excludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String extractOrGenerateTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        return traceId;
    }

    private void logRequest(HttpServletRequest request, String traceId) {
        StringBuilder sb = new StringBuilder();
        sb.append("--> ").append(request.getMethod()).append(" ").append(request.getRequestURI());
        
        String queryString = request.getQueryString();
        if (queryString != null) {
            sb.append("?").append(queryString);
        }

        if (config.includeHeaders()) {
            sb.append(" [headers: ").append(extractHeaders(request)).append("]");
        }

        log.info(sb.toString());
    }

    private void logResponse(HttpServletRequest request, HttpServletResponse response, 
                            String traceId, long duration) {
        int status = response.getStatus();
        String level = status >= 500 ? "ERROR" : status >= 400 ? "WARN" : "INFO";
        
        String message = String.format("<-- %d %s %s (%dms)",
                status,
                request.getMethod(),
                request.getRequestURI(),
                duration);

        if (status >= 500) {
            log.error(message);
        } else if (status >= 400) {
            log.warn(message);
        } else {
            log.info(message);
        }
    }

    private String extractHeaders(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        var headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            // Skip sensitive headers
            if (name.equalsIgnoreCase("Authorization") || 
                name.equalsIgnoreCase("Cookie") ||
                name.equalsIgnoreCase("X-Api-Key")) {
                sb.append(name).append("=[REDACTED], ");
            } else {
                sb.append(name).append("=").append(request.getHeader(name)).append(", ");
            }
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove trailing ", "
        }
        return sb.toString();
    }
}
