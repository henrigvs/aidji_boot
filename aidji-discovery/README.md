# Aidji Discovery

Service discovery module with Eureka client auto-configuration for microservices architecture.

[![Java Version](https://img.shields.io/badge/java-25-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](../LICENSE)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Eureka Server Setup](#eureka-server-setup)
- [Docker & Kubernetes](#docker--kubernetes)
- [High Availability](#high-availability)
- [Health Checks](#health-checks)
- [Examples](#examples)

---

## Overview

**aidji-discovery** provides production-ready service discovery integration with Netflix Eureka:
- Auto-configured Eureka client with sensible defaults
- Environment variable support for containerized deployments
- Multiple Eureka server support (high availability)
- Seamless integration with Spring Cloud LoadBalancer
- Instance metadata customization
- Health check integration

This module is designed for **microservices architectures** where services need to discover each other dynamically.

---

## Features

- **Auto-Configuration** - Zero-config Eureka client setup
- **Environment Variables** - Support for `EUREKA_SERVER_URL`
- **HA Support** - Multiple Eureka servers for high availability
- **IP Preference** - Prefer IP address over hostname (container-friendly)
- **Health Checks** - Automatic health endpoint registration
- **Metadata** - Custom instance metadata
- **Load Balancing** - Integrates with Spring Cloud LoadBalancer
- **Service-to-Service** - Enables @FeignClient and RestClient service discovery

---

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>be.aidji.boot</groupId>
    <artifactId>aidji-discovery</artifactId>
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
        <artifactId>aidji-discovery</artifactId>
    </dependency>
</dependencies>
```

---

## Quick Start

### 1. Configure Eureka Server URL

```yaml
aidji:
  discovery:
    enabled: true
    eureka:
      server-url: http://localhost:8761/eureka
```

### 2. Run Your Application

That's it! Your application will automatically:
- Register with Eureka server on startup
- Send heartbeats every 10 seconds
- Fetch registry updates every 30 seconds
- Deregister on shutdown

### 3. Service-to-Service Communication

**Using Feign Client:**
```java
@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    Order getOrder(@PathVariable Long id);
}
```

**Using RestClient with LoadBalancer:**
```java
@Service
@RequiredArgsConstructor
public class OrderService {

    @LoadBalanced
    private final RestClient.Builder restClientBuilder;

    public Order getOrder(Long id) {
        RestClient client = restClientBuilder.build();
        return client.get()
                .uri("http://order-service/api/orders/{id}", id)
                .retrieve()
                .body(Order.class);
    }
}
```

---

## Configuration

### Complete Configuration Reference

```yaml
aidji:
  discovery:
    # Enable/disable discovery (default: true)
    enabled: true

    eureka:
      # Eureka server URL(s)
      server-url: ${EUREKA_SERVER_URL:http://localhost:8761/eureka}

      # Instance configuration
      instance:
        # Prefer IP address over hostname (recommended for Docker/K8s)
        prefer-ip-address: true

        # Heartbeat interval (default: 10s)
        lease-renewal-interval-seconds: 10

        # Time before Eureka evicts instance (default: 30s)
        lease-expiration-duration-seconds: 30

        # Instance ID (default: ${spring.application.name}:${random.value})
        instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}

        # Health check URL
        health-check-url-path: /actuator/health

        # Status page URL
        status-page-url-path: /actuator/info

        # Custom metadata
        metadata-map:
          zone: us-east-1a
          version: 1.0.0

      # Client configuration
      client:
        # Fetch registry from Eureka (default: true)
        fetch-registry: true

        # Register with Eureka (default: true)
        register-with-eureka: true

        # Registry fetch interval (default: 30s)
        registry-fetch-interval-seconds: 30

        # Initial instance registration delay (default: 40s)
        initial-instance-info-replication-interval-seconds: 40

spring:
  application:
    name: my-service  # Service name (required)
```

### Environment Variables

```bash
# Eureka server URL
export EUREKA_SERVER_URL=http://eureka:8761/eureka

# Service name
export SPRING_APPLICATION_NAME=my-service

# Instance ID (optional)
export SPRING_APPLICATION_INSTANCE_ID=1
```

---

## Eureka Server Setup

### Run Eureka Server with Docker

```bash
docker run -d \
  --name eureka-server \
  -p 8761:8761 \
  springcloud/eureka
```

### Access Eureka Dashboard

Open http://localhost:8761 in your browser to see registered services.

### Custom Eureka Server

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

**application.yml:**
```yaml
server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

---

## Docker & Kubernetes

### Docker Compose

```yaml
version: '3.8'

services:
  eureka:
    image: springcloud/eureka
    ports:
      - "8761:8761"
    networks:
      - microservices

  order-service:
    image: mycompany/order-service:latest
    environment:
      - EUREKA_SERVER_URL=http://eureka:8761/eureka
      - SPRING_APPLICATION_NAME=order-service
    depends_on:
      - eureka
    networks:
      - microservices

  user-service:
    image: mycompany/user-service:latest
    environment:
      - EUREKA_SERVER_URL=http://eureka:8761/eureka
      - SPRING_APPLICATION_NAME=user-service
    depends_on:
      - eureka
    networks:
      - microservices

networks:
  microservices:
```

### Kubernetes Deployment

**ConfigMap:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: discovery-config
data:
  EUREKA_SERVER_URL: "http://eureka-server.default.svc.cluster.local:8761/eureka"
```

**Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      containers:
      - name: order-service
        image: mycompany/order-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_APPLICATION_NAME
          value: "order-service"
        - name: EUREKA_SERVER_URL
          valueFrom:
            configMapKeyRef:
              name: discovery-config
              key: EUREKA_SERVER_URL
```

---

## High Availability

### Multiple Eureka Servers

```yaml
aidji:
  discovery:
    eureka:
      server-url: |
        http://eureka1:8761/eureka,
        http://eureka2:8762/eureka,
        http://eureka3:8763/eureka
```

### Eureka Server Cluster

**Eureka Server 1:**
```yaml
server:
  port: 8761

eureka:
  instance:
    hostname: eureka1
  client:
    service-url:
      defaultZone: http://eureka2:8762/eureka,http://eureka3:8763/eureka
```

**Eureka Server 2:**
```yaml
server:
  port: 8762

eureka:
  instance:
    hostname: eureka2
  client:
    service-url:
      defaultZone: http://eureka1:8761/eureka,http://eureka3:8763/eureka
```

**Benefits:**
- No single point of failure
- Automatic failover
- Data replication
- Load distribution

---

## Health Checks

### Enable Health Endpoint

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info

  endpoint:
    health:
      show-details: always
```

### Custom Health Indicator

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Connected")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

Eureka will automatically check `/actuator/health` and remove unhealthy instances.

---

## Examples

### Microservices Architecture

```
                    ┌─────────────┐
                    │   Eureka    │
                    │   Server    │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
    ┌───▼────┐      ┌─────▼─────┐      ┌────▼─────┐
    │  User  │      │   Order   │      │ Payment  │
    │Service │◄────►│  Service  │◄────►│ Service  │
    └────────┘      └───────────┘      └──────────┘
```

### Service A (User Service)

```java
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return new User(id, "John Doe");
    }
}
```

**application.yml:**
```yaml
spring:
  application:
    name: user-service

server:
  port: 8081

aidji:
  discovery:
    eureka:
      server-url: http://localhost:8761/eureka
```

### Service B (Order Service) - Calling User Service

```java
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    User getUser(@PathVariable Long id);
}

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserClient userClient;

    public Order createOrder(Long userId, OrderRequest request) {
        // Call user-service via Eureka
        User user = userClient.getUser(userId);

        return new Order(user, request.getItems());
    }
}
```

**application.yml:**
```yaml
spring:
  application:
    name: order-service

server:
  port: 8082

aidji:
  discovery:
    eureka:
      server-url: http://localhost:8761/eureka
```

---

## Disabling Discovery

For local development or testing:

```yaml
aidji:
  discovery:
    enabled: false
```

Or via environment:

```bash
AIDJI_DISCOVERY_ENABLED=false
```

---

## Troubleshooting

### Service not registering

1. Check Eureka server is running
2. Verify `server-url` is correct
3. Check network connectivity
4. Review logs for errors

### Slow registration

Increase heartbeat frequency:

```yaml
aidji:
  discovery:
    eureka:
      instance:
        lease-renewal-interval-seconds: 5
        lease-expiration-duration-seconds: 15
```

### Wrong IP registered

Force IP preference:

```yaml
aidji:
  discovery:
    eureka:
      instance:
        prefer-ip-address: true
```

---

## License

Apache License 2.0 - see [LICENSE](../LICENSE)

---

## Links

- **Repository**: https://github.com/henrigvs/aidji_boot
- **Eureka Wiki**: https://github.com/Netflix/eureka/wiki
- **Spring Cloud Netflix**: https://spring.io/projects/spring-cloud-netflix
