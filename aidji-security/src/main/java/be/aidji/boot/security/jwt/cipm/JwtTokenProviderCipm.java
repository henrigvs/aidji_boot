package be.aidji.boot.security.jwt.cipm;

import be.aidji.boot.core.exception.SecurityErrorCode;
import be.aidji.boot.core.exception.SecurityException;
import be.aidji.boot.security.AidjiSecurityProperties;
import be.aidji.boot.security.helpers.SecurityHelper;
import be.aidji.boot.security.jwt.JwtTokenProvider;
import be.aidji.boot.security.jwt.JwtTokenVerificator;
import be.aidji.boot.security.models.JwtClaims;
import be.aidji.boot.security.models.SignTokenRequest;
import be.aidji.boot.security.models.SignTokenResponse;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.UUID;

/**
 * CIPM-backed JWT provider.
 *
 * <p>Delegates token generation to CIPM service (Vault-backed) and
 * validates tokens using JWKS endpoint.</p>
 */
public class JwtTokenProviderCipm implements JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProviderCipm.class);

    private final RestClient restClient;
    private final AidjiSecurityProperties.CipmProperties cipmProperties;
    private final String issuer;
    private final long ttlSeconds;

    public JwtTokenProviderCipm(AidjiSecurityProperties securityProperties) {
        AidjiSecurityProperties.JwtProperties jwtProperties = securityProperties.jwt();
        this.cipmProperties = jwtProperties.cipmProperties();

        if (this.cipmProperties == null) {
            throw new IllegalStateException(
                    "CIPM properties are required when using CIPM mode. " +
                    "Please configure aidji.security.jwt.cipm-properties in your application properties."
            );
        }

        this.issuer = cipmProperties.issuer();
        this.ttlSeconds = jwtProperties.maxAge();

        this.restClient = RestClient.builder()
                .baseUrl(cipmProperties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + cipmProperties.apiToken())
                .defaultHeader("Content-Type", "application/json")
                .build();

        log.info("🔐 CIPM JWT provider initialized (url: {}, issuer: {})", cipmProperties.baseUrl(), issuer);
    }

    @Override
    public String generateToken(String subject, Map<String, Object> claims) {
        SignTokenRequest request = new SignTokenRequest(
                UUID.randomUUID().toString(),
                subject,
                issuer,
                ttlSeconds,
                claims
        );

        try {
            SignTokenResponse response = restClient.post()
                    .uri(cipmProperties.signTokenUri())
                    .body(request)
                    .retrieve()
                    .body(SignTokenResponse.class);

            if (response == null || response.token() == null) {
                throw SecurityException.builder(SecurityErrorCode.BEARER_TOKEN_NOT_VALID)
                        .message("CIPM returned empty token")
                        .build();
            }

            log.debug("Token generated via CIPM for subject: {}", subject);
            return response.token();

        } catch (RestClientException e) {
            throw SecurityException.builder(SecurityErrorCode.BEARER_TOKEN_NOT_VALID)
                    .message("Failed to generate token via CIPM: %s", e.getMessage())
                    .cause(e)
                    .build();
        }
    }
}