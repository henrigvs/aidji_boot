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
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Aidji observability with OpenTelemetry.
 *
 * <p>This configuration automatically sets up:</p>
 * <ul>
 *     <li>Distributed tracing with OpenTelemetry</li>
 *     <li>Metrics collection with Micrometer</li>
 *     <li>OTLP exporter for telemetry data</li>
 *     <li>Service metadata and resource attributes</li>
 * </ul>
 *
 * <p>The module is enabled by default but can be disabled via:</p>
 * <pre>{@code
 * aidji.observability.enabled=false
 * }</pre>
 *
 * @see AidjiObservabilityProperties
 */
@AutoConfiguration
@ConditionalOnProperty(
        name = "aidji.observability.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableConfigurationProperties(AidjiObservabilityProperties.class)
public class AidjiObservabilityAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AidjiObservabilityAutoConfiguration.class);

    private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
    private static final AttributeKey<String> SERVICE_VERSION = AttributeKey.stringKey("service.version");
    private static final AttributeKey<String> DEPLOYMENT_ENVIRONMENT = AttributeKey.stringKey("deployment.environment");

    /**
     * Creates OpenTelemetry resource with service metadata.
     */
    @Bean
    @ConditionalOnMissingBean
    public Resource otelResource(AidjiObservabilityProperties properties) {
        log.info("Configuring OpenTelemetry resource: service={}, version={}, environment={}",
                properties.serviceName(), properties.serviceVersion(), properties.environment());

        return Resource.create(
                Attributes.builder()
                        .put(SERVICE_NAME, properties.serviceName())
                        .put(SERVICE_VERSION, properties.serviceVersion())
                        .put(DEPLOYMENT_ENVIRONMENT, properties.environment())
                        .build()
        );
    }

    /**
     * Creates OTLP gRPC span exporter if enabled.
     */
    @Bean
    @ConditionalOnProperty(name = "aidji.observability.exporters.otlp.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public OtlpGrpcSpanExporter otlpGrpcSpanExporter(AidjiObservabilityProperties properties) {
        var otlpConfig = properties.exporters().otlp();
        log.info("Configuring OTLP gRPC exporter: endpoint={}", otlpConfig.endpoint());

        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(otlpConfig.endpoint())
                .setTimeout(otlpConfig.timeout())
                .build();
    }

    /**
     * Creates OpenTelemetry tracer provider with configured samplers and exporters.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "aidji.observability.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public SdkTracerProvider sdkTracerProvider(
            AidjiObservabilityProperties properties,
            Resource resource,
            OtlpGrpcSpanExporter spanExporter
    ) {
        var tracingConfig = properties.tracing();
        log.info("Configuring OpenTelemetry tracer: sampling={}%",
                tracingConfig.samplingProbability() * 100);

        Sampler sampler = Sampler.traceIdRatioBased(tracingConfig.samplingProbability());

        return SdkTracerProvider.builder()
                .setResource(resource)
                .setSampler(sampler)
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .build();
    }

    /**
     * Creates OpenTelemetry SDK instance.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(OpenTelemetry.class)
    @ConditionalOnProperty(name = "aidji.observability.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public OpenTelemetry openTelemetry(SdkTracerProvider tracerProvider) {
        log.info("Initializing OpenTelemetry SDK");

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
    }

    /**
     * Creates a tracer for application use.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(Tracer.class)
    @ConditionalOnProperty(name = "aidji.observability.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public Tracer tracer(OpenTelemetry openTelemetry, AidjiObservabilityProperties properties) {
        return openTelemetry.getTracer(properties.serviceName(), properties.serviceVersion());
    }

    /**
     * Configures MeterRegistry with common tags when available.
     */
    @Autowired(required = false)
    public void configureMeterRegistry(MeterRegistry registry, AidjiObservabilityProperties properties) {
        if (registry != null) {
            log.info("Configuring Micrometer metrics with service tags");

            registry.config()
                    .commonTags(
                            "service", properties.serviceName(),
                            "version", properties.serviceVersion(),
                            "environment", properties.environment()
                    );
        }
    }
}
