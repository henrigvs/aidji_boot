package be.aidji.boot.security.jwt;

import be.aidji.boot.security.AidjiSecurityProperties;
import be.aidji.boot.security.jwt.cipm.JwtTokenVerificatorCipm;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Spring Security filter that processes JWT authentication for incoming HTTP requests.
 * <p>
 * Supports two modes of token extraction:
 * <ul>
 *   <li><b>Cookie-based</b> (recommended): Token stored in HTTP-only cookie</li>
 *   <li><b>Header-based</b>: Token in Authorization header (Bearer scheme)</li>
 * </ul>
 *
 * <p>When a {@link UserDetailsService} bean is available, the filter loads user details
 * from the application's store and uses them as the authentication principal.
 * When absent (e.g. in DELEGATED mode with an external IdP), the JWT subject is used
 * directly as the principal and authorities are extracted from the JWT claims.
 *
 * <p>Authentication failures are delegated to the configured
 * {@link AuthenticationEntryPoint}, so the response payload format remains under the
 * control of the consuming application.
 *
 * @see JwtTokenVerificatorCipm
 * @see AidjiSecurityProperties.JwtProperties
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenVerificator jwtTokenVerificator;
    @Nullable
    private final UserDetailsService userDetailsService;
    private final AidjiSecurityProperties properties;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(
            JwtTokenVerificator jwtTokenVerificator,
            @Nullable UserDetailsService userDetailsService,
            AidjiSecurityProperties properties,
            AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtTokenVerificator = jwtTokenVerificator;
        this.userDetailsService = userDetailsService;
        this.properties = properties;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            extractToken(request)
                    .filter(_ -> SecurityContextHolder.getContext().getAuthentication() == null)
                    .ifPresent(token -> authenticateToken(token, request));

        } catch (be.aidji.boot.core.exception.SecurityException e) {
            log.debug("JWT authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException(e.getMessage(), e));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from a cookie or Authorization header based on configuration.
     */
    private Optional<String> extractToken(HttpServletRequest request) {
        if (properties.jwt().cookieBased()) {
            Optional<String> cookieToken = extractTokenFromCookie(request);
            if (cookieToken.isPresent()) {
                return cookieToken;
            }
        }

        return extractTokenFromHeader(request);
    }

    private Optional<String> extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> properties.jwt().cookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private Optional<String> extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            return token.isBlank() ? Optional.empty() : Optional.of(token);
        }

        return Optional.empty();
    }

    /**
     * Validates token and sets up Spring Security context.
     */
    private void authenticateToken(String token, HttpServletRequest request) {
        Claims claims = jwtTokenVerificator.validateToken(token);
        String username = claims.getSubject();

        if (username == null) {
            log.debug("No subject found in JWT token");
            return;
        }

        Object principal = userDetailsService != null
                ? userDetailsService.loadUserByUsername(username)
                : username;

        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.debug("Authentication successful for user: {}", username);
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
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<SimpleGrantedAuthority> toAuthority(Object authority) {
        if (authority instanceof String str) {
            return Optional.of(new SimpleGrantedAuthority(str));
        }
        if (authority instanceof Map<?, ?> map) {
            Object value = map.get("authority");
            if (value != null) {
                return Optional.of(new SimpleGrantedAuthority(value.toString()));
            }
        }
        log.warn("Ignoring authority with unsupported format: {}", authority);
        return Optional.empty();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return properties.publicPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}