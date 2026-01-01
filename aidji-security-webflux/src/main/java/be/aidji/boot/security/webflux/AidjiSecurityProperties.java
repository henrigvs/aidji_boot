package be.aidji.boot.security.webflux;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for Aidji Security WebFlux module.
 *
 * <p>Example configuration:</p>
 * <pre>{@code
 * aidji:
 *   security:
 *     jwt:
 *       public-key-url: https://auth.example.com/.well-known/jwks.json
 *       public-key-cache-ttl-seconds: 3600
 *       cookie-based: true
 *       cookie-name: jwt-security-principal
 *     security:
 *       public-paths:
 *         - /api/auth/**
 *         - /actuator/health
 * }</pre>
 */
@ConfigurationProperties(prefix = "aidji.security")
public record AidjiSecurityProperties(
        JwtProperties jwt,
        SecurityProperties security
) {

    public record JwtProperties(

            // Public key URL
            String publicKeyUrl,

            // Cache duration for public keys
            Long publicKeyCacheTtlSeconds,

            // Use HTTP-only cookie instead of Authorization header
            boolean cookieBased,

            // Cookie name when cookie-based auth is enabled
            String cookieName,

            Long maxAge
    ) {
        public JwtProperties {
            if (publicKeyUrl == null || publicKeyUrl.isBlank()) {
                throw new IllegalArgumentException("aidji.security.jwt.public-key-url is required");
            }
            if (publicKeyCacheTtlSeconds == null) {
                publicKeyCacheTtlSeconds = 3600L;
            }
            if (cookieName == null || cookieName.isBlank()) {
                cookieName = "jwt-security-principal";
            }
            if(maxAge == null) {
                maxAge = 3600L;
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
