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
import be.aidji.boot.web.client.AidjiRestClientFactory;
import be.aidji.boot.web.client.RestClientCustomizer;
import be.aidji.boot.web.exception.GlobalExceptionHandler;
import be.aidji.boot.web.filter.RequestLoggingFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for Aidji Web components.
 * 
 * <p>Automatically configures:</p>
 * <ul>
 *   <li>Global exception handler</li>
 *   <li>Request logging filter</li>
 *   <li>CORS configuration</li>
 *   <li>RestClient factory</li>
 * </ul>
 *
 * <p>All components can be disabled or customized via properties:</p>
 * <pre>{@code
 * aidji:
 *   web:
 *     request-logging:
 *       enabled: false
 *     cors:
 *       enabled: true
 *       allowed-origins:
 *         - https://myapp.com
 * }</pre>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(AidjiWebProperties.class)
public class AidjiWebAutoConfiguration {

    // ========== Exception Handling ==========

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler aidjiGlobalExceptionHandler(AidjiWebProperties properties) {
        return new GlobalExceptionHandler(properties);
    }

    // ========== Request Logging ==========

    @Bean
    @ConditionalOnProperty(name = "aidji.web.request-logging.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RequestLoggingFilter> aidjiRequestLoggingFilter(AidjiWebProperties properties) {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter(properties.requestLogging()));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setName("aidjiRequestLoggingFilter");
        return registration;
    }

    // ========== CORS ==========

    @Bean
    @ConditionalOnProperty(name = "aidji.web.cors.enabled", havingValue = "true")
    public WebMvcConfigurer aidjiCorsConfigurer(AidjiWebProperties properties) {
        return new AidjiCorsConfiguration(properties.cors());
    }

    // ========== RestClient ==========

    @Bean
    @ConditionalOnMissingBean
    public AidjiRestClientFactory aidjiRestClientFactory(
            AidjiWebProperties properties,
            RestClient.Builder restClientBuilder,
            ObjectProvider<RestClientCustomizer> customizers) {
        
        // Apply all customizers
        customizers.orderedStream().forEach(c -> c.customize(restClientBuilder));
        
        return new AidjiRestClientFactory(properties.restClient(), restClientBuilder);
    }
}
