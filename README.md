# Aidji Boot

Opinionated Spring Boot 4 framework for rapid application development.

## Features

- 🚀 **Convention over Configuration** - Works out of the box with zero config
- 🔒 **Security** - JWT authentication with HttpOnly cookies (coming soon)
- 📊 **Observability** - Prometheus, Micrometer, distributed tracing (coming soon)
- 🌐 **API Gateway** - Pre-configured Spring Cloud Gateway (coming soon)
- ✨ **Modern Java** - Built for Java 25 with preview features

## Requirements

- Java 25+
- Maven 3.9+
- Spring Boot 4.0+

## Quick Start

Add the BOM to your project:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>be.aidji.boot</groupId>
            <artifactId>aidji-bom</artifactId>
            <version>0.1.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Or use the parent POM:

```xml
<parent>
    <groupId>be.aidji.boot</groupId>
    <artifactId>aidji-parent</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</parent>
```

Then add the modules you need:

```xml
<dependencies>
    <dependency>
        <groupId>be.aidji.boot</groupId>
        <artifactId>aidji-core</artifactId>
    </dependency>
</dependencies>
```

## Modules

| Module | Description | Status |
|--------|-------------|--------|
| `aidji-bom` | Bill of Materials | ✅ Available |
| `aidji-parent` | Parent POM with build config | ✅ Available |
| `aidji-core` | Exceptions, DTOs, utilities | ✅ Available |
| `aidji-security` | JWT, OAuth2, RBAC | 🚧 Planned |
| `aidji-web` | RestClient, error handling | 🚧 Planned |
| `aidji-observability` | Metrics, tracing | 🚧 Planned |
| `aidji-gateway` | API Gateway | 🚧 Planned |

## Usage Examples

### Exception Handling

```java
// Define your error codes
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND("USER-001", "User not found", 404),
    EMAIL_TAKEN("USER-002", "Email already registered", 409);
    
    // constructor and getters...
}

// Throw business exceptions
throw new BusinessException(UserErrorCode.USER_NOT_FOUND, 
    "User with id " + userId + " not found");

// Or use the fluent builder
throw BusinessException.builder(UserErrorCode.EMAIL_TAKEN)
    .message("Email %s is already registered", email)
    .context("email", email)
    .build();
```

### Validation

```java


public User findById(Long id) {
    requireNonNull(id, "User ID is required");
    return requireFound(
            userRepository.findById(id).orElse(null),
            UserErrorCode.USER_NOT_FOUND,
            "User with id " + id + " not found"
    );
}
```

### API Response

```java
@GetMapping("/{id}")
public ApiResponse<UserDto> getUser(@PathVariable Long id) {
    UserDto user = userService.findById(id);
    return ApiResponse.success(user);
}
```

## Building

```bash
# Build all modules
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl aidji-core -am
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please read the contributing guidelines before submitting a PR.
