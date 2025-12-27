package be.aidji.boot.security.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Callback interface for customizing the HttpSecurity configuration.
 * <p>
 * Implement this interface and register as a bean to add custom security rules
 * on top of Aidji's defaults.
 *
 * <p>Example:</p>
 * <pre>{@code
 * @Bean
 * public AidjiSecurityCustomizer mySecurityCustomizer() {
 *     return http -> http
 *         .authorizeHttpRequests(auth -> auth
 *             .requestMatchers("/admin/**").hasRole("ADMIN")
 *         );
 * }
 * }</pre>
 */
@FunctionalInterface
public interface AidjiSecurityCustomizer {

    /**
     * Customize the HttpSecurity configuration.
     *
     * @param http the HttpSecurity to customize
     * @throws Exception if an error occurs
     */
    void customize(HttpSecurity http) throws Exception;
}