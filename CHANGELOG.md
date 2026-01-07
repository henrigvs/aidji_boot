# Changelog

All notable changes to the Aidji Boot framework will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - 1.0.5-SNAPSHOT

### Added
- ✨ **aidji-discovery**: New module for service discovery integration
- ✅ **Test Coverage**: Comprehensive test suite for aidji-core (77% coverage)
  - ApiResponse and PageResponse tests (100% coverage)
  - FunctionalException and TechnicalException tests
- 📝 **Documentation**: Added Apache 2.0 license headers to all source files
- 🔧 **LogFeignClient**: Simplified annotation design (removed errorCodeClass/errorCodeValue parameters)

### Fixed
- 🐛 **SecurityErrorCode**: Removed trailing semicolon compilation error
- 🐛 **LogFeignClient**: Fixed invalid `import module java.base` statement
- 🐛 **aidji-bom**: Fixed version property reference for aidji-discovery dependency

### Changed
- 🔄 **LogFeignClient**: Now uses `CommonErrorCode.EXTERNAL_SERVICE_ERROR` by default
- 🧪 **GlobalExceptionHandler**: Added 13 comprehensive test cases

---

## [1.0.4] - 2025-01-XX

**Tag:** `v1.0.4` ([View on GitHub](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.4))

### Fixed
- 🐛 **Threading**: Fixed loss of headers during thread execution
- 🐛 **Security**: Added null check on public paths property to prevent NPE
- 🔧 **Response Handling**: Improved response mapping consistency

### Technical Details
- Fixed thread-local context propagation for request headers
- Enhanced security configuration validation for edge cases

---

## [1.0.3] - 2024-XX-XX

**Tag:** `v1.0.3` ([View on GitHub](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.3))

### Fixed
- 🐛 **Security WebFlux**: Fixed mapping of AidjiPrincipal in reactive security context
- 🔧 **Authentication**: Improved principal object serialization

### Technical Details
- Corrected principal extraction from reactive security context
- Enhanced compatibility with Spring WebFlux authentication flow

---

## [1.0.2] - 2024-XX-XX

**Tag:** `v1.0.2` ([View on GitHub](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.2))

### Changed
- 🔄 Minor internal improvements and dependency updates
- 📦 Build configuration enhancements

---

## [1.0.1] - 2024-XX-XX

**Tag:** `v1.0.1` ([View on GitHub](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.1))

### Fixed
- 🐛 **Security WebFlux**: Modified security configuration considering authentication delegation
- 🔧 **Reactive Stack**: Improved WebFlux security filter chain

### Technical Details
- Enhanced authentication delegation mechanism in reactive security
- Fixed filter chain ordering for WebFlux applications

---

## [1.0.0] - 2024-XX-XX

**Tag:** `v1.0.0` ([View on GitHub](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.0))

### Added - Initial Release 🎉

#### Core Modules
- 🎯 **aidji-bom**: Bill of Materials for dependency management
- 🔧 **aidji-parent**: Parent POM with build configuration
- 💎 **aidji-core**: Core utilities, exceptions, and DTOs
- 🌐 **aidji-web**: Web layer with RestClient, exception handling, CORS
- 🔒 **aidji-security**: JWT authentication (servlet-based)
- ⚡ **aidji-security-webflux**: JWT authentication (reactive WebFlux)

#### Core Features
- **Exception Handling**
  - Hierarchical exception design (FunctionalException, TechnicalException, SecurityException)
  - Builder pattern with fluent API
  - Error codes with HTTP status mapping
  - Context enrichment for debugging
  - Unique error IDs for log correlation

- **API Response Wrapper**
  - `ApiResponse<T>` for consistent REST API responses
  - `PageResponse<T>` for paginated data
  - Metadata support (timestamp, traceId, path)
  - Structured error reporting

- **Security**
  - JWT token validation via JWKS endpoint
  - Support for RS256/RS384/RS512 algorithms
  - Cookie-based and header-based JWT extraction
  - Public path exclusions with AntPathMatcher
  - Integration with Keycloak, Auth0, Okta, Azure AD
  - Native Java implementation (no external JSON libraries)

- **Web Utilities**
  - Global exception handler with Spring MVC integration
  - RestClient factory with timeout configuration
  - CORS auto-configuration
  - Request logging filter with trace ID propagation
  - Feign client aspect for standardized logging

- **Auto-Configuration**
  - Spring Boot 4 auto-configuration pattern
  - Conditional beans with `@ConditionalOnMissingBean`
  - Type-safe configuration with Java records
  - Customization interfaces for extensibility

#### Technical Stack
- ☕ Java 25 with preview features
- 🍃 Spring Boot 4.0.0
- 🔨 Maven multi-module build
- 📜 Apache 2.0 License

---

## Version Numbering

Aidji Boot follows [Semantic Versioning](https://semver.org/):

- **MAJOR** version (X.0.0): Incompatible API changes
- **MINOR** version (0.X.0): New functionality in a backward compatible manner
- **PATCH** version (0.0.X): Backward compatible bug fixes

### Tag Format

Tags follow the format: `vMAJOR.MINOR.PATCH`

Examples:
- `v1.0.0` - Initial release
- `v1.0.1` - Patch release (bug fixes)
- `v1.1.0` - Minor release (new features)
- `v2.0.0` - Major release (breaking changes)

---

## Release Process

1. **Update Version**: Update version in all `pom.xml` files
2. **Update Changelog**: Document all changes in this file
3. **Create Tag**: `git tag -a vX.Y.Z -m "Release X.Y.Z"`
4. **Push Tag**: `git push origin vX.Y.Z`
5. **GitHub Release**: Create release notes on GitHub
6. **Deploy**: Publish to Maven repository

---

## Migration Guides

### Upgrading to 1.0.5

#### LogFeignClient Annotation Simplified

**Before (1.0.4):**
```java
@LogFeignClient(
    clientName = "payment-service",
    errorCodeClass = PaymentErrorCode.class,
    errorCodeValue = "PAYMENT_SERVICE_ERROR"
)
public PaymentResponse processPayment(PaymentRequest request) {
    return paymentClient.process(request);
}
```

**After (1.0.5):**
```java
@LogFeignClient(clientName = "payment-service")
public PaymentResponse processPayment(PaymentRequest request) {
    return paymentClient.process(request);
}
```

The annotation now automatically uses `CommonErrorCode.EXTERNAL_SERVICE_ERROR` for all Feign client errors. This simplifies the API without loss of functionality.

---

## Links

- **Repository**: https://github.com/henrigvs/aidji_boot
- **Issues**: https://github.com/henrigvs/aidji_boot/issues
- **Releases**: https://github.com/henrigvs/aidji_boot/releases
- **Maven Central**: https://maven.pkg.github.com/henrigvs/aidji_boot

---

## Legend

- 🎉 **Release** - Major milestone
- ✨ **Added** - New features
- 🔄 **Changed** - Changes in existing functionality
- 🗑️ **Deprecated** - Soon-to-be removed features
- ❌ **Removed** - Removed features
- 🐛 **Fixed** - Bug fixes
- 🔒 **Security** - Security improvements
- 📝 **Documentation** - Documentation changes
- 🔧 **Technical** - Internal/technical changes
