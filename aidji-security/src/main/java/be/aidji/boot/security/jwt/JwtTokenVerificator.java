package be.aidji.boot.security.jwt;

import be.aidji.boot.core.exception.CommonErrorCode;
import be.aidji.boot.core.exception.TechnicalException;
import be.aidji.boot.security.AidjiSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import module java.base;
import module java.net.http;

import static be.aidji.boot.core.exception.SecurityErrorCode.BEARER_TOKEN_EXPIRED;
import static be.aidji.boot.core.exception.SecurityErrorCode.BEARER_TOKEN_NOT_VALID;

/**
 * Validates JWT tokens signed with asymmetric keys (RS256, RS384, RS512).
 * <p>
 * Uses only native Java APIs - no external JSON library required.
 */
public class JwtTokenVerificator {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenVerificator.class);

    private final AidjiSecurityProperties.JwtProperties jwtProperties;
    private final HttpClient httpClient;

    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private volatile Instant lastFetchTime = Instant.EPOCH;

    public JwtTokenVerificator(AidjiSecurityProperties.JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Validates a JWT token and returns its claims.
     */
    public Claims validateToken(String token) {
        try {
            String kid = extractKid(token);
            PublicKey publicKey = getPublicKey(kid);

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

    /**
     * Checks if a token is valid without throwing exceptions.
     */
    public boolean isValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the key ID (kid) from the JWT header using native Base64.
     */
    private String extractKid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            String kid = extractJsonValue(headerJson, "kid");

            if (kid == null) {
                throw new IllegalArgumentException("No 'kid' in JWT header");
            }
            return kid;

        } catch (Exception e) {
            throw new be.aidji.boot.core.exception.SecurityException(
                    BEARER_TOKEN_NOT_VALID, "Cannot extract kid from token", e);
        }
    }

    private PublicKey getPublicKey(String kid) {
        PublicKey cachedKey = keyCache.get(kid);
        if (cachedKey != null && !isCacheExpired()) {
            return cachedKey;
        }

        synchronized (this) {
            cachedKey = keyCache.get(kid);
            if (cachedKey != null && !isCacheExpired()) {
                return cachedKey;
            }
            refreshKeyCache();
        }

        cachedKey = keyCache.get(kid);
        if (cachedKey == null) {
            throw new be.aidji.boot.core.exception.SecurityException(
                    BEARER_TOKEN_NOT_VALID, "Unknown key ID: " + kid);
        }

        return cachedKey;
    }

    private boolean isCacheExpired() {
        long ttlSeconds = jwtProperties.publicKeyCacheTtlSeconds();
        return Instant.now().isAfter(lastFetchTime.plusSeconds(ttlSeconds));
    }

    private void refreshKeyCache() {
        log.debug("Fetching JWKS from {}", jwtProperties.publicKeyUrl());

        try {
            String jwksJson = fetchJwks();
            List<Jwk> keys = parseJwks(jwksJson);

            keyCache.clear();
            for (Jwk jwk : keys) {
                if ("RSA".equals(jwk.kty) && "sig".equals(jwk.use)) {
                    PublicKey publicKey = buildRsaPublicKey(jwk);
                    keyCache.put(jwk.kid, publicKey);
                    log.debug("Cached public key: kid={}, alg={}", jwk.kid, jwk.alg);
                }
            }

            lastFetchTime = Instant.now();
            log.info("JWKS refreshed, {} keys cached", keyCache.size());

        } catch (Exception e) {
            throw new TechnicalException(
                    CommonErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Failed to fetch JWKS from " + jwtProperties.publicKeyUrl(),
                    e
            );
        }
    }

    private String fetchJwks() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(jwtProperties.publicKeyUrl()))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("JWKS fetch failed with status: " + response.statusCode());
        }

        return response.body();
    }

    /**
     * Parses JWKS JSON using regex - no external library needed.
     * Works for standard JWKS format from Keycloak, Auth0, Okta, etc.
     */
    private List<Jwk> parseJwks(String json) {
        List<Jwk> keys = new ArrayList<>();

        // Pattern to match each key object in the "keys" array
        Pattern keyPattern = Pattern.compile("\\{[^{}]*\"kty\"[^{}]*\\}");
        Matcher matcher = keyPattern.matcher(json);

        while (matcher.find()) {
            String keyJson = matcher.group();

            Jwk jwk = new Jwk(
                    extractJsonValue(keyJson, "kty"),
                    extractJsonValue(keyJson, "use"),
                    extractJsonValue(keyJson, "kid"),
                    extractJsonValue(keyJson, "alg"),
                    extractJsonValue(keyJson, "n"),
                    extractJsonValue(keyJson, "e")
            );

            if (jwk.kid != null && jwk.n != null && jwk.e != null) {
                keys.add(jwk);
            }
        }

        return keys;
    }

    /**
     * Extracts a string value from JSON using regex.
     */
    private String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private PublicKey buildRsaPublicKey(Jwk jwk) {
        try {
            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.n));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.e));

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return factory.generatePublic(spec);

        } catch (Exception e) {
            throw new TechnicalException(
                    CommonErrorCode.INTERNAL_ERROR,
                    "Failed to build RSA public key for kid: " + jwk.kid,
                    e
            );
        }
    }

    /**
     * Simple record to hold JWK data - no Jackson annotations needed.
     */
    private record Jwk(
            String kty,
            String use,
            String kid,
            String alg,
            String n,
            String e
    ) {}
}