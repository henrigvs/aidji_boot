# Aidji Boot

**Opinionated Spring Boot 4 framework for rapid REST API development.**

[![Version](https://img.shields.io/badge/version-1.0.5--SNAPSHOT-blue.svg)](CHANGELOG.md)
[![Latest Release](https://img.shields.io/badge/release-v1.0.4-success.svg)](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.4)
[![Java Version](https://img.shields.io/badge/java-25-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/spring%20boot-4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

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
            <version>1.0.5-SNAPSHOT</version>
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
    <version>1.0.5-SNAPSHOT</version>
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

| Module | Description | Coverage | Documentation |
|--------|-------------|----------|---------------|
| `aidji-bom` | Bill of Materials for dependency management | - | - |
| `aidji-parent` | Parent POM with build config and plugins | - | - |
| **[aidji-core](aidji-core/README.md)** | Core utilities, exceptions hierarchy, DTOs | 77% | [README](aidji-core/README.md) |
| **[aidji-web](aidji-web/README.md)** | RestClient, global exception handler, CORS, request logging | 36% | [README](aidji-web/README.md) |
| **[aidji-security](aidji-security/README.md)** | JWT authentication (cookie/header), auto-configuration | 46% | [README](aidji-security/README.md) |
| `aidji-security-webflux` | JWT authentication for reactive WebFlux applications | - | - |
| `aidji-discovery` | Service discovery integration (Consul, Eureka) | - | - |

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

## Version History

**Current Version:** `1.0.5-SNAPSHOT`

**Latest Stable Release:** [`v1.0.4`](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.4) (2025-01-XX)

See the [CHANGELOG](CHANGELOG.md) for detailed release notes and version history.

### Semantic Versioning

Aidji Boot follows [Semantic Versioning 2.0.0](https://semver.org/):
- **MAJOR** version (X.0.0): Incompatible API changes
- **MINOR** version (0.X.0): New functionality in a backward compatible manner
- **PATCH** version (0.0.X): Backward compatible bug fixes

### Recent Releases

| Version | Release Date | Highlights |
|---------|--------------|------------|
| [v1.0.4](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.4) | 2025-01-XX | Fixed threading issues, security improvements |
| [v1.0.3](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.3) | 2024-XX-XX | WebFlux security fixes |
| [v1.0.2](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.2) | 2024-XX-XX | Dependency updates |
| [v1.0.1](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.1) | 2024-XX-XX | Security configuration improvements |
| [v1.0.0](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.0) | 2024-XX-XX | Initial stable release |

---

## Building

```bash
# Build all modules
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl aidji-core -am

# Run tests with coverage
mvn test jacoco:report
```

## Contributing

Contributions are welcome! Please read the [CONTRIBUTING](CONTRIBUTING.md) guide for:
- Code style and conventions
- Git commit message format
- Versioning and tagging strategy
- Pull request process
- Development workflow

---

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## Links

- **Repository**: https://github.com/henrigvs/aidji_boot
- **Issues**: https://github.com/henrigvs/aidji_boot/issues
- **Releases**: https://github.com/henrigvs/aidji_boot/releases
- **Changelog**: [CHANGELOG.md](CHANGELOG.md)
- **Contributing Guide**: [CONTRIBUTING.md](CONTRIBUTING.md)

### Module Documentation

- [aidji-core](aidji-core/README.md) - Core utilities and exceptions
- [aidji-web](aidji-web/README.md) - Web layer components
- [aidji-security](aidji-security/README.md) - JWT authentication
