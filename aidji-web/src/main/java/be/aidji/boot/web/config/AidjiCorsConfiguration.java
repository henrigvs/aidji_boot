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
package be.aidji.boot.web.config;

import be.aidji.boot.web.AidjiWebProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration based on Aidji properties.
 */
public class AidjiCorsConfiguration implements WebMvcConfigurer {

    private final AidjiWebProperties.CorsProperties corsProperties;

    public AidjiCorsConfiguration(AidjiWebProperties.CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        if (!corsProperties.enabled()) {
            return;
        }

        registry.addMapping("/**")
                .allowedOrigins(corsProperties.allowedOrigins().toArray(String[]::new))
                .allowedMethods(corsProperties.allowedMethods().toArray(String[]::new))
                .allowedHeaders(corsProperties.allowedHeaders().toArray(String[]::new))
                .allowCredentials(corsProperties.allowCredentials())
                .maxAge(corsProperties.maxAge().toSeconds());
    }
}