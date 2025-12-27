package be.aidji.boot.security.config;

import be.aidji.boot.security.AidjiSecurityProperties;
import be.aidji.boot.security.handler.AidjiAccessDeniedHandler;
import be.aidji.boot.security.handler.AidjiAuthenticationEntryPoint;
import be.aidji.boot.security.jwt.JwtAuthenticationFilter;
import be.aidji.boot.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@AutoConfiguration
@EnableConfigurationProperties(AidjiSecurityProperties.class)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(name = "aidji.security.enabled", havingValue = "true", matchIfMissing = true)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class AidjiSecurityAutoConfiguration {

    // ========== Core Beans ==========

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    // ========== JWT Beans ==========

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "aidji.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public JwtTokenProvider jwtTokenProvider(AidjiSecurityProperties properties) {
        return new JwtTokenProvider(properties.jsonWebTokenProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "aidji.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            ObjectProvider<UserDetailsService> userDetailsServiceProvider,
            AidjiSecurityProperties properties) {

        UserDetailsService userDetailsService = userDetailsServiceProvider.getIfAvailable(() -> {
            throw new IllegalStateException(
                    "No UserDetailsService bean found. " +
                            "Please define a UserDetailsService bean in your application."
            );
        });

        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, properties.jsonWebTokenProperties());
    }

    // ========== Error Handlers ==========

    @Bean
    @ConditionalOnMissingBean
    public AidjiAccessDeniedHandler aidjiAccessDeniedHandler() {
        return new AidjiAccessDeniedHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public AidjiAuthenticationEntryPoint aidjiAuthenticationEntryPoint() {
        return new AidjiAuthenticationEntryPoint();
    }

    // ========== Security Filter Chain ==========

    @Bean
    @Order(100)
    @ConditionalOnMissingBean
    public SecurityFilterChain aidjiSecurityFilterChain(
            HttpSecurity http,
            AidjiSecurityProperties properties,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AidjiAccessDeniedHandler accessDeniedHandler,
            AidjiAuthenticationEntryPoint authenticationEntryPoint,
            ObjectProvider<CorsConfigurationSource> corsConfigurationSource,
            ObjectProvider<AidjiSecurityCustomizer> customizers) {

        // Base configuration
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint)
                );

        // CORS
        corsConfigurationSource.ifAvailable(cors ->
                http.cors(c -> c.configurationSource(cors))
        );

        // Authorization rules
        http.authorizeHttpRequests(auth -> {
            // Public paths
            properties.jsonWebTokenProperties().publicPaths().forEach(path ->
                    auth.requestMatchers(path).permitAll()
            );
            // Error endpoint always public
            auth.requestMatchers("/error").permitAll();
            // All others require authentication
            auth.anyRequest().authenticated();
        });

        // JWT Filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Apply customizers
        customizers.orderedStream().forEach(customizer -> {
            try {
                customizer.customize(http);
            } catch (Exception e) {
                throw new RuntimeException("Failed to apply security customizer", e);
            }
        });

        return http.build();
    }
}
