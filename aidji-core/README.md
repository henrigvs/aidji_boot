# Aidji Core

Foundation module providing core utilities, exception handling, and DTOs for the Aidji Boot framework.

[![Code Coverage](https://img.shields.io/badge/coverage-77%25-brightgreen)](target/site/jacoco/index.html)
[![Java Version](https://img.shields.io/badge/java-25-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](../LICENSE)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Exception Hierarchy](#exception-hierarchy)
- [API Response Wrapper](#api-response-wrapper)
- [Pagination Support](#pagination-support)
- [Utilities](#utilities)
- [Examples](#examples)
- [Version History](#version-history)

---

## Overview

**aidji-core** is the foundation module with zero Aidji dependencies. It provides:
- Hierarchical exception system with builder pattern
- Standardized API response wrappers
- Pagination support
- Validation utilities
- Error code abstraction

This module is dependency-free (except Spring Boot core) and can be used standalone.

---

## Features

- **Exception Hierarchy** - Three-tier exception model (Functional, Technical, Security)
- **Fluent Builders** - Builder pattern with method chaining for exception creation
- **Error Codes** - Type-safe error codes with HTTP status mapping
- **Context Enrichment** - Add debugging context to exceptions
- **API Response** - Consistent REST response structure with metadata
- **Pagination** - Complete pagination support with `PageResponse<T>`
- **Preconditions** - Validation utilities with automatic exception throwing
- **Error ID Generation** - Unique error IDs for log correlation

---

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>be.aidji.boot</groupId>
    <artifactId>aidji-core</artifactId>
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
        <artifactId>aidji-core</artifactId>
    </dependency>
</dependencies>
```

---

## Exception Hierarchy

### Overview

```
AidjiException (abstract)
├── FunctionalException      # Expected business errors
├── TechnicalException       # Infrastructure/system errors
└── SecurityException        # Authentication/authorization errors
```

### FunctionalException

For **expected business errors** like validation failures, not found, conflicts:

```java
throw FunctionalException.builder(UserErrorCode.USER_NOT_FOUND)
    .message("User with email %s not found", email)
    .context("email", email)
    .context("requestId", requestId)
    .build();
```

**When to use:**
- Validation errors
- Resource not found
- Business rule violations
- Duplicate resources

### TechnicalException

For **infrastructure and system errors**:

```java
throw TechnicalException.builder(CommonErrorCode.DATABASE_ERROR)
    .message("Failed to save user to database")
    .cause(sqlException)
    .context("userId", userId)
    .build();
```

**When to use:**
- Database errors
- External service failures
- I/O errors
- Configuration errors

### SecurityException

For **authentication and authorization errors**:

```java
throw new SecurityException(
    SecurityErrorCode.ACCESS_DENIED,
    "User does not have permission to access resource"
);
```

**When to use:**
- Invalid JWT tokens
- Expired tokens
- Insufficient permissions
- Unauthorized access

### Wrapping Exceptions

Convert any exception to `TechnicalException`:

```java
try {
    // risky operation
} catch (IOException e) {
    throw TechnicalException.wrap(e);  // Uses INTERNAL_ERROR code
}
```

---

## API Response Wrapper

### ApiResponse<T>

Standardized REST API response structure:

```java
public record ApiResponse<T>(
    T data,
    ApiMetadata metadata,
    List<ApiError> errors
) {
    public boolean isSuccess() {
        return errors == null || errors.isEmpty();
    }
}
```

### Success Response

```java
User user = userService.findById(userId);
return ApiResponse.success(user);
```

**JSON Output:**
```json
{
  "data": {
    "id": 123,
    "email": "john@example.com",
    "name": "John Doe"
  },
  "metadata": {
    "timestamp": "2025-01-07T10:30:00Z",
    "traceId": "abc-def-123",
    "path": "/api/users/123"
  },
  "errors": null
}
```

### Failure Response

```java
ApiError error = ApiError.of("USER-001", "User not found");
return ApiResponse.failure(error);
```

**JSON Output:**
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
      "message": "User not found",
      "errorId": "550e8400-e29b-41d4-a716-446655440000"
    }
  ]
}
```

### Multiple Errors

```java
List<ApiError> errors = List.of(
    ApiError.of("VAL-001", "Email is required"),
    ApiError.of("VAL-002", "Password must be at least 8 characters")
);
return ApiResponse.failure(errors);
```

---

## Pagination Support

### PageResponse<T>

Complete pagination wrapper with metadata:

```java
public record PageResponse<T>(
    List<T> content,
    PageInfo page
) {
    public record PageInfo(
        int number,        // Current page (0-indexed)
        int size,          // Items per page
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
    ) {}
}
```

### Creating Paginated Response

```java
Page<User> userPage = userRepository.findAll(PageRequest.of(0, 10));

PageResponse<User> response = PageResponse.of(
    userPage.getContent(),
    userPage.getNumber(),
    userPage.getSize(),
    userPage.getTotalElements()
);
```

### Mapping Content

Transform page content while preserving pagination:

```java
PageResponse<UserDto> dtoPage = userPage.map(user ->
    new UserDto(user.getId(), user.getEmail(), user.getName())
);
```

### JSON Output

```json
{
  "content": [
    {"id": 1, "email": "user1@example.com"},
    {"id": 2, "email": "user2@example.com"}
  ],
  "page": {
    "number": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

## Utilities

### Preconditions

Validation utilities with automatic exception throwing:

```java
import static be.aidji.boot.core.util.Preconditions.*;

public void createUser(String email, String password) {
    // Throws FunctionalException if condition fails
    checkArgument(email != null, "Email is required");
    checkArgument(password.length() >= 8, "Password must be at least 8 characters");

    // Continue with valid data
    userRepository.save(new User(email, password));
}
```

**Available Methods:**

| Method | Exception | Use Case |
|--------|-----------|----------|
| `checkArgument(condition, message, args...)` | `FunctionalException` | Validate arguments |
| `checkState(condition, message, args...)` | `FunctionalException` | Validate object state |
| `checkNotNull(value, message, args...)` | `FunctionalException` | Null check |

---

## Examples

### Complete CRUD Example

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ApiResponse<User> findById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> FunctionalException.builder(UserErrorCode.NOT_FOUND)
                .message("User with id %d not found", id)
                .context("userId", id)
                .build());

        return ApiResponse.success(user);
    }

    public ApiResponse<User> create(CreateUserRequest request) {
        // Validation
        Preconditions.checkArgument(
            request.email() != null,
            "Email is required"
        );

        // Check for duplicates
        if (userRepository.existsByEmail(request.email())) {
            throw FunctionalException.builder(UserErrorCode.DUPLICATE_EMAIL)
                .message("User with email %s already exists", request.email())
                .context("email", request.email())
                .build();
        }

        // Create user
        User user = new User(request.email(), request.name());
        User saved = userRepository.save(user);

        return ApiResponse.success(saved);
    }

    public PageResponse<User> findAll(int page, int size) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));

        return PageResponse.of(
            userPage.getContent(),
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements()
        );
    }
}
```

### Custom Error Codes

```java
@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("USER-001", "User not found", 404),
    DUPLICATE_EMAIL("USER-002", "Email already exists", 409),
    INVALID_EMAIL("USER-003", "Invalid email format", 400),
    WEAK_PASSWORD("USER-004", "Password does not meet requirements", 400);

    private final String code;
    private final String message;
    private final int httpStatus;
}
```

### Exception with Full Context

```java
try {
    processPayment(orderId, amount);
} catch (PaymentGatewayException e) {
    throw TechnicalException.builder(PaymentErrorCode.GATEWAY_ERROR)
        .message("Payment gateway failed for order %s", orderId)
        .cause(e)
        .context("orderId", orderId)
        .context("amount", amount)
        .context("currency", "EUR")
        .context("gateway", "stripe")
        .context("timestamp", Instant.now())
        .build();
}
```

**Log Output:**
```
ERROR be.aidji.boot.payment.PaymentService - Payment gateway failed for order ORD-123
Error ID: 550e8400-e29b-41d4-a716-446655440000
Error Code: PAY-001
Context: {orderId=ORD-123, amount=99.99, currency=EUR, gateway=stripe, timestamp=2025-01-07T10:30:00Z}
```

---

## Version History

See the [CHANGELOG](../CHANGELOG.md) for detailed version history and release notes.

**Current Version:** `1.0.5-SNAPSHOT`

**Latest Stable:** `v1.0.4` ([View Release](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.4))

### Version Compatibility

| Aidji Core | Java | Spring Boot |
|------------|------|-------------|
| 1.0.x      | 25+  | 4.0.x       |

---

## Testing

Run tests with coverage:

```bash
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

**Current Coverage:** 77% (exceeds target of 75%)

---

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](../LICENSE) file for details.

---

## Links

- **Repository**: https://github.com/henrigvs/aidji_boot
- **Issues**: https://github.com/henrigvs/aidji_boot/issues
- **Releases**: https://github.com/henrigvs/aidji_boot/releases
- **Contributing**: [CONTRIBUTING.md](../CONTRIBUTING.md)
