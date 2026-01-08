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

/**
 * Aidji Framework Observability Module.
 *
 * <p>This module provides OpenTelemetry-based observability for distributed systems:</p>
 *
 * <ul>
 *   <li><b>Distributed Tracing</b> - Automatic request tracing across services with OpenTelemetry</li>
 *   <li><b>Metrics</b> - Application and JVM metrics with Micrometer integration</li>
 *   <li><b>Multiple Exporters</b> - Support for OTLP, Zipkin, Jaeger, and Prometheus</li>
 *   <li><b>Service Metadata</b> - Automatic tagging with service name, version, and environment</li>
 * </ul>
 *
 * <h2>Quick Start</h2>
 *
 * <p>Add the dependency to your project:</p>
 * <pre>{@code
 * <dependency>
 *     <groupId>be.aidji.boot</groupId>
 *     <artifactId>aidji-observability</artifactId>
 * </dependency>
 * }</pre>
 *
 * <p>Configure your application:</p>
 * <pre>{@code
 * aidji:
 *   observability:
 *     enabled: true
 *     service-name: my-service
 *     service-version: 1.0.0
 *     environment: production
 *     tracing:
 *       enabled: true
 *       sampling-probability: 1.0
 *     metrics:
 *       enabled: true
 *     exporters:
 *       otlp:
 *         enabled: true
 *         endpoint: http://localhost:4317
 * }</pre>
 *
 * <h2>Supported Backends</h2>
 *
 * <p>The module works with any OpenTelemetry-compatible observability backend:</p>
 * <ul>
 *   <li><b>Jaeger</b> - Distributed tracing (via OTLP or native exporter)</li>
 *   <li><b>Zipkin</b> - Distributed tracing</li>
 *   <li><b>Grafana Tempo</b> - Tracing backend (via OTLP)</li>
 *   <li><b>Grafana Loki</b> - Log aggregation</li>
 *   <li><b>Prometheus</b> - Metrics collection</li>
 *   <li><b>Elastic APM</b> - Full observability stack (via OTLP)</li>
 *   <li><b>Datadog</b> - Commercial APM (via OTLP)</li>
 *   <li><b>New Relic</b> - Commercial APM (via OTLP)</li>
 * </ul>
 *
 * <h2>Features</h2>
 *
 * <h3>Automatic Instrumentation</h3>
 * <p>The module automatically instruments:</p>
 * <ul>
 *   <li>HTTP server requests (Spring MVC and WebFlux)</li>
 *   <li>HTTP client requests (RestClient, RestTemplate, WebClient)</li>
 *   <li>Database queries (JDBC, R2DBC)</li>
 *   <li>Async operations (@Async, CompletableFuture)</li>
 * </ul>
 *
 * <h3>Trace Context Propagation</h3>
 * <p>Trace context is automatically propagated:</p>
 * <ul>
 *   <li>Between microservices via HTTP headers (W3C Trace Context)</li>
 *   <li>To MDC for log correlation</li>
 *   <li>To reactive context (WebFlux)</li>
 * </ul>
 *
 * <h3>Custom Spans</h3>
 * <p>Create custom spans for business logic:</p>
 * <pre>{@code
 * @Service
 * public class OrderService {
 *
 *     private final Tracer tracer;
 *
 *     public void processOrder(Order order) {
 *         Span span = tracer.spanBuilder("process-order")
 *                 .setAttribute("order.id", order.getId())
 *                 .setAttribute("order.amount", order.getAmount())
 *                 .startSpan();
 *
 *         try (Scope scope = span.makeCurrent()) {
 *             // Your business logic
 *             validateOrder(order);
 *             saveOrder(order);
 *             sendNotification(order);
 *         } finally {
 *             span.end();
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h2>Configuration Examples</h2>
 *
 * <h3>Development (Jaeger All-in-One)</h3>
 * <pre>{@code
 * aidji:
 *   observability:
 *     service-name: my-service
 *     environment: development
 *     tracing:
 *       sampling-probability: 1.0  # 100% sampling
 *     exporters:
 *       otlp:
 *         enabled: true
 *         endpoint: http://localhost:4317
 * }</pre>
 *
 * <h3>Production (Grafana Stack)</h3>
 * <pre>{@code
 * aidji:
 *   observability:
 *     service-name: my-service
 *     environment: production
 *     tracing:
 *       sampling-probability: 0.1  # 10% sampling
 *     metrics:
 *       step: 30s
 *     exporters:
 *       otlp:
 *         enabled: true
 *         endpoint: https://tempo-gateway.mycompany.com:4317
 *       prometheus:
 *         enabled: true
 *         path: /actuator/prometheus
 * }</pre>
 *
 * <h3>Multiple Exporters</h3>
 * <pre>{@code
 * aidji:
 *   observability:
 *     exporters:
 *       otlp:
 *         enabled: true
 *         endpoint: http://localhost:4317
 *       zipkin:
 *         enabled: true
 *         endpoint: http://localhost:9411/api/v2/spans
 *       prometheus:
 *         enabled: true
 * }</pre>
 *
 * @see be.aidji.boot.observability.config.AidjiObservabilityAutoConfiguration
 * @see be.aidji.boot.observability.properties.AidjiObservabilityProperties
 */
package be.aidji.boot.observability;
