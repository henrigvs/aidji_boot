# Aidji Boot

**Opinionated Spring Boot 4 framework for rapid REST API development.**

Aidji Boot provides pre-configured, production-ready modules that follow the "convention over configuration" philosophy. Built with Java 25 and Spring Boot 4, it eliminates boilerplate while maintaining flexibility through auto-configuration.

## Features

- 🚀 **Convention over Configuration** - Works out of the box with sensible defaults
- 🔒 **JWT Security** - Cookie-based or header-based authentication with auto-configuration
- 🌐 **Web Utilities** - RestClient, global exception handling, CORS, request logging
- 🎯 **Standardized APIs** - Consistent error responses with `ApiResponse` wrapper
- 🛡️ **Type-Safe Config** - Java records for configuration properties
- ✨ **Modern Java** - Built for Java 25 with preview features enabled
- 📦 **Modular Design** - Pick only what you need, zero circular dependencies

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
            <version>1.0.2</version>
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
    <version>1.0.2</version>
</parent>
```

Then add the modules you need:

```xml
<dependencies>
    <dependency>
        <groupId>be.aidji.boot</groupId>
        <artifactId>aidji-core</artifactId>
    </dependency>
    <dependency>
        <groupId>be.aidji.boot</groupId>
        <artifactId>aidji-web</artifactId>
    </dependency>
    <dependency>
        <groupId>be.aidji.boot</groupId>
        <artifactId>aidji-security</artifactId>
    </dependency>
</dependencies>
```

## Modules

| Module | Description | Status |
|--------|-------------|--------|
| `aidji-bom` | Bill of Materials for dependency management | ✅ Available |
| `aidji-parent` | Parent POM with build config and plugins | ✅ Available |
| `aidji-core` | Core utilities, exceptions hierarchy, DTOs | ✅ Available |
| `aidji-web` | RestClient, global exception handler, CORS, request logging | ✅ Available |
| `aidji-security` | JWT authentication (cookie/header), auto-configuration | ✅ Available |
| `aidji-observability` | Metrics, distributed tracing, Prometheus | 🚧 Planned |
| `aidji-gateway` | Pre-configured API Gateway | 🚧 Planned |

## Usage Examples

### Exception Handling

```java
// Define your error codes
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND("USER-001", "User not found", 404),
    EMAIL_TAKEN("USER-002", "Email already registered", 409);
    
    // constructor and getters...
}

// Throw functional exceptions
throw new FunctionalException(UserErrorCode.USER_NOT_FOUND, 
    "User with id " + userId + " not found");

// Or use the fluent builder
throw FunctionalException.builder(UserErrorCode.EMAIL_TAKEN)
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
