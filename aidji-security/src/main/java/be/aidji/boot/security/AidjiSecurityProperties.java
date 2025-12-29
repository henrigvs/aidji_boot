package be.aidji.boot.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.security.autoconfigure.SecurityProperties;

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
        JwtProperties jsonWebTokenProperties,
        SecurityProperties securityProperties
) {

    public record JwtProperties(

            // Public key URL
            String publicKeyUrl,

            // Cache duration for public keys
            Long publicKeyCacheTtlSeconds,

            // Use HTTP-only cookie instead of Authorization header
            boolean cookieBased,

            // Cookie name when cookie-based auth is enabled
            String cookieName
    ) {
        public JwtProperties {
            if (publicKeyCacheTtlSeconds == null) {
                publicKeyCacheTtlSeconds = 3600L;
            }
            if (cookieName == null || cookieName.isBlank()) {
                cookieName = "jwt-security-principal";
            }
        }
    }

    public record SecurityProperties(
            // Paths that don't require authentication
            List<String> publicPaths
    ) {
        public SecurityProperties {
            if (publicPaths == null) {
                publicPaths = List.of("/api/auth/**", "/actuator/health");
            }
        }
    }
}
