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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties({AidjiWebProperties.class})
public class AidjiWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler aidjiGlobalExceptionHandler(AidjiWebProperties properties) {
        return new GlobalExceptionHandler(properties);
    }

    @Bean
    @ConditionalOnProperty(
            name = "aidji.web.request-logging.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public FilterRegistrationBean<RequestLoggingFilter> aidjiRequestLoggingFilter(AidjiWebProperties properties) {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter(properties.requestLogging()));
        registration.setOrder(-2147483638);
        registration.setName("aidjiRequestLoggingFilter");
        return registration;
    }

    @Bean
    @ConditionalOnProperty(
            name = "aidji.web.cors.enabled",
            havingValue = "true"
    )
    public WebMvcConfigurer aidjiCorsConfigurer(AidjiWebProperties properties) {
        return new AidjiCorsConfiguration(properties.cors());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(RestClient.class)
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(RestClient.class)
    public AidjiRestClientFactory aidjiRestClientFactory(
            AidjiWebProperties properties,
            RestClient.Builder restClientBuilder,
            ObjectProvider<RestClientCustomizer> customizers) {
        customizers.orderedStream().forEach(c -> c.customize(restClientBuilder));
        return new AidjiRestClientFactory(properties.restClient(), restClientBuilder);
    }
}