/*
 * Copyright 2025 Henri GEVENOIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.aidji.boot.security.config;

import be.aidji.boot.security.AidjiSecurityProperties;
import be.aidji.boot.security.handler.AidjiAccessDeniedHandler;
import be.aidji.boot.security.handler.AidjiAuthenticationEntryPoint;
import be.aidji.boot.security.jwt.JwtAuthenticationFilter;
import be.aidji.boot.security.jwt.JwtTokenVerificator;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Auto-configuration for Aidji Security.
 * <p>
 * Provides a pre-configured, opinionated security setup with:
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
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(name = "aidji.security.enabled", havingValue = "true", matchIfMissing = true)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class AidjiSecurityAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AidjiSecurityAutoConfiguration.class);

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
    public JwtTokenVerificator jwtTokenVerificator(AidjiSecurityProperties properties) {
        return new JwtTokenVerificator(properties.jwt());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "aidji.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenVerificator jwtTokenVerificator,
            ObjectProvider<UserDetailsService> userDetailsServiceProvider,
            AidjiSecurityProperties properties) {

        UserDetailsService userDetailsService = userDetailsServiceProvider.getIfAvailable(() -> {
            throw new IllegalStateException(
                    "No UserDetailsService bean found. " +
                            "Please define a UserDetailsService bean in your application."
            );
        });

        return new JwtAuthenticationFilter(
                jwtTokenVerificator,
                userDetailsService,
                properties.jwt(),
                properties.security()
        );
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

        String[] whitelist = properties.security().publicPaths().toArray(String[]::new);

        log.info("Aidji Security initialized with {} public paths: {}", whitelist.length, String.join(", ", whitelist));
        log.info("JWT cookie-based: {}, cookie name: {}", properties.jwt().cookieBased(), properties.jwt().cookieName());

        // Base configuration
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .anonymous(Customizer.withDefaults()) // Enable anonymous authentication for public paths
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint)
                );

        // CORS - use aidji-web config if available
        corsConfigurationSource.ifAvailable(cors ->
                http.cors(c -> c.configurationSource(cors))
        );

        // Authorization rules
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(whitelist).permitAll();
            auth.requestMatchers("/error").permitAll();
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