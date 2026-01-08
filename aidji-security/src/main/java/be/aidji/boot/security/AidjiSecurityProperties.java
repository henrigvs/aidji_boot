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
        JwtProperties jwt,
        SecurityProperties security
) {

    public record JwtProperties(

            // The way the application going to handle the jwt
            String mode,

            // Indicates if the generation of JWT is enabled in the application
            boolean generationEnabled, // true = generate + validate, false = validate only

            // Use HTTP-only cookie instead of Authorization header
            boolean cookieBased,

            // Cookie name when cookie-based auth is enabled
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

    public record StandaloneProperties(
            String issuer,
            int keySize,
            String privateKey,  // PEM content ou Base64
            String publicKey    // PEM content ou Base64
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
