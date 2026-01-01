package be.aidji.boot.security.webflux.jwt;

import be.aidji.boot.security.webflux.AidjiPrincipal;
import be.aidji.boot.security.webflux.AidjiSecurityProperties;
import io.jsonwebtoken.Claims;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Reactive Spring Security filter that processes JWT authentication for incoming HTTP requests.
 * <p>
 * Supports two modes of token extraction:
 * <ul>
 *   <li><b>Cookie-based</b> (recommended): Token stored in HTTP-only cookie</li>
 *   <li><b>Header-based</b>: Token in Authorization header (Bearer scheme)</li>
 * </ul>
 *
 * @see JwtTokenVerificator
 * @see AidjiSecurityProperties.JwtProperties
 */
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationWebFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenVerificator jwtTokenVerificator;
    private final AidjiSecurityProperties.JwtProperties jwtProperties;
    private final AidjiSecurityProperties.SecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationWebFilter(
            JwtTokenVerificator jwtTokenVerificator,
            AidjiSecurityProperties.JwtProperties jwtProperties,
            AidjiSecurityProperties.SecurityProperties securityProperties) {
        this.jwtTokenVerificator = jwtTokenVerificator;
        this.jwtProperties = jwtProperties;
        this.securityProperties = securityProperties;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        // Skip filter for public paths
        String path = exchange.getRequest().getPath().value();
        if (shouldNotFilter(path)) {
            return chain.filter(exchange);
        }

        return extractToken(exchange.getRequest())
                .flatMap(this::authenticateToken)
                .flatMap(authentication ->
                        chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                )
                .onErrorResume(be.aidji.boot.core.exception.SecurityException.class, e -> {
                    log.debug("JWT authentication failed: {}", e.getMessage());
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    /**
     * Extracts JWT token from a cookie or Authorization header based on configuration.
     */
    private Mono<String> extractToken(ServerHttpRequest request) {
        if (jwtProperties.cookieBased()) {
            Mono<String> cookieToken = extractTokenFromCookie(request);
            return cookieToken.switchIfEmpty(extractTokenFromHeader(request));
        }

        return extractTokenFromHeader(request);
    }

    private Mono<String> extractTokenFromCookie(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst(jwtProperties.cookieName());
        if (cookie != null && !cookie.getValue().isBlank()) {
            return Mono.just(cookie.getValue());
        }
        return Mono.empty();
    }

    private Mono<String> extractTokenFromHeader(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return Mono.just(authHeader.substring(BEARER_PREFIX.length()));
        }

        return Mono.empty();
    }

    /**
     * Validates token and creates Spring Security Authentication.
     */
    private Mono<UsernamePasswordAuthenticationToken> authenticateToken(String token) {
        return Mono.fromCallable(() -> jwtTokenVerificator.validateToken(token))
                .flatMap(claims -> {
                    String username = claims.getSubject();
                    if (username == null) {
                        log.debug("No subject found in JWT token");
                        return Mono.empty();
                    }

                    var principal = new AidjiPrincipal(
                            username,
                            (String) claims.get("ipAddress"),
                            (String) claims.get("aud"),
                            claims.getIssuer(),
                            (String) claims.get("sessionId"),
                            extractAuthorities(claims),
                            (Map<String, Object>) claims.get("extraClaims")
                    );

                    var auth = new UsernamePasswordAuthenticationToken(
                            principal,
                            token,
                            principal.getAuthorities()
                    );
                    auth.setAuthenticated(true);

                    return Mono.just(auth);
                });
    }


    private Collection<? extends GrantedAuthority> mapAuthorities(List<String> authorities) {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    /**
     * Extracts authorities from JWT claims.
     * <p>
     * Supports two formats:
     * <ul>
     *   <li>List of strings: ["ROLE_USER", "ROLE_ADMIN"]</li>
     *   <li>List of objects: [{"authority": "ROLE_USER"}]</li>
     * </ul>
     */
    private Collection<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<?> authoritiesClaim = claims.get("authorities", List.class);

        if (authoritiesClaim == null || authoritiesClaim.isEmpty()) {
            return List.of();
        }

        return authoritiesClaim.stream()
                .map(this::toAuthority)
                .toList();
    }

    private SimpleGrantedAuthority toAuthority(Object authority) {
        if (authority instanceof String str) {
            return new SimpleGrantedAuthority(str);
        }
        if (authority instanceof java.util.Map<?, ?> map) {
            Object value = map.get("authority");
            if (value != null) {
                return new SimpleGrantedAuthority(value.toString());
            }
        }
        throw new IllegalArgumentException("Invalid authority format: " + authority);
    }

    private boolean shouldNotFilter(String path) {
        return securityProperties.publicPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
