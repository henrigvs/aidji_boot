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
package be.aidji.boot.observability.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for Aidji Boot Observability module.
 *
 * <p>Provides OpenTelemetry-based distributed tracing, metrics, and logging integration.</p>
 *
 * <p>Example configuration:</p>
 * <pre>{@code
 * aidji:
 *   observability:
 *     enabled: true
 *     service-name: my-service
 *     service-version: 1.0.0
 *     tracing:
 *       enabled: true
 *       sampling-probability: 1.0
 *     metrics:
 *       enabled: true
 *       step: 60s
 *     exporters:
 *       otlp:
 *         enabled: true
 *         endpoint: http://localhost:4317
 *       zipkin:
 *         enabled: false
 *         endpoint: http://localhost:9411/api/v2/spans
 * }</pre>
 */
@ConfigurationProperties(prefix = "aidji.observability")
public record AidjiObservabilityProperties(
        boolean enabled,
        String serviceName,
        String serviceVersion,
        String environment,
        TracingProperties tracing,
        MetricsProperties metrics,
        ExportersProperties exporters
) {

    public AidjiObservabilityProperties {
        if (serviceName == null || serviceName.isBlank()) {
            serviceName = "aidji-application";
        }
        if (serviceVersion == null || serviceVersion.isBlank()) {
            serviceVersion = "1.0.0";
        }
        if (environment == null || environment.isBlank()) {
            environment = "development";
        }
        if (tracing == null) {
            tracing = new TracingProperties(true, 1.0, List.of());
        }
        if (metrics == null) {
            metrics = new MetricsProperties(true, Duration.ofSeconds(60));
        }
        if (exporters == null) {
            exporters = new ExportersProperties(
                    new OtlpExporterProperties(true, "http://localhost:4317", Duration.ofSeconds(10)),
                    new ZipkinExporterProperties(false, "http://localhost:9411/api/v2/spans"),
                    new JaegerExporterProperties(false, "http://localhost:14250"),
                    new PrometheusExporterProperties(false, "/actuator/prometheus")
            );
        }
    }

    /**
     * Tracing configuration.
     */
    public record TracingProperties(
            // Enable distributed tracing.
            boolean enabled,

            // Sampling probability (0.0 to 1.0). 1.0 means 100% of traces are sampled.
            double samplingProbability,

            // Paths to exclude from tracing (e.g., /actuator/**).
            List<String> excludePaths
    ) {
        public TracingProperties {
            if (samplingProbability < 0.0 || samplingProbability > 1.0) {
                throw new IllegalArgumentException("Sampling probability must be between 0.0 and 1.0");
            }
            if (excludePaths == null) {
                excludePaths = List.of("/actuator/**", "/health/**", "/metrics/**");
            }
        }
    }

    /**
     * Metrics configuration.
     */
    public record MetricsProperties(
            // Enable metrics collection.
            boolean enabled,

            // Metrics export interval.
            Duration step
    ) {
        public MetricsProperties {
            if (step == null) {
                step = Duration.ofSeconds(60);
            }
        }
    }

    /**
     * Exporters configuration.
     */
    public record ExportersProperties(
            OtlpExporterProperties otlp,
            ZipkinExporterProperties zipkin,
            JaegerExporterProperties jaeger,
            PrometheusExporterProperties prometheus
    ) {
        public ExportersProperties {
            if (otlp == null) {
                otlp = new OtlpExporterProperties(true, "http://localhost:4317", Duration.ofSeconds(10));
            }
            if (zipkin == null) {
                zipkin = new ZipkinExporterProperties(false, "http://localhost:9411/api/v2/spans");
            }
            if (jaeger == null) {
                jaeger = new JaegerExporterProperties(false, "http://localhost:14250");
            }
            if (prometheus == null) {
                prometheus = new PrometheusExporterProperties(false, "/actuator/prometheus");
            }
        }
    }

    /**
     * OTLP (OpenTelemetry Protocol) exporter configuration.
     * OTLP is the standard protocol for OpenTelemetry and works with most observability backends
     * (Jaeger, Grafana Tempo, Elastic APM, etc.).
     */
    public record OtlpExporterProperties(
            // Enable OTLP exporter.
            boolean enabled,

            // OTLP collector endpoint (gRPC).
            String endpoint,

            // Export timeout.
            Duration timeout
    ) {
        public OtlpExporterProperties {
            if (endpoint == null || endpoint.isBlank()) {
                endpoint = "http://localhost:4317";
            }
            if (timeout == null) {
                timeout = Duration.ofSeconds(10);
            }
        }
    }

    /**
     * Zipkin exporter configuration.
     */
    public record ZipkinExporterProperties(
            // Enable Zipkin exporter.
            boolean enabled,

            // Zipkin endpoint.
            String endpoint
    ) {
        public ZipkinExporterProperties {
            if (endpoint == null || endpoint.isBlank()) {
                endpoint = "http://localhost:9411/api/v2/spans";
            }
        }
    }

    /**
     * Jaeger exporter configuration.
     */
    public record JaegerExporterProperties(
            // Enable Jaeger exporter.
            boolean enabled,

            // Jaeger endpoint.
            String endpoint
    ) {
        public JaegerExporterProperties {
            if (endpoint == null || endpoint.isBlank()) {
                endpoint = "http://localhost:14250";
            }
        }
    }

    /**
     * Prometheus exporter configuration.
     */
    public record PrometheusExporterProperties(
            // Enable Prometheus metrics endpoint.
            boolean enabled,

            // Metrics scrape path.
            String path
    ) {
        public PrometheusExporterProperties {
            if (path == null || path.isBlank()) {
                path = "/actuator/prometheus";
            }
        }
    }
}
