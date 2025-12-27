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
package be.aidji.boot.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for Aidji Boot Web module.
 *
 * <p>Example configuration:</p>
 * <pre>{@code
 * aidji:
 *   web:
 *     exception-handling:
 *       include-stacktrace: false
 *       include-error-id: true
 *     rest-client:
 *       connect-timeout: 5s
 *       read-timeout: 30s
 *     cors:
 *       enabled: true
 *       allowed-origins:
 *         - http://localhost:3000
 * }</pre>
 */
@ConfigurationProperties(prefix = "aidji.web")
public record AidjiWebProperties(
        ExceptionHandlingProperties exceptionHandling,
        RestClientProperties restClient,
        CorsProperties cors,
        RequestLoggingProperties requestLogging
) {

    public AidjiWebProperties {
        if (exceptionHandling == null) {
            exceptionHandling = new ExceptionHandlingProperties(false, true);
        }
        if (restClient == null) {
            restClient = new RestClientProperties(Duration.ofSeconds(5), Duration.ofSeconds(30), true);
        }
        if (cors == null) {
            cors = new CorsProperties(false, List.of("*"), List.of("*"), List.of("*"), true, Duration.ofHours(1));
        }
        if (requestLogging == null) {
            requestLogging = new RequestLoggingProperties(true, false, false, List.of("/actuator/**"));
        }
    }

    /**
     * Exception handling configuration.
     */
    public record ExceptionHandlingProperties(
            //Include stack traces in error responses. Should be false in production.
            boolean includeStacktrace,

            //Include error ID in responses for log correlation.
            boolean includeErrorId
    ) {
        public ExceptionHandlingProperties {
            // defaults handled in parent record
        }
    }

    /**
     * RestClient configuration.
     */
    public record RestClientProperties(
            // Connection timeout for HTTP clients.
            Duration connectTimeout,

            // Read timeout for HTTP clients.
            Duration readTimeout,

            // Enable request/response logging for RestClient.
            boolean loggingEnabled
    ) {
        public RestClientProperties {
            if (connectTimeout == null) {
                connectTimeout = Duration.ofSeconds(5);
            }
            if (readTimeout == null) {
                readTimeout = Duration.ofSeconds(30);
            }
        }
    }

    /**
     * CORS configuration.
     */
    public record CorsProperties(
            // Enable CORS support.
            boolean enabled,

            // Allowed origins (e.g., http://localhost:3000).
            List<String> allowedOrigins,

            // Allowed HTTP methods.
            List<String> allowedMethods,

            // Allowed headers.
            List<String> allowedHeaders,

            // Allow credentials (cookies, authorization headers).
            boolean allowCredentials,

            // Max age for preflight cache.
            Duration maxAge
    ) {
        public CorsProperties {
            if (allowedOrigins == null) {
                allowedOrigins = List.of("*");
            }
            if (allowedMethods == null) {
                allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
            }
            if (allowedHeaders == null) {
                allowedHeaders = List.of("*");
            }
            if (maxAge == null) {
                maxAge = Duration.ofHours(1);
            }
        }
    }

    /**
     * Request logging configuration.
     */
    public record RequestLoggingProperties(
            // Enable request logging.
            boolean enabled,

            // Include the request body in logs. Be careful with sensitive data.
            boolean includePayload,

            // Include headers in logs.
            boolean includeHeaders,

            // Paths to exclude from logging (e.g., /actuator/**).
            List<String> excludePaths
    ) {
        public RequestLoggingProperties {
            if (excludePaths == null) {
                excludePaths = List.of("/actuator/**", "/health/**");
            }
        }
    }
}
