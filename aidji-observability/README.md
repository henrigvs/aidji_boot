# Aidji Observability

OpenTelemetry-based observability module for distributed tracing and metrics in Spring Boot applications.

[![Java Version](https://img.shields.io/badge/java-25-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](../LICENSE)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Supported Backends](#supported-backends)
- [Configuration](#configuration)
- [Tracing](#tracing)
- [Metrics](#metrics)
- [Exporters](#exporters)
- [Custom Spans](#custom-spans)
- [Examples](#examples)
- [Running with Docker](#running-with-docker)

---

## Overview

**aidji-observability** provides production-ready observability for Spring Boot applications using OpenTelemetry:

- Distributed tracing across microservices
- Application and JVM metrics
- Multiple exporter support (OTLP, Zipkin, Jaeger, Prometheus)
- Automatic instrumentation for HTTP, databases, and async operations
- W3C Trace Context propagation
- Zero-code integration via auto-configuration

This module is designed for **distributed systems** and works with any OpenTelemetry-compatible backend.

---

## Features

- **OpenTelemetry SDK** - Industry standard for observability
- **Automatic Instrumentation** - HTTP server/client, JDBC, R2DBC, async operations
- **Distributed Tracing** - Track requests across services with trace IDs
- **Metrics Collection** - Application metrics via Micrometer + OpenTelemetry
- **Multiple Exporters** - OTLP (gRPC), Zipkin, Jaeger, Prometheus
- **Trace Context Propagation** - W3C Trace Context standard
- **MDC Integration** - Correlation IDs in logs
- **Configurable Sampling** - Control trace volume (0-100%)
- **Service Metadata** - Automatic tagging with service name, version, environment
- **Auto-Configuration** - Zero-config Spring Boot integration

---

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>be.aidji.boot</groupId>
    <artifactId>aidji-observability</artifactId>
    <version>1.0.6-SNAPSHOT</version>
</dependency>
```

Or import the BOM:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>be.aidji.boot</groupId>
            <artifactId>aidji-bom</artifactId>
            <version>1.0.6-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>be.aidji.boot</groupId>
        <artifactId>aidji-observability</artifactId>
    </dependency>
</dependencies>
```

---

## Quick Start

### 1. Add Dependency

See [Installation](#installation)

### 2. Configure Application

```yaml
aidji:
  observability:
    enabled: true
    service-name: order-service
    service-version: 1.0.0
    environment: production

    tracing:
      enabled: true
      sampling-probability: 1.0  # 100% sampling (reduce in production)

    metrics:
      enabled: true
      step: 60s

    exporters:
      otlp:
        enabled: true
        endpoint: http://localhost:4317  # Jaeger/Tempo/etc
```

### 3. Run Your Application

That's it! All HTTP requests, database queries, and async operations are automatically traced.

### 4. View Traces

Open your observability backend:

- **Jaeger**: http://localhost:16686
- **Zipkin**: http://localhost:9411
- **Grafana Tempo**: Configure in Grafana datasources

---

## Supported Backends

This module works with any OpenTelemetry-compatible backend:

| Backend | Type | Exporter | Production Ready |
|---------|------|----------|------------------|
| **Jaeger** | Tracing | OTLP / Native | ✅ Yes |
| **Zipkin** | Tracing | Native | ✅ Yes |
| **Grafana Tempo** | Tracing | OTLP | ✅ Yes |
| **Grafana Loki** | Logs | - | ✅ Yes |
| **Prometheus** | Metrics | Scrape | ✅ Yes |
| **Elastic APM** | Full Stack | OTLP | ✅ Yes |
| **Datadog** | Full Stack | OTLP | ✅ Yes (Commercial) |
| **New Relic** | Full Stack | OTLP | ✅ Yes (Commercial) |
| **Honeycomb** | Full Stack | OTLP | ✅ Yes (Commercial) |
| **Lightstep** | Full Stack | OTLP | ✅ Yes (Commercial) |

**Recommended Stack (Free & Open Source):**
- **Tracing**: Grafana Tempo (OTLP)
- **Metrics**: Prometheus
- **Logs**: Grafana Loki
- **Visualization**: Grafana

---

## Configuration

### Complete Configuration Reference

```yaml
aidji:
  observability:
    # Enable/disable observability (default: true)
    enabled: true

    # Service metadata
    service-name: my-service          # Default: aidji-application
    service-version: 1.0.0            # Default: 1.0.0
    environment: production           # Default: development

    # Tracing configuration
    tracing:
      enabled: true
      sampling-probability: 1.0       # 0.0 to 1.0 (1.0 = 100%)
      exclude-paths:                  # Paths to exclude from tracing
        - /actuator/**
        - /health/**
        - /metrics/**

    # Metrics configuration
    metrics:
      enabled: true
      step: 60s                       # Export interval

    # Exporters
    exporters:
      # OTLP (OpenTelemetry Protocol) - Universal exporter
      otlp:
        enabled: true
        endpoint: http://localhost:4317  # gRPC endpoint
        timeout: 10s

      # Zipkin exporter
      zipkin:
        enabled: false
        endpoint: http://localhost:9411/api/v2/spans

      # Jaeger native exporter
      jaeger:
        enabled: false
        endpoint: http://localhost:14250

      # Prometheus metrics endpoint
      prometheus:
        enabled: false
        path: /actuator/prometheus
```

### Environment Variables

```bash
# Service metadata
AIDJI_OBSERVABILITY_SERVICE_NAME=my-service
AIDJI_OBSERVABILITY_SERVICE_VERSION=1.0.0
AIDJI_OBSERVABILITY_ENVIRONMENT=production

# Tracing
AIDJI_OBSERVABILITY_TRACING_ENABLED=true
AIDJI_OBSERVABILITY_TRACING_SAMPLING_PROBABILITY=0.1

# OTLP endpoint
AIDJI_OBSERVABILITY_EXPORTERS_OTLP_ENABLED=true
AIDJI_OBSERVABILITY_EXPORTERS_OTLP_ENDPOINT=http://tempo:4317
```

### Development vs Production

**Development (100% sampling):**
```yaml
aidji:
  observability:
    service-name: my-service
    environment: development
    tracing:
      sampling-probability: 1.0  # Trace everything
```

**Production (10% sampling):**
```yaml
aidji:
  observability:
    service-name: my-service
    environment: production
    tracing:
      sampling-probability: 0.1  # Trace 10% of requests
```

**Production (Head-based sampling):**
```yaml
aidji:
  observability:
    tracing:
      sampling-probability: 0.05  # 5% base rate
      # Use tail-based sampling in backend for errors/slow requests
```

---

## Tracing

### Automatic Instrumentation

The following are automatically traced:

**HTTP Server Requests:**
- Spring MVC (`@RestController`, `@Controller`)
- Spring WebFlux (reactive endpoints)

**HTTP Client Requests:**
- `RestClient` (Spring Boot 4)
- `RestTemplate`
- `WebClient` (WebFlux)
- Feign clients

**Database:**
- JDBC (via DataSource instrumentation)
- R2DBC (reactive databases)

**Async Operations:**
- `@Async` methods
- `CompletableFuture`
- Virtual threads (Java 21+)

### Trace Attributes

Each span includes:

| Attribute | Description | Example |
|-----------|-------------|---------|
| `service.name` | Service name | `order-service` |
| `service.version` | Service version | `1.0.0` |
| `deployment.environment` | Environment | `production` |
| `http.method` | HTTP method | `GET` |
| `http.route` | Route template | `/api/users/{id}` |
| `http.status_code` | HTTP status | `200` |
| `http.url` | Full URL | `https://api.example.com/users/123` |
| `db.system` | Database type | `postgresql` |
| `db.statement` | SQL query | `SELECT * FROM users` |

### Trace Context Propagation

Trace context is automatically propagated:

**Between Services (HTTP Headers):**
```
traceparent: 00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01
tracestate: congo=t61rcWkgMzE
```

**To MDC (for logs):**
```
2025-01-08 10:30:00 INFO [order-service,0af7651916cd43dd,b7ad6b7169203331] Processing order
```

**Reactive Context (WebFlux):**
Trace context propagates through reactive chains automatically.

---

## Metrics

### Automatic Metrics

The following metrics are automatically collected:

**JVM Metrics:**
- Memory usage (heap, non-heap, pools)
- Garbage collection
- Thread count
- CPU usage

**HTTP Metrics:**
- Request count
- Response time (percentiles)
- Error rate
- Active requests

**Database Metrics:**
- Connection pool size
- Active connections
- Query duration

**System Metrics:**
- CPU load
- Disk usage
- Network I/O

### Custom Metrics

Add custom metrics using Micrometer:

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final MeterRegistry registry;

    public void processOrder(Order order) {
        // Counter
        registry.counter("orders.processed",
            "status", order.getStatus(),
            "region", order.getRegion()
        ).increment();

        // Timer
        Timer.Sample sample = Timer.start(registry);
        try {
            // Process order
            validateOrder(order);
            saveOrder(order);
        } finally {
            sample.stop(registry.timer("orders.processing.time"));
        }

        // Gauge
        registry.gauge("orders.total.amount", order.getTotalAmount());
    }
}
```

### Common Tags

All metrics are automatically tagged with:

```
service=my-service
version=1.0.0
environment=production
```

---

## Exporters

### OTLP (Recommended)

OTLP is the universal OpenTelemetry protocol. Use it with:

- Jaeger (v1.35+)
- Grafana Tempo
- Elastic APM
- Datadog
- New Relic
- Honeycomb

```yaml
aidji:
  observability:
    exporters:
      otlp:
        enabled: true
        endpoint: http://localhost:4317  # gRPC
        timeout: 10s
```

**OTLP over HTTP:**
```yaml
aidji:
  observability:
    exporters:
      otlp:
        endpoint: http://localhost:4318  # HTTP endpoint
```

### Zipkin

Legacy but widely supported:

```yaml
aidji:
  observability:
    exporters:
      zipkin:
        enabled: true
        endpoint: http://localhost:9411/api/v2/spans
```

### Jaeger (Native)

Direct Jaeger protocol:

```yaml
aidji:
  observability:
    exporters:
      jaeger:
        enabled: true
        endpoint: http://localhost:14250
```

### Prometheus

Metrics scrape endpoint:

```yaml
aidji:
  observability:
    exporters:
      prometheus:
        enabled: true
        path: /actuator/prometheus

management:
  endpoints:
    web:
      exposure:
        include: prometheus
```

**Prometheus scrape config:**
```yaml
scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Multiple Exporters

Send to multiple backends simultaneously:

```yaml
aidji:
  observability:
    exporters:
      otlp:
        enabled: true
        endpoint: http://tempo:4317
      zipkin:
        enabled: true
        endpoint: http://zipkin:9411/api/v2/spans
      prometheus:
        enabled: true
```

---

## Custom Spans

### Manual Span Creation

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final Tracer tracer;
    private final OrderRepository repository;

    public void processOrder(Order order) {
        Span span = tracer.spanBuilder("process-order")
                .setAttribute("order.id", order.getId())
                .setAttribute("order.amount", order.getTotalAmount())
                .setAttribute("order.customer_id", order.getCustomerId())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Business logic
            validateOrder(order);

            // Child span
            Span validateSpan = tracer.spanBuilder("validate-order").startSpan();
            try (Scope validateScope = validateSpan.makeCurrent()) {
                // Validation logic
            } finally {
                validateSpan.end();
            }

            saveOrder(order);
            sendNotification(order);

            span.setAttribute("order.status", "processed");
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

### Span Events

Add events to track important moments:

```java
span.addEvent("Order validated");
span.addEvent("Payment processed", Attributes.of(
    AttributeKey.stringKey("payment.method"), "credit_card",
    AttributeKey.doubleKey("payment.amount"), 99.99
));
```

### Span Status

```java
// Success
span.setStatus(StatusCode.OK);

// Error
span.setStatus(StatusCode.ERROR, "Payment failed");

// Unset (default)
span.setStatus(StatusCode.UNSET);
```

---

## Examples

### Complete Application

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**application.yml:**
```yaml
spring:
  application:
    name: order-service

aidji:
  observability:
    enabled: true
    service-name: ${spring.application.name}
    service-version: 1.0.0
    environment: production

    tracing:
      enabled: true
      sampling-probability: 0.1

    exporters:
      otlp:
        enabled: true
        endpoint: http://tempo:4317

      prometheus:
        enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    tags:
      application: ${spring.application.name}
```

### Service with Custom Tracing

```java
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final Tracer tracer;
    private final PaymentGateway gateway;
    private final MeterRegistry registry;

    public Payment processPayment(Order order) {
        Span span = tracer.spanBuilder("process-payment")
                .setAttribute("order.id", order.getId())
                .setAttribute("amount", order.getTotalAmount())
                .startSpan();

        Timer.Sample sample = Timer.start(registry);

        try (Scope scope = span.makeCurrent()) {
            // Call external payment gateway
            PaymentResponse response = gateway.charge(order.getPaymentDetails());

            span.addEvent("Payment authorized", Attributes.of(
                AttributeKey.stringKey("transaction.id"), response.getTransactionId()
            ));

            Payment payment = new Payment(
                response.getTransactionId(),
                order.getTotalAmount(),
                PaymentStatus.COMPLETED
            );

            // Metrics
            registry.counter("payments.processed",
                "status", "success",
                "gateway", "stripe"
            ).increment();

            return payment;

        } catch (PaymentException e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Payment failed");

            registry.counter("payments.processed",
                "status", "failed",
                "gateway", "stripe"
            ).increment();

            throw e;
        } finally {
            sample.stop(registry.timer("payments.processing.time"));
            span.end();
        }
    }
}
```

---

## Running with Docker

### Jaeger All-in-One (Development)

```bash
docker run -d --name jaeger \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  jaegertracing/all-in-one:latest
```

- **UI**: http://localhost:16686
- **OTLP gRPC**: localhost:4317
- **OTLP HTTP**: localhost:4318

**Application config:**
```yaml
aidji:
  observability:
    exporters:
      otlp:
        endpoint: http://localhost:4317
```

### Grafana Stack (Production)

**docker-compose.yml:**
```yaml
services:
  tempo:
    image: grafana/tempo:latest
    ports:
      - "4317:4317"  # OTLP gRPC
      - "4318:4318"  # OTLP HTTP
    command: [ "-config.file=/etc/tempo.yaml" ]
    volumes:
      - ./tempo.yaml:/etc/tempo.yaml
      - tempo-data:/tmp/tempo

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_AUTH_ANONYMOUS_ENABLED=true
      - GF_AUTH_ANONYMOUS_ORG_ROLE=Admin
    volumes:
      - grafana-data:/var/lib/grafana

volumes:
  tempo-data:
  prometheus-data:
  grafana-data:
```

**Application config:**
```yaml
aidji:
  observability:
    exporters:
      otlp:
        endpoint: http://tempo:4317
      prometheus:
        enabled: true
```

---

## Best Practices

### 1. Use Appropriate Sampling

**Development:**
```yaml
sampling-probability: 1.0  # Trace everything
```

**Production:**
```yaml
sampling-probability: 0.1  # Trace 10%
```

**High-traffic Production:**
```yaml
sampling-probability: 0.01  # Trace 1%
```

### 2. Exclude Health Checks

```yaml
tracing:
  exclude-paths:
    - /actuator/**
    - /health
    - /metrics
```

### 3. Add Business Context

```java
span.setAttribute("customer.id", customerId);
span.setAttribute("order.type", orderType);
span.setAttribute("cart.items.count", items.size());
```

### 4. Use Semantic Conventions

Follow OpenTelemetry semantic conventions:
https://opentelemetry.io/docs/specs/semconv/

### 5. Monitor Cardinality

Avoid high-cardinality attributes in metrics:

**Bad:**
```java
registry.counter("requests", "user_id", userId); // Millions of users!
```

**Good:**
```java
registry.counter("requests", "user_type", userType); // Few types
```

---

## Troubleshooting

### No traces appearing

1. Check exporter endpoint is reachable
2. Verify sampling probability > 0
3. Check application logs for errors
4. Test with 100% sampling first

### High memory usage

1. Reduce sampling probability
2. Adjust batch size (OpenTelemetry configuration)
3. Exclude unnecessary paths

### Missing spans

1. Ensure parent context is propagated
2. Check async operations use proper context
3. Verify instrumentation libraries are loaded

---

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](../LICENSE) file for details.

---

## Links

- **Repository**: https://github.com/henrigvs/aidji_boot
- **Issues**: https://github.com/henrigvs/aidji_boot/issues
- **OpenTelemetry**: https://opentelemetry.io
- **Grafana**: https://grafana.com
- **Jaeger**: https://www.jaegertracing.io
