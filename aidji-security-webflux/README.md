# Aidji Security WebFlux

Security layer components for reactive Spring WebFlux applications.

## Features

- **JWT-based authentication** via JWKS (JSON Web Key Set)
- **Reactive/Non-blocking** - Built for WebFlux with Reactor
- **Cookie or Header-based** token extraction
- **Auto-configuration** - Zero config to get started
- **Customizable** - Override any bean or security rule

## Supported Identity Providers

- Keycloak
- Auth0
- Okta
- Azure AD
- Any OIDC-compliant provider with JWKS endpoint

## Quick Start

### 1. Add dependency

```xml
<dependency>
    <groupId>be.aidji.boot</groupId>
    <artifactId>aidji-security-webflux</artifactId>
</dependency>
```

### 2. Configure properties

```yaml
aidji:
  security:
    jwt:
      public-key-url: https://your-idp.com/.well-known/jwks.json
      cookie-based: true
      cookie-name: jwt-security-principal
    security:
      public-paths:
        - /api/auth/**
        - /actuator/health
```

### 3. Provide ReactiveUserDetailsService

```java
@Bean
public ReactiveUserDetailsService reactiveUserDetailsService() {
    return username -> userRepository.findByUsername(username)
            .map(user -> User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(user.getAuthorities())
                    .build());
}
```

That's it! Your WebFlux application now has JWT authentication.

## Configuration

### Properties

| Property | Default | Description |
|----------|---------|-------------|
| `aidji.security.enabled` | `true` | Enable/disable security |
| `aidji.security.jwt.enabled` | `true` | Enable/disable JWT |
| `aidji.security.jwt.public-key-url` | *(required)* | JWKS endpoint URL |
| `aidji.security.jwt.public-key-cache-ttl-seconds` | `3600` | Cache TTL for public keys |
| `aidji.security.jwt.cookie-based` | `false` | Use cookie instead of Authorization header |
| `aidji.security.jwt.cookie-name` | `jwt-security-principal` | Cookie name |
| `aidji.security.security.public-paths` | `[/api/auth/**, /actuator/health]` | Paths that don't require auth |

## Customization

### Custom Security Rules

```java
@Bean
public AidjiSecurityCustomizer mySecurityCustomizer() {
    return http -> http
        .authorizeExchange(auth -> auth
            .pathMatchers("/admin/**").hasRole("ADMIN")
        );
}
```

### Override Beans

Override any auto-configured bean by declaring your own:

```java
@Bean
public JwtTokenVerificator jwtTokenVerificator(AidjiSecurityProperties properties) {
    // Your custom implementation
}
```

## Architecture

```
Request → JwtAuthenticationWebFilter → Extract JWT → Validate → ReactiveUserDetailsService → SecurityContext
```

### Key Components

- **JwtTokenVerificator** - Validates JWT using JWKS public keys
- **JwtAuthenticationWebFilter** - Reactive filter that processes JWT
- **ReactiveUserDetailsService** - Loads user details (provided by app)
- **ServerAccessDeniedHandler** - Handles 403 errors
- **ServerAuthenticationEntryPoint** - Handles 401 errors

## Differences from aidji-security

| Feature | aidji-security | aidji-security-webflux |
|---------|----------------|------------------------|
| Stack | Spring MVC (Servlet) | Spring WebFlux (Reactive) |
| Filter | `OncePerRequestFilter` | `WebFilter` |
| Security Config | `SecurityFilterChain` | `SecurityWebFilterChain` |
| User Service | `UserDetailsService` | `ReactiveUserDetailsService` |
| APIs | Blocking | Reactive (Mono/Flux) |

## Example Application

```java
@SpringBootApplication
public class MyReactiveApp {

    public static void main(String[] args) {
        SpringApplication.run(MyReactiveApp.class, args);
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(user -> User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities(user.getAuthorities())
                        .build());
    }
}

@RestController
@RequestMapping("/api")
class MyController {

    @GetMapping("/public")
    public Mono<String> publicEndpoint() {
        return Mono.just("Public access");
    }

    @GetMapping("/protected")
    public Mono<String> protectedEndpoint(@AuthenticationPrincipal UserDetails user) {
        return Mono.just("Hello " + user.getUsername());
    }
}
```

## License

Apache License 2.0
