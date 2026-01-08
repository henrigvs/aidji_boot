/*
 * Copyright 2025 Henri GEVENOIS
 */
package be.aidji.boot.security.jwt;


import java.util.Map;

/**
 * Interface for JWT token generation and validation.
 *
 * <p>Two implementations available:</p>
 * <ul>
 *   <li>{@code standalone} - Local key pair</li>
 *   <li>{@code cipm} - Delegates to CIPM service (Vault-backed)</li>
 * </ul>
 */
public interface JwtTokenProvider {

    /**
     * Generates a signed JWT token.
     *
     * @param subject the subject (usually user ID)
     * @param claims  additional claims to include
     * @return signed JWT token string
     */
    String generateToken(String subject, Map<String, Object> claims);
}