package be.aidji.boot.security.webflux.config;

import org.springframework.security.config.web.server.ServerHttpSecurity;

/**
 * Callback interface for customizing the ServerHttpSecurity configuration.
 * <p>
 * Implement this interface and register as a bean to add custom security rules
 * on top of Aidji's defaults.
 *
 * <p>Example:</p>
 * <pre>{@code
 * @Bean
 * public AidjiSecurityCustomizer mySecurityCustomizer() {
 *     return http -> http
 *         .authorizeExchange(auth -> auth
 *             .pathMatchers("/admin/**").hasRole("ADMIN")
 *         );
 * }
 * }</pre>
 */
@FunctionalInterface
public interface AidjiSecurityCustomizer {

    /**
     * Customize the ServerHttpSecurity configuration.
     *
     * @param http the ServerHttpSecurity to customize
     */
    void customize(ServerHttpSecurity http);
}
