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

package be.aidji.boot.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for Aidji Security module.
 *
 * <p>Example configuration for CIPM mode (default):</p>
 * <pre>{@code
 * aidji:
 *   security:
 *     jwt:
 *       mode: cipm
 *       generation-enabled: true
 *       cookie-based: true
 *       cookie-name: jwt-security-principal
 *       max-age: 3600
 *       cipm-properties:
 *         base-url: https://cipm.example.com
 *         public-key-uri: /.well-known/jwks.json
 *         sign-token-uri: /api/sign-token
 *         api-token: ${CIPM_API_TOKEN}
 *         issuer: cipm-issuer
 *         jwks-cache-ttl-seconds: 3600
 *     security:
 *       public-paths:
 *         - /api/auth/**
 *         - /actuator/health
 * }</pre>
 *
 * <p>Example configuration for standalone mode:</p>
 * <pre>{@code
 * aidji:
 *   security:
 *     jwt:
 *       mode: standalone
 *       cookie-based: true
 *       cookie-name: jwt-security-principal
 *       max-age: 3600
 *       standalone:
 *         issuer: my-app
 *         key-size: 2048
 *         private-key: ${JWT_PRIVATE_KEY}
 *         public-key: ${JWT_PUBLIC_KEY}
 *     security:
 *       public-paths:
 *         - /api/auth/**
 *         - /actuator/health
 * }</pre>
 */
@ConfigurationProperties(prefix = "aidji.security")
public record AidjiSecurityProperties(
        Boolean enabled,
        List<String> publicPaths,
        JwtProperties jwt
) {
    public AidjiSecurityProperties {
        if (enabled == null) {
            enabled = false;
        }
        if (publicPaths == null) {
            publicPaths = List.of("/api/auth/**", "/actuator/health");
        }
    }

    /**
     * JWT authentication configuration properties.
     *
     * @param mode JWT mode: "cipm" for external CIPM service, "standalone" for self-contained JWT
     * @param generationEnabled Whether JWT generation is enabled (true = generate + validate, false = validate only)
     * @param cookieBased Use HTTP-only cookie instead of Authorization header
     * @param cookieName Cookie name when cookie-based auth is enabled (defaults to "jwt-security-principal")
     * @param maxAge Cookie max age in seconds (defaults to 3600)
     * @param standalone Standalone mode configuration (for mode="standalone")
     * @param cipmProperties CIPM mode configuration (for mode="cipm")
     */
    public record JwtProperties(
            String mode,
            boolean generationEnabled,
            boolean cookieBased,
            String cookieName,
            Long maxAge,
            StandaloneProperties standalone,
            CipmProperties cipmProperties
    ) {
        public JwtProperties {
            if (cookieName == null || cookieName.isBlank()) {
                cookieName = "jwt-security-principal";
            }
            if (maxAge == null) {
                maxAge = 3600L;
            }
        }
    }

    /**
     * Standalone mode JWT configuration properties.
     *
     * @param issuer JWT issuer identifier (defaults to "aidji-boot-app")
     * @param keySize RSA key size in bits (defaults to 2048)
     * @param privateKey Private key content (PEM or Base64 encoded)
     * @param publicKey Public key content (PEM or Base64 encoded)
     */
    public record StandaloneProperties(
            String issuer,
            int keySize,
            String privateKey,
            String publicKey
    ) {
        public StandaloneProperties {
            if (issuer == null || issuer.isBlank()) {
                issuer = "aidji-boot-app";
            }
            if (keySize <= 0) {
                keySize = 2048;
            }
        }

        public boolean hasKeys() {
            return privateKey != null && !privateKey.isBlank()
                    && publicKey != null && !publicKey.isBlank();
        }
    }

    /**
     * CIPM mode JWT configuration properties.
     *
     * @param baseUrl Base URL of the CIPM service
     * @param publicKeyUri URI path to the JWKS endpoint (e.g., "/.well-known/jwks.json")
     * @param signTokenUri URI path to the token signing endpoint (e.g., "/api/sign-token")
     * @param apiToken API token for authenticating with CIPM service
     * @param issuer JWT issuer identifier
     * @param jwksCacheTtlSeconds JWKS cache TTL in seconds (defaults to 3600)
     */
    public record CipmProperties(
            String baseUrl,
            String publicKeyUri,
            String signTokenUri,
            String apiToken,
            String issuer,
            long jwksCacheTtlSeconds
    ) {
        public CipmProperties {
            if (jwksCacheTtlSeconds <= 0) {
                jwksCacheTtlSeconds = 3600L;
            }
        }

        public String getPublicKeyUrl() {
            return baseUrl + publicKeyUri;
        }
        public String getSignTokenUrl() {
            return baseUrl + signTokenUri;
        }
    }
}
