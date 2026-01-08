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

package be.aidji.boot.security.jwt.stand_alone;

import be.aidji.boot.security.jwt.JwtTokenProvider;
import be.aidji.boot.security.jwt.JwtTokenVerificator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static be.aidji.boot.core.exception.SecurityErrorCode.BEARER_TOKEN_EXPIRED;
import static be.aidji.boot.core.exception.SecurityErrorCode.BEARER_TOKEN_NOT_VALID;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Standalone JWT provider using RSA key pair.
 *
 * <p>Keys can be provided via environment variables in two formats:</p>
 * <ul>
 *   <li>Base64-encoded PEM content</li>
 *   <li>Raw PEM content (with newlines as \n)</li>
 * </ul>
 *
 * <h2>Environment Variables</h2>
 * <pre>{@code
 * # Generate keys
 * openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048
 * openssl rsa -in private.pem -pubout -out public.pem
 *
 * # Export as Base64 (recommended)
 * export JWT_PRIVATE_KEY=$(cat private.pem | base64 | tr -d '\n')
 * export JWT_PUBLIC_KEY=$(cat public.pem | base64 | tr -d '\n')
 * }</pre>
 */
@Slf4j
@Getter
public class JwtTokenProviderStandAlone implements JwtTokenProvider, JwtTokenVerificator {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String issuer;
    private final String keyId;
    private final Long ttlSeconds;

    /**
     * Creates a provider with keys from environment variables.
     *
     * @param issuer          JWT issuer claim
     * @param privateKeyValue PEM content (raw or Base64-encoded)
     * @param publicKeyValue  PEM content (raw or Base64-encoded)
     */
    public JwtTokenProviderStandAlone(String issuer, String privateKeyValue, String publicKeyValue, Long ttlSeconds) {
        this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
        this.keyId = UUID.randomUUID().toString();

        try {
            this.privateKey = parsePrivateKey(privateKeyValue);
            this.publicKey = parsePublicKey(publicKeyValue);
            log.info("Standalone JWT provider initialized with environment keys (issuer: {}, kid: {})",
                    issuer, keyId);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse JWT keys from environment", e);
        }
    }

    /**
     * Creates a provider with auto-generated keys (development only).
     *
     * <p><strong>WARNING:</strong> Keys change on restart, invalidating all tokens!</p>
     */
    public JwtTokenProviderStandAlone(String issuer, int keySize, Long ttlSeconds) {
        this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
        this.keyId = UUID.randomUUID().toString();

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            log.warn("⚠️  Standalone JWT provider using AUTO-GENERATED keys. " +
                    "Tokens invalidated on restart! Set JWT_PRIVATE_KEY and JWT_PUBLIC_KEY for production.");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA algorithm not available", e);
        }
    }

    @Override
    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (ttlSeconds * 1000));

        return Jwts.builder()
                .header()
                .keyId(keyId)
                .and()
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .claims(claims)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    @Override
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new be.aidji.boot.core.exception.SecurityException(BEARER_TOKEN_EXPIRED, "Token expired", e);
        } catch (JwtException e) {
            throw new be.aidji.boot.core.exception.SecurityException(BEARER_TOKEN_NOT_VALID, "Invalid token", e);
        }
    }

    // ========== Key Parsing ==========

    private PrivateKey parsePrivateKey(String value) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = decodeToPem(value);
        String keyContent = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey parsePublicKey(String value) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = decodeToPem(value);
        String keyContent = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    /**
     * Handles both raw PEM and Base64-encoded PEM.
     */
    private String decodeToPem(String value) {
        // If starts with "-----BEGIN", it's raw PEM
        if (value.trim().startsWith("-----BEGIN")) {
            return value;
        }

        // Otherwise, assume Base64-encoded PEM
        try {
            byte[] decoded = Base64.getDecoder().decode(value.trim());
            return new String(decoded);
        } catch (IllegalArgumentException e) {
            // Not valid Base64, return as-is
            return value;
        }
    }
}
