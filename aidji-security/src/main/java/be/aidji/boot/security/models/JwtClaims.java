/*
 * Copyright 2025 Henri GEVENOIS
 */
package be.aidji.boot.security.models;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents claims extracted from a JWT token.
 */
@Builder(toBuilder = true)
public record JwtClaims(
        String jti,
        String subject,
        String issuer,
        Instant issuedAt,
        Instant expiration,
        List<String> authorities,
        Map<String, Object> additionalClaims
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiration);
    }
}