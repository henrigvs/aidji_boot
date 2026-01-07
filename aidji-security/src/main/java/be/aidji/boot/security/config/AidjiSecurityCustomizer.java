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