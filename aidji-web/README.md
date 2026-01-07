# Aidji Web

Web layer module providing REST client utilities, global exception handling, CORS configuration, and request logging for Spring MVC applications.

[![Code Coverage](https://img.shields.io/badge/coverage-36%25-yellow)](target/site/jacoco/index.html)
[![Java Version](https://img.shields.io/badge/java-25-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](../LICENSE)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Global Exception Handler](#global-exception-handler)
- [REST Client Factory](#rest-client-factory)
- [CORS Configuration](#cors-configuration)
- [Request Logging](#request-logging)
- [Feign Client Logging](#feign-client-logging)
- [Configuration](#configuration)
- [Examples](#examples)
- [Version History](#version-history)

---

## Overview

**aidji-web** provides production-ready web layer components for Spring Boot 4 applications:
- Automatic exception handling with standardized `ApiResponse` format
- Pre-configured `RestClient` with timeout management
- CORS auto-configuration
- Request logging with trace ID propagation
- Feign client aspect for standardized error handling

This module depends on **aidji-core** and integrates seamlessly with Spring MVC.

---

## Features

- **Global Exception Handler** - Catches all exceptions and returns standardized `ApiResponse<T>`
- **REST Client Factory** - Pre-configured `RestClient.Builder` with timeouts
- **CORS Support** - Auto-configured cross-origin resource sharing
- **Request Logging** - Logs HTTP requests with trace IDs
- **Feign Client Aspect** - `@LogFeignClient` annotation for Feign error handling
- **Error ID Configuration** - Configurable error ID inclusion in responses
- **Auto-Configuration** - Zero-config Spring Boot integration

---

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>be.aidji.boot</groupId>
    <artifactId>aidji-web</artifactId>
    <version>1.0.5-SNAPSHOT</version>
</dependency>
```

Or import the BOM:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>be.aidji.boot</groupId>
            <artifactId>aidji-bom</artifactId>
            <version>1.0.5-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>be.aidji.boot</groupId>
        <artifactId>aidji-web</artifactId>
    </dependency>
</dependencies>
```

---

## Global Exception Handler

### Overview

`GlobalExceptionHandler` automatically catches all exceptions and converts them to standardized `ApiResponse` format.

### Supported Exceptions

| Exception Type | HTTP Status | Description |
|----------------|-------------|-------------|
| `FunctionalException` | From error code | Business/validation errors |
| `TechnicalException` | From error code | Infrastructure errors |
| `SecurityException` | From error code | Auth/authz errors |
| `MethodArgumentNotValidException` | 400 | Bean validation failures |
| `HttpMessageNotReadableException` | 400 | Malformed JSON |
| `HttpRequestMethodNotSupportedException` | 405 | Wrong HTTP method |
| `Exception` (fallback) | 500 | Unexpected errors |

### Example Response

**Exception thrown:**
```java
throw FunctionalException.builder(UserErrorCode.NOT_FOUND)
    .message("User with id %d not found", userId)
    .context("userId", userId)
    .build();
```

**HTTP Response (404):**
```json
{
  "data": null,
  "metadata": {
    "timestamp": "2025-01-07T10:30:00Z",
    "traceId": "abc-def-123",
    "path": "/api/users/123"
  },
  "errors": [
    {
      "code": "USER-001",
      "message": "User with id 123 not found",
      "errorId": "550e8400-e29b-41d4-a716-446655440000"
    }
  ]
}
```

### Bean Validation Errors

**Request:**
```json
{
  "email": "",
  "password": "123"
}
```

**Response (400):**
```json
{
  "data": null,
  "metadata": { ... },
  "errors": [
    {
      "code": "AIDJI-002",
      "message": "email: must not be blank"
    },
    {
      "code": "AIDJI-002",
      "message": "password: size must be between 8 and 100"
    }
  ]
}
```

### Configuration

```yaml
aidji:
  web:
    exception-handling:
      include-error-id: true  # Include error IDs in API responses (default: false)
```

---

## REST Client Factory

### Overview

Pre-configured `RestClient.Builder` with sensible timeout defaults.

### Usage

```java
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestClient.Builder restClientBuilder;
    private final RestClient paymentClient;

    @PostConstruct
    public void init() {
        paymentClient = restClientBuilder
            .baseUrl("https://api.payment-gateway.com")
            .defaultHeader("API-Key", apiKey)
            .build();
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        return paymentClient.post()
            .uri("/payments")
            .body(request)
            .retrieve()
            .body(PaymentResponse.class);
    }
}
```

### Configuration

```yaml
aidji:
  web:
    rest-client:
      connect-timeout: 5000    # Connection timeout in ms (default: 5000)
      read-timeout: 30000      # Read timeout in ms (default: 30000)
```

### Default Timeouts

| Timeout | Default | Description |
|---------|---------|-------------|
| Connection | 5 seconds | Time to establish connection |
| Read | 30 seconds | Time to read response |

---

## CORS Configuration

### Overview

Auto-configured CORS support for cross-origin requests.

### Configuration

```yaml
aidji:
  web:
    cors:
      allowed-origins:
        - https://app.example.com
        - https://admin.example.com
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
      allowed-headers:
        - Authorization
        - Content-Type
      exposed-headers:
        - X-Total-Count
      allow-credentials: true
      max-age: 3600  # Cache preflight for 1 hour
```

### Development Configuration

```yaml
aidji:
  web:
    cors:
      allowed-origins:
        - http://localhost:3000
        - http://localhost:4200
      allowed-methods:
        - "*"
      allowed-headers:
        - "*"
      allow-credentials: true
```

### Production Best Practices

```yaml
aidji:
  web:
    cors:
      allowed-origins:
        - https://app.example.com  # Explicit domains only
      allowed-methods:
        - GET
        - POST  # Only methods you need
      allowed-headers:
        - Authorization
        - Content-Type  # Explicit headers only
      allow-credentials: true  # Required for cookies
      max-age: 3600  # Reduce preflight requests
```

---

## Request Logging

### Overview

`RequestLoggingFilter` logs all HTTP requests with trace ID propagation.

### Log Format

```
INFO  be.aidji.boot.web.filter.RequestLoggingFilter - HTTP Request [GET /api/users/123] traceId=abc-def-123
INFO  be.aidji.boot.web.filter.RequestLoggingFilter - HTTP Response [GET /api/users/123] status=200 duration=45ms traceId=abc-def-123
```

### Trace ID Propagation

The filter:
1. Extracts trace ID from `X-Trace-Id` header (if present)
2. Generates new trace ID if missing
3. Adds trace ID to MDC for logging
4. Includes trace ID in `ApiResponse.metadata`
5. Returns trace ID in response header

### Configuration

```yaml
logging:
  level:
    be.aidji.boot.web.filter.RequestLoggingFilter: INFO

# Custom log pattern with trace ID
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [traceId=%X{traceId}] - %msg%n"
```

---

## Feign Client Logging

### Overview

`@LogFeignClient` aspect provides standardized error handling for Feign clients.

### Usage

```java
@FeignClient(name = "payment-service", url = "${payment.service.url}")
public interface PaymentClient {

    @LogFeignClient(clientName = "payment-service")
    @PostMapping("/payments")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);

    @LogFeignClient(clientName = "payment-service", rethrowException = false)
    @GetMapping("/payments/{id}")
    Optional<PaymentResponse> getPayment(@PathVariable String id);
}
```

### Features

- **Automatic Error Handling** - Catches `FeignException` and wraps in `TechnicalException`
- **Standardized Error Code** - Uses `CommonErrorCode.EXTERNAL_SERVICE_ERROR`
- **Request/Response Logging** - Logs calls with timing
- **Optional Rethrowing** - Control exception propagation

### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `clientName` | String | `""` | Service name for logging |
| `rethrowException` | boolean | `true` | Whether to rethrow as `TechnicalException` |

### Example Log Output

**Success:**
```
INFO  LogFeignClientAspect - Calling Feign client [payment-service]: processPayment
INFO  LogFeignClientAspect - Feign client [payment-service] call successful - duration: 234ms
```

**Failure (rethrowException = true):**
```
ERROR LogFeignClientAspect - Feign client [payment-service] call failed - status: 503, error: Service Unavailable
Exception in thread "main" be.aidji.boot.core.exception.TechnicalException: Call to payment-service failed with status 503 SERVICE_UNAVAILABLE
```

**Failure (rethrowException = false):**
```
WARN  LogFeignClientAspect - Feign client [payment-service] call failed but exception not rethrown - status: 404
```

### Migration from 1.0.4

**Before (1.0.4):**
```java
@LogFeignClient(
    clientName = "payment-service",
    errorCodeClass = PaymentErrorCode.class,
    errorCodeValue = "PAYMENT_SERVICE_ERROR"
)
```

**After (1.0.5):**
```java
@LogFeignClient(clientName = "payment-service")
```

The annotation now automatically uses `CommonErrorCode.EXTERNAL_SERVICE_ERROR` for all Feign client errors.

---

## Configuration

### Complete Configuration Reference

```yaml
aidji:
  web:
    # Exception Handling
    exception-handling:
      include-error-id: false  # Include error IDs in responses (default: false)
      enabled: true            # Enable GlobalExceptionHandler (default: true)

    # REST Client
    rest-client:
      connect-timeout: 5000    # Connection timeout in ms (default: 5000)
      read-timeout: 30000      # Read timeout in ms (default: 30000)

    # CORS
    cors:
      enabled: true            # Enable CORS (default: true)
      allowed-origins:
        - "*"                  # Production: use explicit domains
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - PATCH
        - OPTIONS
      allowed-headers:
        - "*"
      exposed-headers: []
      allow-credentials: false
      max-age: 3600            # Preflight cache in seconds

    # Request Logging
    logging:
      enabled: true            # Enable request logging (default: true)
```

### Disabling Features

```yaml
# Disable exception handling
aidji:
  web:
    exception-handling:
      enabled: false

# Disable CORS
aidji:
  web:
    cors:
      enabled: false

# Disable request logging
aidji:
  web:
    logging:
      enabled: false
```

---

## Examples

### Complete REST Controller

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        // Exceptions are automatically caught by GlobalExceptionHandler
        return userService.findById(id);
    }

    @PostMapping
    public ApiResponse<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        // Bean validation errors are automatically handled
        return userService.create(request);
    }

    @GetMapping
    public PageResponse<User> listUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return userService.findAll(page, size);
    }
}
```

### Custom Exception Handler

Extend `GlobalExceptionHandler` to add custom logic:

```java
@ControllerAdvice
public class CustomExceptionHandler extends GlobalExceptionHandler {

    public CustomExceptionHandler(AidjiWebProperties properties) {
        super(properties);
    }

    @ExceptionHandler(CustomBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(
        CustomBusinessException ex,
        HttpServletRequest request
    ) {
        ApiError error = ApiError.of(ex.getCode(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.failure(error);

        return ResponseEntity
            .status(ex.getHttpStatus())
            .body(response);
    }
}
```

### Feign Client with Retry

```java
@FeignClient(
    name = "payment-service",
    url = "${payment.service.url}",
    configuration = PaymentClientConfig.class
)
public interface PaymentClient {

    @LogFeignClient(clientName = "payment-service")
    @PostMapping("/payments")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);
}

@Configuration
public class PaymentClientConfig {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, 1000, 3);  // 3 retries
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
```

---

## Testing

### Testing Exception Handler

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.findById(123L))
            .thenThrow(FunctionalException.builder(UserErrorCode.NOT_FOUND)
                .message("User not found")
                .build());

        mockMvc.perform(get("/api/users/123"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errors[0].code").value("USER-001"))
            .andExpect(jsonPath("$.errors[0].message").value("User not found"));
    }
}
```

### Testing with RestClient

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @LocalServerPort
    private int port;

    @Test
    void shouldProcessPayment() {
        PaymentRequest request = new PaymentRequest("ORD-123", 99.99);

        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response.status()).isEqualTo("SUCCESS");
    }
}
```

---

## Version History

See the [CHANGELOG](../CHANGELOG.md) for detailed version history and release notes.

**Current Version:** `1.0.5-SNAPSHOT`

**Latest Stable:** `v1.0.4` ([View Release](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.4))

### Recent Changes (1.0.5-SNAPSHOT)

- Simplified `@LogFeignClient` annotation (removed `errorCodeClass`/`errorCodeValue` parameters)
- Added 13 comprehensive tests for `GlobalExceptionHandler` (98% coverage)
- Fixed invalid import statement in `LogFeignClient`

---

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](../LICENSE) file for details.

---

## Links

- **Repository**: https://github.com/henrigvs/aidji_boot
- **Issues**: https://github.com/henrigvs/aidji_boot/issues
- **Releases**: https://github.com/henrigvs/aidji_boot/releases
- **Contributing**: [CONTRIBUTING.md](../CONTRIBUTING.md)
