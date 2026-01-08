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
package be.aidji.boot.observability.config;

import be.aidji.boot.observability.properties.AidjiObservabilityProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AidjiObservabilityAutoConfiguration}.
 */
@DisplayName("AidjiObservabilityAutoConfiguration")
class AidjiObservabilityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AidjiObservabilityAutoConfiguration.class));

    @Nested
    @DisplayName("Auto-configuration")
    class AutoConfigurationTests {

        @Test
        @DisplayName("should create all beans when enabled")
        void shouldCreateAllBeansWhenEnabled() {
            contextRunner
                    .withPropertyValues(
                            "aidji.observability.enabled=true",
                            "aidji.observability.service-name=test-service",
                            "aidji.observability.service-version=1.0.0",
                            "aidji.observability.tracing.enabled=true",
                            "aidji.observability.exporters.otlp.enabled=true"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(Resource.class);
                        assertThat(context).hasSingleBean(SdkTracerProvider.class);
                        assertThat(context).hasSingleBean(OpenTelemetry.class);
                        assertThat(context).hasSingleBean(Tracer.class);
                    });
        }

        @Test
        @DisplayName("should not create beans when disabled")
        void shouldNotCreateBeansWhenDisabled() {
            contextRunner
                    .withPropertyValues("aidji.observability.enabled=false")
                    .run(context -> {
                        assertThat(context).doesNotHaveBean(Resource.class);
                        assertThat(context).doesNotHaveBean(SdkTracerProvider.class);
                        assertThat(context).doesNotHaveBean(OpenTelemetry.class);
                        assertThat(context).doesNotHaveBean(Tracer.class);
                    });
        }

        @Test
        @DisplayName("should create beans with default configuration")
        void shouldCreateBeansWithDefaultConfiguration() {
            contextRunner
                    .run(context -> {
                        assertThat(context).hasSingleBean(Resource.class);
                        assertThat(context).hasSingleBean(AidjiObservabilityProperties.class);

                        AidjiObservabilityProperties properties = context.getBean(AidjiObservabilityProperties.class);
                        // enabled is false by default (primitive boolean)
                        assertThat(properties.serviceName()).isEqualTo("aidji-application");
                        assertThat(properties.serviceVersion()).isEqualTo("1.0.0");
                        assertThat(properties.environment()).isEqualTo("development");
                    });
        }
    }

    @Nested
    @DisplayName("Resource Configuration")
    class ResourceConfigurationTests {

        @Test
        @DisplayName("should create resource with service metadata")
        void shouldCreateResourceWithServiceMetadata() {
            contextRunner
                    .withPropertyValues(
                            "aidji.observability.service-name=my-service",
                            "aidji.observability.service-version=2.0.0",
                            "aidji.observability.environment=production"
                    )
                    .run(context -> {
                        Resource resource = context.getBean(Resource.class);
                        assertThat(resource).isNotNull();

                        AidjiObservabilityProperties properties = context.getBean(AidjiObservabilityProperties.class);
                        assertThat(properties.serviceName()).isEqualTo("my-service");
                        assertThat(properties.serviceVersion()).isEqualTo("2.0.0");
                        assertThat(properties.environment()).isEqualTo("production");
                    });
        }

        @Test
        @DisplayName("should use default values when not configured")
        void shouldUseDefaultValuesWhenNotConfigured() {
            contextRunner
                    .run(context -> {
                        Resource resource = context.getBean(Resource.class);
                        assertThat(resource).isNotNull();

                        AidjiObservabilityProperties properties = context.getBean(AidjiObservabilityProperties.class);
                        assertThat(properties.serviceName()).isEqualTo("aidji-application");
                        assertThat(properties.serviceVersion()).isEqualTo("1.0.0");
                        assertThat(properties.environment()).isEqualTo("development");
                    });
        }
    }

    @Nested
    @DisplayName("Tracer Configuration")
    class TracerConfigurationTests {

        @Test
        @DisplayName("should create tracer when tracing enabled")
        void shouldCreateTracerWhenTracingEnabled() {
            contextRunner
                    .withPropertyValues(
                            "aidji.observability.tracing.enabled=true",
                            "aidji.observability.service-name=test-service"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(Tracer.class);

                        Tracer tracer = context.getBean(Tracer.class);
                        assertThat(tracer).isNotNull();
                    });
        }

        @Test
        @DisplayName("should not create tracer when tracing disabled")
        void shouldNotCreateTracerWhenTracingDisabled() {
            contextRunner
                    .withPropertyValues("aidji.observability.tracing.enabled=false")
                    .run(context -> {
                        assertThat(context).doesNotHaveBean(SdkTracerProvider.class);
                        assertThat(context).doesNotHaveBean(Tracer.class);
                    });
        }

        @Test
        @DisplayName("should respect sampling probability")
        void shouldRespectSamplingProbability() {
            contextRunner
                    .withPropertyValues(
                            "aidji.observability.tracing.enabled=true",
                            "aidji.observability.tracing.sampling-probability=0.5"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(SdkTracerProvider.class);

                        AidjiObservabilityProperties properties = context.getBean(AidjiObservabilityProperties.class);
                        assertThat(properties.tracing().samplingProbability()).isEqualTo(0.5);
                    });
        }
    }

    @Nested
    @DisplayName("Exporter Configuration")
    class ExporterConfigurationTests {

        @Test
        @DisplayName("should enable OTLP exporter by default")
        void shouldEnableOtlpExporterByDefault() {
            contextRunner
                    .run(context -> {
                        AidjiObservabilityProperties properties = context.getBean(AidjiObservabilityProperties.class);
                        assertThat(properties.exporters().otlp().enabled()).isTrue();
                        assertThat(properties.exporters().otlp().endpoint()).isEqualTo("http://localhost:4317");
                    });
        }

        @Test
        @DisplayName("should allow custom OTLP endpoint")
        void shouldAllowCustomOtlpEndpoint() {
            contextRunner
                    .withPropertyValues(
                            "aidji.observability.exporters.otlp.endpoint=http://custom-host:4317"
                    )
                    .run(context -> {
                        AidjiObservabilityProperties properties = context.getBean(AidjiObservabilityProperties.class);
                        assertThat(properties.exporters().otlp().endpoint()).isEqualTo("http://custom-host:4317");
                    });
        }

        @Test
        @DisplayName("should disable other exporters by default")
        void shouldDisableOtherExportersByDefault() {
            contextRunner
                    .run(context -> {
                        AidjiObservabilityProperties properties = context.getBean(AidjiObservabilityProperties.class);
                        assertThat(properties.exporters().zipkin().enabled()).isFalse();
                        assertThat(properties.exporters().jaeger().enabled()).isFalse();
                        assertThat(properties.exporters().prometheus().enabled()).isFalse();
                    });
        }
    }

    @Nested
    @DisplayName("Properties Validation")
    class PropertiesValidationTests {

        @Test
        @DisplayName("should reject sampling probability less than 0")
        void shouldRejectSamplingProbabilityLessThanZero() {
            contextRunner
                    .withPropertyValues(
                            "aidji.observability.tracing.sampling-probability=-0.1"
                    )
                    .run(context -> {
                        assertThat(context).hasFailed();
                    });
        }

        @Test
        @DisplayName("should reject sampling probability greater than 1")
        void shouldRejectSamplingProbabilityGreaterThanOne() {
            contextRunner
                    .withPropertyValues(
                            "aidji.observability.tracing.sampling-probability=1.5"
                    )
                    .run(context -> {
                        assertThat(context).hasFailed();
                    });
        }

        @Test
        @DisplayName("should accept sampling probability between 0 and 1")
        void shouldAcceptValidSamplingProbability() {
            contextRunner
                    .withPropertyValues(
                            "aidji.observability.tracing.sampling-probability=0.5"
                    )
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        AidjiObservabilityProperties properties = context.getBean(AidjiObservabilityProperties.class);
                        assertThat(properties.tracing().samplingProbability()).isEqualTo(0.5);
                    });
        }
    }
}
