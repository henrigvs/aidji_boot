package be.aidji.boot.security.jwt;

import be.aidji.boot.security.AidjiSecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import static be.aidji.boot.core.exception.SecurityErrorCode.BEARER_TOKEN_EXPIRED;
import static be.aidji.boot.core.exception.SecurityErrorCode.BEARER_TOKEN_NOT_VALID;

/**
 * Provides JWT token generation, parsing, and validation capabilities.
 * <p>
 * This class handles all JWT operations using HMAC-SHA algorithms for signing.
 * Tokens include user authorities and support custom claims.
 *
 * @see AidjiSecurityProperties.JsonWebTokenProperties
 */
public class JwtTokenProvider {

    private final AidjiSecurityProperties.JsonWebTokenProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(AidjiSecurityProperties.JsonWebTokenProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = buildSecretKey();
    }

    // ========== Token Generation ==========

    /**
     * Generates a JWT token for a user with optional custom claims.
     *
     * @param userDetails  the user for whom the token is generated
     * @param customClaims additional claims to include (nullable)
     * @return a signed JWT token string
     */
    public String generateToken(@NonNull UserDetails userDetails, @Nullable Map<String, Object> customClaims) {
        long now = System.currentTimeMillis();
        Collection<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var builder = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("authorities", authorities)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.validityDurationInMs()))
                .signWith(secretKey);

        if (customClaims != null && !customClaims.isEmpty()) {
            builder.claims(customClaims);
        }

        return builder.compact();
    }

    /**
     * Generates a JWT token for a user without custom claims.
     */
    public String generateToken(@NonNull UserDetails userDetails) {
        return generateToken(userDetails, null);
    }

    /**
     * Generates an expired token, useful for invalidating httpOnly cookies on logout.
     */
    public String generateExpiredToken() {
        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(secretKey)
                .compact();
    }

    // ========== Token Parsing ==========

    /**
     * Extracts the username (subject) from a token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim using a resolver function.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    /**
     * Extracts all claims from a token.
     *
     * @throws be.aidji.boot.core.exception.SecurityException if token is invalid or expired
     */
    public Claims extractAllClaims(@NonNull String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new be.aidji.boot.core.exception.SecurityException(BEARER_TOKEN_EXPIRED, e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new be.aidji.boot.core.exception.SecurityException(BEARER_TOKEN_NOT_VALID, e);
        }
    }

    // ========== Token Validation ==========

    /**
     * Validates a token's signature and expiration.
     *
     * @return true if valid
     * @throws be.aidji.boot.core.exception.SecurityException if invalid or expired
     */
    public boolean isTokenValid(String token) {
        extractAllClaims(token); // Throws if invalid
        return true;
    }

    /**
     * Validates a token and checks if it belongs to the given user.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks if a token has expired without throwing an exception.
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (be.aidji.boot.core.exception.SecurityException e) {
            return true;
        }
    }

    // ========== Internal ==========

    private SecretKey buildSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.encryptionKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}