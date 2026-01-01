package be.aidji.boot.security.webflux.config;

import be.aidji.boot.security.webflux.AidjiSecurityProperties;
import be.aidji.boot.security.webflux.handler.AidjiServerAccessDeniedHandler;
import be.aidji.boot.security.webflux.handler.AidjiServerAuthenticationEntryPoint;
import be.aidji.boot.security.webflux.jwt.JwtAuthenticationWebFilter;
import be.aidji.boot.security.webflux.jwt.JwtTokenVerificator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

/**
 * Auto-configuration for Aidji Security WebFlux.
 * <p>
 * Provides a pre-configured, opinionated security setup for reactive applications with:
 * <ul>
 *   <li>JWT-based stateless authentication (cookie or header)</li>
 *   <li>CORS configuration (uses aidji-web if available)</li>
 *   <li>CSRF disabled (stateless)</li>
 *   <li>Custom error handlers</li>
 *   <li>Method-level security enabled</li>
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties(AidjiSecurityProperties.class)
@ConditionalOnClass(SecurityWebFilterChain.class)
@ConditionalOnProperty(name = "aidji.security.enabled", havingValue = "true", matchIfMissing = true)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class AidjiSecurityWebFluxAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AidjiSecurityWebFluxAutoConfiguration.class);

    // ========== Core Beans ==========

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ========== JWT Beans ==========

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "aidji.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public JwtTokenVerificator jwtTokenVerificator(AidjiSecurityProperties properties) {
        return new JwtTokenVerificator(properties.jwt());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "aidji.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public JwtAuthenticationWebFilter jwtAuthenticationWebFilter(
            JwtTokenVerificator jwtTokenVerificator,
            ObjectProvider<ReactiveUserDetailsService> userDetailsServiceProvider,
            AidjiSecurityProperties properties) {

        ReactiveUserDetailsService userDetailsService = userDetailsServiceProvider.getIfAvailable(() -> {
            throw new IllegalStateException(
                    "No ReactiveUserDetailsService bean found. " +
                            "Please define a ReactiveUserDetailsService bean in your application."
            );
        });

        return new JwtAuthenticationWebFilter(
                jwtTokenVerificator,
                userDetailsService,
                properties.jwt(),
                properties.security()
        );
    }

    // ========== Error Handlers ==========

    @Bean
    @ConditionalOnMissingBean
    public AidjiServerAccessDeniedHandler aidjiServerAccessDeniedHandler() {
        return new AidjiServerAccessDeniedHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public AidjiServerAuthenticationEntryPoint aidjiServerAuthenticationEntryPoint() {
        return new AidjiServerAuthenticationEntryPoint();
    }

    // ========== Security Filter Chain ==========

    @Bean
    @Order(100)
    @ConditionalOnMissingBean
    public SecurityWebFilterChain aidjiSecurityWebFilterChain(
            ServerHttpSecurity http,
            AidjiSecurityProperties properties,
            JwtAuthenticationWebFilter jwtAuthenticationWebFilter,
            AidjiServerAccessDeniedHandler accessDeniedHandler,
            AidjiServerAuthenticationEntryPoint authenticationEntryPoint,
            ObjectProvider<CorsConfigurationSource> corsConfigurationSource,
            ObjectProvider<AidjiSecurityCustomizer> customizers) {

        String[] whitelist = properties.security().publicPaths().toArray(String[]::new);

        log.info("Aidji Security WebFlux initialized with {} public paths: {}", whitelist.length, String.join(", ", whitelist));
        log.info("JWT cookie-based: {}, cookie name: {}", properties.jwt().cookieBased(), properties.jwt().cookieName());

        // Base configuration
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint)
                );

        // CORS - use aidji-web config if available
        corsConfigurationSource.ifAvailable(cors ->
                http.cors(c -> c.configurationSource(cors))
        );

        // Authorization rules
        http.authorizeExchange(auth -> {
            auth.pathMatchers(whitelist).permitAll();
            auth.pathMatchers("/error").permitAll();
            auth.anyExchange().authenticated();
        });

        // JWT Filter
        http.addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        // Apply customizers
        customizers.orderedStream().forEach(customizer -> customizer.customize(http));

        return http.build();
    }
}
