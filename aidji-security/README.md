# Aidji Security

JWT authentication module for Spring MVC applications with JWKS-based token validation.

[![Code Coverage](https://img.shields.io/badge/coverage-46%25-yellow)](target/site/jacoco/index.html)
[![Java Version](https://img.shields.io/badge/java-25-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](../LICENSE)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [JWT Validation](#jwt-validation)
- [Supported Identity Providers](#supported-identity-providers)
- [Configuration](#configuration)
- [Authentication Flow](#authentication-flow)
- [Public Paths](#public-paths)
- [Cookie-Based JWT](#cookie-based-jwt)
- [Custom Principal](#custom-principal)
- [Error Handling](#error-handling)
- [Examples](#examples)
- [Version History](#version-history)

---

## Overview

**aidji-security** provides production-ready JWT authentication for Spring MVC applications:
- Token validation via external JWKS endpoints (Keycloak, Auth0, Okta, Azure AD)
- Cookie-based or header-based JWT extraction
- Public path exclusions
- Custom principal object (`AidjiPrincipal`)
- Native Java implementation (no external JSON libraries)
- Auto-configuration for Spring Boot 4

This module is designed for applications using **external Identity Providers** (IdP) for authentication.

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
- **Spring Security Integration** - Seamless integration with Spring Security filter chain

---

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>be.aidji.boot</groupId>
    <artifactId>aidji-security</artifactId>
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
        <artifactId>aidji-security</artifactId>
    </dependency>
</dependencies>
```

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

### 2. Create UserDetailsService (Required)

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password("")  // Not used for JWT
            .authorities(user.getRoles().toArray(new String[0]))
            .build();
    }
}
```

### 3. Access Authenticated User

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ApiResponse<UserDto> getCurrentUser(Authentication authentication) {
        AidjiPrincipal principal = (AidjiPrincipal) authentication.getPrincipal();

        UserDto user = new UserDto(
            principal.userId(),
            principal.email(),
            principal.roles()
        );

        return ApiResponse.success(user);
    }
}
```

---

## JWT Validation

### How It Works

1. **Extract JWT** - From cookie or Authorization header
2. **Fetch JWKS** - Download public keys from IdP (cached)
3. **Validate Signature** - Verify JWT signature using RS256/RS384/RS512
4. **Validate Claims** - Check expiration, issuer, audience
5. **Load User** - Call `UserDetailsService` to load user details
6. **Set Context** - Populate Spring Security context with `AidjiPrincipal`

### Supported Algorithms

| Algorithm | Key Type | Description |
|-----------|----------|-------------|
| RS256 | RSA 2048+ | Recommended (default) |
| RS384 | RSA 2048+ | Higher security |
| RS512 | RSA 2048+ | Maximum security |

### Token Structure

```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.signature
```

**Decoded:**
```json
{
  "sub": "user-id-123",
  "email": "john@example.com",
  "name": "John Doe",
  "roles": ["ROLE_USER", "ROLE_ADMIN"],
  "iss": "https://your-keycloak.com/realms/your-realm",
  "exp": 1735999999,
  "iat": 1735999000
}
```

### JWKS Endpoint

The JWKS endpoint must return public keys in this format:

```json
{
  "keys": [
    {
      "kid": "key-id-123",
      "kty": "RSA",
      "alg": "RS256",
      "use": "sig",
      "n": "modulus...",
      "e": "AQAB"
    }
  ]
}
```

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

### Generic OIDC Provider

Any OIDC-compliant provider works. Find the JWKS URL in `.well-known/openid-configuration`:

```bash
curl https://your-idp.com/.well-known/openid-configuration | jq .jwks_uri
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

### Environment Variables

```bash
# JWKS endpoint
AIDJI_SECURITY_JWT_PUBLIC_KEY_URL=https://keycloak.example.com/realms/my-realm/protocol/openid-connect/certs

# Cookie configuration
AIDJI_SECURITY_JWT_COOKIE_BASED=true
AIDJI_SECURITY_JWT_COOKIE_NAME=access_token

# Cache TTL (1 hour)
AIDJI_SECURITY_JWT_PUBLIC_KEY_CACHE_TTL_SECONDS=3600
```

### Disabling Security

```yaml
aidji:
  security:
    enabled: false  # Disable all security features
```

---

## Authentication Flow

### 1. User Login (External IdP)

User logs in via your Identity Provider (Keycloak, Auth0, etc.) and receives a JWT:

```http
POST /auth/login
Host: your-idp.com

{
  "username": "john@example.com",
  "password": "secret"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 3600,
  "token_type": "Bearer"
}
```

### 2. Client Stores JWT

**Cookie-based (recommended for web apps):**
```http
Set-Cookie: access_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...; HttpOnly; Secure; SameSite=Strict
```

**Header-based (for mobile/SPA):**
```javascript
localStorage.setItem('access_token', 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...');
```

### 3. Client Sends Request

**Cookie-based:**
```http
GET /api/users/me
Cookie: access_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Header-based:**
```http
GET /api/users/me
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4. Aidji Security Validates JWT

1. Extract JWT from cookie/header
2. Fetch JWKS public keys (cached)
3. Validate signature and claims
4. Call `UserDetailsService.loadUserByUsername(email)`
5. Create `AidjiPrincipal` and populate `SecurityContext`

### 5. Controller Receives Authenticated User

```java
@GetMapping("/me")
public UserDto getCurrentUser(Authentication auth) {
    AidjiPrincipal principal = (AidjiPrincipal) auth.getPrincipal();
    return new UserDto(principal.userId(), principal.email());
}
```

---

## Public Paths

### Configuration

Exclude endpoints from authentication:

```yaml
aidji:
  security:
    jwt:
      public-paths:
        - /actuator/health      # Health check
        - /actuator/info        # Application info
        - /api/public/**        # Public API endpoints
        - /swagger-ui/**        # Swagger UI
        - /v3/api-docs/**       # OpenAPI docs
        - /error                # Error page
```

### Path Matching

Uses Spring's `AntPathMatcher`:

| Pattern | Matches | Example |
|---------|---------|---------|
| `/api/public/**` | All paths under `/api/public` | `/api/public/users` |
| `/api/*/info` | Single level wildcard | `/api/users/info` |
| `/api/**/info` | Multi-level wildcard | `/api/v1/users/info` |
| `/health` | Exact match | `/health` |

---

## Cookie-Based JWT

### Why Cookie-Based?

**Security Benefits:**
- **HttpOnly** - JavaScript cannot access the token (XSS protection)
- **Secure** - Only sent over HTTPS
- **SameSite** - CSRF protection
- **Automatic** - Browser handles token storage and transmission

### Configuration

```yaml
aidji:
  security:
    jwt:
      cookie-based: true
      cookie-name: access_token  # Customize cookie name
```

### Setting the Cookie (After Login)

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        // Authenticate with IdP
        String jwt = idpClient.login(request.email(), request.password());

        // Set HTTP-only cookie
        Cookie cookie = new Cookie("access_token", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);  // HTTPS only
        cookie.setPath("/");
        cookie.setMaxAge(3600);  // 1 hour
        cookie.setAttribute("SameSite", "Strict");

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}
```

### Client-Side (No Code Required)

Browser automatically sends cookie with every request:

```http
GET /api/users/me
Cookie: access_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Custom Principal

### AidjiPrincipal

The authenticated user is represented by `AidjiPrincipal`:

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

### Accessing in Controllers

**Via Authentication:**
```java
@GetMapping("/me")
public UserDto getCurrentUser(Authentication auth) {
    AidjiPrincipal principal = (AidjiPrincipal) auth.getPrincipal();
    return new UserDto(principal.userId(), principal.email(), principal.roles());
}
```

**Via Principal Parameter:**
```java
@GetMapping("/me")
public UserDto getCurrentUser(Principal principal) {
    AidjiPrincipal aidjiPrincipal = (AidjiPrincipal) principal;
    return new UserDto(aidjiPrincipal.userId(), aidjiPrincipal.email());
}
```

**Via SecurityContextHolder:**
```java
@Service
public class UserService {

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AidjiPrincipal principal = (AidjiPrincipal) auth.getPrincipal();

        return userRepository.findById(principal.userId())
            .orElseThrow(() -> new FunctionalException(...));
    }
}
```

---

## Error Handling

### Invalid Token

**Response (401 Unauthorized):**
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

### Expired Token

**Response (401 Unauthorized):**
```json
{
  "errors": [
    {
      "code": "SECU-001",
      "message": "Bearer token expired"
    }
  ]
}
```

### Access Denied

**Response (403 Forbidden):**
```json
{
  "errors": [
    {
      "code": "SECU-003",
      "message": "Access denied"
    }
  ]
}
```

### Error Codes

| Code | Message | HTTP Status | Description |
|------|---------|-------------|-------------|
| `SECU-001` | Bearer token expired | 401 | JWT expired |
| `SECU-002` | Bearer token not valid | 401 | Invalid signature or claims |
| `SECU-003` | Access denied | 403 | Insufficient permissions |
| `SECU-004` | Unauthorized | 403 | Not authenticated |

---

## Examples

### Complete Application Setup

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
aidji:
  security:
    enabled: true
    jwt:
      public-key-url: https://keycloak.example.com/realms/my-realm/protocol/openid-connect/certs
      cookie-based: true
      cookie-name: access_token
      public-key-cache-ttl-seconds: 3600
      public-paths:
        - /actuator/health
        - /api/public/**

spring:
  application:
    name: my-app
```

### UserDetailsService Implementation

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password("")  // Not used for JWT
            .authorities(user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList())
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!user.isActive())
            .build();
    }
}
```

### Secure Controller

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserDto> getCurrentUser(Authentication auth) {
        AidjiPrincipal principal = (AidjiPrincipal) auth.getPrincipal();

        User user = userService.findById(principal.userId());

        UserDto dto = new UserDto(
            user.getId(),
            user.getEmail(),
            user.getName(),
            principal.roles()
        );

        return ApiResponse.success(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PageResponse<UserDto> listUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return userService.findAll(page, size);
    }
}
```

### Public Endpoint

```java
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("version", "1.0.0", "name", "My App");
    }
}
```

### Custom Security Configuration

```java
@Configuration
@EnableMethodSecurity  // Enable @PreAuthorize, @Secured, etc.
public class SecurityConfig {

    @Bean
    public AidjiSecurityCustomizer securityCustomizer() {
        return http -> {
            // Add custom security rules
            http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
            );

            // Add custom filters
            http.addFilterBefore(
                new CustomLoggingFilter(),
                UsernamePasswordAuthenticationFilter.class
            );
        };
    }
}
```

---

## Testing

### Testing Secured Endpoints

```java
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void shouldReturnCurrentUser() throws Exception {
        User user = new User("123", "john@example.com", "John Doe");
        when(userService.findById("123")).thenReturn(user);

        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized());
    }
}
```

---

## Version History

See the [CHANGELOG](../CHANGELOG.md) for detailed version history and release notes.

**Current Version:** `1.0.5-SNAPSHOT`

**Latest Stable:** `v1.0.4` ([View Release](https://github.com/henrigvs/aidji_boot/releases/tag/v1.0.4))

### Recent Changes (1.0.4)

- Fixed loss of headers during thread execution
- Added null check on public paths property to prevent NPE

---

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](../LICENSE) file for details.

---

## Links

- **Repository**: https://github.com/henrigvs/aidji_boot
- **Issues**: https://github.com/henrigvs/aidji_boot/issues
- **Releases**: https://github.com/henrigvs/aidji_boot/releases
- **Contributing**: [CONTRIBUTING.md](../CONTRIBUTING.md)
