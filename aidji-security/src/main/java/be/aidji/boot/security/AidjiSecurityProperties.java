package be.aidji.boot.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for Aidji Security module.
 *
 * <p>Example configuration:</p>
 * <pre>{@code
 * aidji:
 *   security:
 *     json-web-token:
 *       encryption-key: "abcde"
 *       validity-duration-in-seconds: 600
 * }</pre>
 */
@ConfigurationProperties(prefix = "aidji.security")
public record AidjiSecurityProperties(
        JsonWebTokenProperties jsonWebTokenProperties
) {

    public record JsonWebTokenProperties(
            // Encryption key for JWT
            String encryptionKey,

            // Validity duration in MilliSeconds
            Long validityDurationInMs,

            //Use HTTP-only cookie instead of Authorization header
            boolean cookieBased,

            //Cookie name when cookie-based auth is enabled
            String cookieName,

            // Paths that don't require authentication
            List<String> publicPaths
    ) {
        public JsonWebTokenProperties {
            if (encryptionKey == null) {
                encryptionKey = "";
            }
            if (validityDurationInMs == null) {
                validityDurationInMs = 600L * 1000;
            }
            if (cookieName == null || cookieName.isBlank()) {
                cookieName = "jwt-token";
            }
            if (publicPaths == null) {
                publicPaths = List.of("/api/auth/**", "/actuator/health");
            }
        }
    }
}
