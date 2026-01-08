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
import be.aidji.boot.web.exception.GlobalExceptionHandler;
import be.aidji.boot.web.filter.RequestLoggingFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AidjiWebAutoConfiguration}.
 */
@DisplayName("AidjiWebAutoConfiguration")
class AidjiWebAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AidjiWebAutoConfiguration.class));

    @Nested
    @DisplayName("Auto-configuration")
    class AutoConfigurationTests {

        @Test
        @DisplayName("should create all beans when enabled")
        void shouldCreateAllBeansWhenEnabled() {
            contextRunner
                    .run(context -> {
                        assertThat(context).hasSingleBean(AidjiWebProperties.class);
                        assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
                        assertThat(context).hasSingleBean(AidjiRestClientFactory.class);
                        assertThat(context).hasSingleBean(RestClient.Builder.class);
                    });
        }

        @Test
        @DisplayName("should create request logging filter by default")
        void shouldCreateRequestLoggingFilterByDefault() {
            contextRunner
                    .run(context -> {
                        assertThat(context).hasBean("aidjiRequestLoggingFilter");
                        assertThat(context).hasSingleBean(FilterRegistrationBean.class);
                    });
        }

        @Test
        @DisplayName("should not create request logging filter when disabled")
        void shouldNotCreateRequestLoggingFilterWhenDisabled() {
            contextRunner
                    .withPropertyValues("aidji.web.request-logging.enabled=false")
                    .run(context -> {
                        assertThat(context).doesNotHaveBean("aidjiRequestLoggingFilter");
                    });
        }
    }

    @Nested
    @DisplayName("CORS Configuration")
    class CorsConfigurationTests {

        @Test
        @DisplayName("should not create CORS configurer by default")
        void shouldNotCreateCorsConfigurerByDefault() {
            contextRunner
                    .run(context -> {
                        assertThat(context).doesNotHaveBean("aidjiCorsConfigurer");
                    });
        }

        @Test
        @DisplayName("should create CORS configurer when enabled")
        void shouldCreateCorsConfigurerWhenEnabled() {
            contextRunner
                    .withPropertyValues("aidji.web.cors.enabled=true")
                    .run(context -> {
                        assertThat(context).hasBean("aidjiCorsConfigurer");
                        assertThat(context).getBean("aidjiCorsConfigurer")
                                .isInstanceOf(WebMvcConfigurer.class);
                    });
        }
    }

    @Nested
    @DisplayName("Properties Configuration")
    class PropertiesConfigurationTests {

        @Test
        @DisplayName("should use default properties when not configured")
        void shouldUseDefaultPropertiesWhenNotConfigured() {
            contextRunner
                    .run(context -> {
                        AidjiWebProperties properties = context.getBean(AidjiWebProperties.class);
                        assertThat(properties).isNotNull();
                        assertThat(properties.exceptionHandling()).isNotNull();
                        assertThat(properties.restClient()).isNotNull();
                        assertThat(properties.cors()).isNotNull();
                        assertThat(properties.requestLogging()).isNotNull();
                    });
        }

        @Test
        @DisplayName("should override default properties")
        void shouldOverrideDefaultProperties() {
            contextRunner
                    .withPropertyValues(
                            "aidji.web.exception-handling.include-stacktrace=true",
                            "aidji.web.rest-client.connect-timeout=10s",
                            "aidji.web.request-logging.include-payload=true"
                    )
                    .run(context -> {
                        AidjiWebProperties properties = context.getBean(AidjiWebProperties.class);
                        assertThat(properties.exceptionHandling().includeStacktrace()).isTrue();
                        assertThat(properties.restClient().connectTimeout().getSeconds()).isEqualTo(10);
                        assertThat(properties.requestLogging().includePayload()).isTrue();
                    });
        }
    }

    @Nested
    @DisplayName("RestClient Configuration")
    class RestClientConfigurationTests {

        @Test
        @DisplayName("should create RestClient builder")
        void shouldCreateRestClientBuilder() {
            contextRunner
                    .run(context -> {
                        assertThat(context).hasSingleBean(RestClient.Builder.class);
                    });
        }

        @Test
        @DisplayName("should create RestClient factory")
        void shouldCreateRestClientFactory() {
            contextRunner
                    .run(context -> {
                        assertThat(context).hasSingleBean(AidjiRestClientFactory.class);
                        AidjiRestClientFactory factory = context.getBean(AidjiRestClientFactory.class);
                        assertThat(factory).isNotNull();
                    });
        }
    }
}
