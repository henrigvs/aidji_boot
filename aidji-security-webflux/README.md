# Aidji Security WebFlux

JWT authentication module for Spring WebFlux reactive applications with JWKS-based token validation.

[![Java Version](https://img.shields.io/badge/java-25-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](../LICENSE)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Differences from aidji-security](#differences-from-aidji-security)
- [Supported Identity Providers](#supported-identity-providers)
- [Configuration](#configuration)
- [Authentication Flow](#authentication-flow)
- [Public Paths](#public-paths)
- [Cookie-Based JWT](#cookie-based-jwt)
- [Custom Principal](#custom-principal)
- [Error Handling](#error-handling)
- [Examples](#examples)

---

## Overview

**aidji-security-webflux** provides production-ready JWT authentication for Spring WebFlux reactive applications:
- Token validation via external JWKS endpoints (Keycloak, Auth0, Okta, Azure AD)
- Cookie-based or header-based JWT extraction
- Reactive filters and handlers
- Public path exclusions
- Custom principal object (`AidjiPrincipal`)
- Native Java implementation (no external JSON libraries)
- Auto-configuration for Spring Boot 4 WebFlux

This module is designed for **reactive applications** using **external Identity Providers** (IdP) for authentication.

---

## Features

- **JWKS Validation** - Validates JWT tokens using public keys from JWKS endpoint
- **RS256/RS384/RS512** - Supports RSA signature algorithms
- **Cookie & Header Support** - Extract JWT from cookie or Authorization header
- **Public Key Caching** - Caches JWKS public keys with configurable TTL
- **Public Paths** - Exclude endpoints from authentication (health checks, Swagger, etc.)
- **Custom Principal** - `AidjiPrincipal` with user ID, email, roles
- **Auto-Configuration** - Zero-config Spring Boot integration
- **Native Java** - No external JSON libraries required
- **Reactive Security** - Full reactive support with Spring Security WebFlux
- **Context Propagation** - Trace context propagates through reactive chains

---

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>be.aidji.boot</groupId>
    <artifactId>aidji-security-webflux</artifactId>
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
        <artifactId>aidji-security-webflux</artifactId>
    </dependency>
</dependencies>
```

**IMPORTANT:** Make sure you're using `spring-boot-starter-webflux` instead of `spring-boot-starter-web`.

---

## Quick Start

### 1. Configure JWKS Endpoint

```yaml
aidji:
  security:
    enabled: true
    jwt:
      public-key-url: https://your-keycloak.com/realms/your-realm/protocol/openid-connect/certs
      cookie-based: true
      cookie-name: access_token
      public-paths:
        - /actuator/health
        - /api/public/**
```

### 2. Create ReactiveUserDetailsService (Required)

```java
@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByEmail(username)
            .map(user -> org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password("")  // Not used for JWT
                .authorities(user.getRoles().toArray(new String[0]))
                .build())
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)));
    }
}
```

### 3. Access Authenticated User

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public Mono<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        AidjiPrincipal principal = (AidjiPrincipal) authentication.getPrincipal();

        UserDto user = new UserDto(
            principal.userId(),
            principal.email(),
            principal.roles()
        );

        return Mono.just(ApiResponse.success(user));
    }
}
```

---

## Differences from aidji-security

| Feature | aidji-security (Servlet) | aidji-security-webflux (Reactive) |
|---------|-------------------------|----------------------------------|
| **Web Stack** | Spring MVC (Servlet) | Spring WebFlux (Reactive) |
| **Filters** | `OncePerRequestFilter` | `WebFilter` |
| **Security Chain** | `HttpSecurity` | `ServerHttpSecurity` |
| **User Details Service** | `UserDetailsService` | `ReactiveUserDetailsService` |
| **Controllers** | Blocking (`@RestController`) | Reactive (`Mono`, `Flux`) |
| **Authentication** | `Authentication` | `Mono<Authentication>` |
| **Principal Access** | `SecurityContextHolder` | `ReactiveSecurityContextHolder` |
| **Performance** | Thread-per-request | Non-blocking I/O |

**When to use:**
- **aidji-security**: Traditional Spring MVC applications, blocking I/O
- **aidji-security-webflux**: Reactive applications, high concurrency, non-blocking I/O

---

## Supported Identity Providers

### Keycloak

```yaml
aidji:
  security:
    jwt:
      public-key-url: https://keycloak.example.com/realms/my-realm/protocol/openid-connect/certs
```

### Auth0

```yaml
aidji:
  security:
    jwt:
      public-key-url: https://YOUR_DOMAIN.auth0.com/.well-known/jwks.json
```

### Okta

```yaml
aidji:
  security:
    jwt:
      public-key-url: https://YOUR_DOMAIN.okta.com/oauth2/default/v1/keys
```

### Azure AD

```yaml
aidji:
  security:
    jwt:
      public-key-url: https://login.microsoftonline.com/YOUR_TENANT_ID/discovery/v2.0/keys
```

---

## Configuration

### Complete Configuration Reference

```yaml
aidji:
  security:
    enabled: true  # Enable security module (default: true)

    jwt:
      # JWKS endpoint (required)
      public-key-url: https://your-idp.com/.well-known/jwks.json

      # Public key cache TTL in seconds (default: 3600 = 1 hour)
      public-key-cache-ttl-seconds: 3600

      # Cookie-based JWT (default: false)
      cookie-based: true
      cookie-name: access_token  # Default: access_token

      # Public paths - no authentication required
      public-paths:
        - /actuator/health
        - /actuator/info
        - /api/public/**
        - /swagger-ui/**
        - /v3/api-docs/**
```

---

## Authentication Flow

### Reactive Flow

1. **User Login** - Via external IdP, receives JWT
2. **Client Stores JWT** - Cookie (HTTP-only) or local storage
3. **Client Sends Request** - With JWT in cookie/header
4. **Reactive Filter** - `JwtAuthenticationWebFilter` extracts JWT
5. **Validate Token** - Async JWKS validation (non-blocking)
6. **Load User** - `ReactiveUserDetailsService.findByUsername()` (reactive)
7. **Set Context** - `ReactiveSecurityContextHolder.setContext()` (propagates through reactive chain)
8. **Controller** - Receives `Mono<Authentication>`

---

## Public Paths

```yaml
aidji:
  security:
    jwt:
      public-paths:
        - /actuator/health
        - /api/public/**
        - /swagger-ui/**
```

---

## Cookie-Based JWT

Reactive cookie handling:

```java
@PostMapping("/login")
public Mono<ResponseEntity<Void>> login(@RequestBody LoginRequest request, ServerHttpResponse response) {
    return idpClient.login(request.email(), request.password())
        .map(jwt -> {
            // Set HTTP-only cookie
            ResponseCookie cookie = ResponseCookie.from("access_token", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(3600)
                .sameSite("Strict")
                .build();

            response.addCookie(cookie);
            return ResponseEntity.ok().build();
        });
}
```

---

## Custom Principal

Same `AidjiPrincipal` as aidji-security:

```java
public record AidjiPrincipal(
    String userId,
    String email,
    List<String> roles
) implements Principal {

    @Override
    public String getName() {
        return email;
    }
}
```

### Accessing in Reactive Controllers

**Via Authentication:**
```java
@GetMapping("/me")
public Mono<UserDto> getCurrentUser(Mono<Authentication> authentication) {
    return authentication
        .map(auth -> (AidjiPrincipal) auth.getPrincipal())
        .map(principal -> new UserDto(principal.userId(), principal.email()));
}
```

**Via ReactiveSecurityContextHolder:**
```java
@Service
public class UserService {

    public Mono<User> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (AidjiPrincipal) ctx.getAuthentication().getPrincipal())
                .flatMap(principal -> userRepository.findById(principal.userId()));
    }
}
```

---

## Error Handling

Standardized error responses:

```json
{
  "data": null,
  "metadata": {
    "timestamp": "2025-01-07T10:30:00Z",
    "path": "/api/users/me"
  },
  "errors": [
    {
      "code": "SECU-002",
      "message": "Bearer token not valid"
    }
  ]
}
```

---

## Examples

### Complete Application

```java
@SpringBootApplication
public class ReactiveApp {
    public static void main(String[] args) {
        SpringApplication.run(ReactiveApp.class, args);
    }
}
```

**Reactive Controller:**
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Mono<ApiResponse<UserDto>> getCurrentUser(Mono<Authentication> authentication) {
        return authentication
                .map(auth -> (AidjiPrincipal) auth.getPrincipal())
                .flatMap(principal -> userService.findById(principal.userId()))
                .map(user -> new UserDto(user.getId(), user.getEmail()))
                .map(ApiResponse::success);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Flux<UserDto> listUsers() {
        return userService.findAll()
                .map(user -> new UserDto(user.getId(), user.getEmail()));
    }
}
```

---

## Testing

```java
@WebFluxTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void shouldReturnCurrentUser() {
        webTestClient.get()
            .uri("/api/users/me")
            .exchange()
            .expectStatus().isOk();
    }
}
```

---

## License

Apache License 2.0 - see [LICENSE](../LICENSE)

---

## Links

- **Repository**: https://github.com/henrigvs/aidji_boot
- **aidji-security (Servlet)**: [README](../aidji-security/README.md)
- **Spring Security Reactive**: https://docs.spring.io/spring-security/reference/reactive/
