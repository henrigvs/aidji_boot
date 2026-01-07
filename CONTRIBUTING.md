# Contributing to Aidji Boot

Thank you for your interest in contributing to Aidji Boot! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Versioning and Tagging](#versioning-and-tagging)
- [Commit Message Convention](#commit-message-convention)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Release Process](#release-process)

---

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help maintain a welcoming environment for all contributors

---

## Getting Started

### Prerequisites

- Java 25 or higher
- Maven 3.9 or higher
- Git
- Your favorite IDE (IntelliJ IDEA recommended)

### Setup

1. **Fork the repository**
   ```bash
   # Fork on GitHub, then clone your fork
   git clone https://github.com/YOUR_USERNAME/aidji_boot.git
   cd aidji_boot
   ```

2. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/henrigvs/aidji_boot.git
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run tests**
   ```bash
   mvn test
   ```

---

## Development Workflow

### 1. Create a Feature Branch

```bash
# Update your main branch
git checkout main
git pull upstream main

# Create a feature branch
git checkout -b feature/my-new-feature
```

### Branch Naming Convention

- `feature/` - New features (e.g., `feature/add-rate-limiting`)
- `fix/` - Bug fixes (e.g., `fix/jwt-validation-npe`)
- `docs/` - Documentation only (e.g., `docs/update-readme`)
- `refactor/` - Code refactoring (e.g., `refactor/exception-hierarchy`)
- `test/` - Adding or updating tests (e.g., `test/globalexceptionhandler`)
- `chore/` - Maintenance tasks (e.g., `chore/update-dependencies`)

### 2. Make Your Changes

- Write clean, readable code
- Follow the coding standards (see below)
- Add tests for new functionality
- Update documentation as needed

### 3. Commit Your Changes

Follow the commit message convention (see below):

```bash
git add .
git commit -m "[FEATURE] Add rate limiting support"
```

### 4. Keep Your Branch Updated

```bash
git fetch upstream
git rebase upstream/main
```

### 5. Push Your Changes

```bash
git push origin feature/my-new-feature
```

---

## Versioning and Tagging

Aidji Boot follows **[Semantic Versioning 2.0.0](https://semver.org/)**.

### Version Format

```
MAJOR.MINOR.PATCH
```

- **MAJOR**: Incompatible API changes (breaking changes)
- **MINOR**: New functionality, backward compatible
- **PATCH**: Backward compatible bug fixes

### Examples

| Version | Type | Description |
|---------|------|-------------|
| `1.0.0` | Initial | First stable release |
| `1.0.1` | Patch | Bug fix release |
| `1.1.0` | Minor | New features added |
| `2.0.0` | Major | Breaking API changes |

### Pre-release Versions

- Development: `1.0.5-SNAPSHOT`
- Release Candidate: `1.1.0-RC1`
- Beta: `2.0.0-beta.1`

### Tag Naming Convention

Tags must follow the format: `vMAJOR.MINOR.PATCH`

**Examples:**
```bash
v1.0.0    # Correct
v1.0.1    # Correct
v2.0.0    # Correct
1.0.0     # ❌ Wrong - missing 'v' prefix
V1.0.0    # ❌ Wrong - uppercase 'V'
```

### Creating a Tag

```bash
# Annotated tag (recommended)
git tag -a v1.0.5 -m "Release 1.0.5"

# Lightweight tag (not recommended for releases)
git tag v1.0.5

# Push tag to remote
git push origin v1.0.5

# Push all tags
git push origin --tags
```

### Viewing Tags

```bash
# List all tags
git tag

# List tags with annotations
git tag -l -n9

# Show specific tag
git show v1.0.4

# List tags matching pattern
git tag -l "v1.0.*"
```

### Deleting a Tag

```bash
# Delete local tag
git tag -d v1.0.5

# Delete remote tag
git push origin :refs/tags/v1.0.5
# or
git push origin --delete v1.0.5
```

### Version Bumping Strategy

#### PATCH (Bug Fixes)

**When to bump:**
- Bug fixes
- Security patches
- Documentation fixes
- Dependency updates (patch versions)

**Example:** `1.0.4` → `1.0.5`

#### MINOR (New Features)

**When to bump:**
- New features (backward compatible)
- New modules added
- Deprecating functionality (not removing)
- Dependency updates (minor versions)

**Example:** `1.0.5` → `1.1.0`

#### MAJOR (Breaking Changes)

**When to bump:**
- Breaking API changes
- Removing deprecated features
- Major architecture changes
- Java version upgrade
- Spring Boot major version upgrade

**Example:** `1.1.0` → `2.0.0`

---

## Commit Message Convention

We follow a structured commit message format with prefixes:

### Format

```
[PREFIX] Short description (50 chars max)

Optional longer description explaining the change.
Can be multiple lines.

Fixes #123
```

### Prefixes

| Prefix | Usage | Example |
|--------|-------|---------|
| `[FEATURE]` | New features | `[FEATURE] Add rate limiting support` |
| `[FIX]` | Bug fixes | `[FIX] Null pointer in JWT validation` |
| `[IMP]` | Improvements | `[IMP] Optimize JWKS caching` |
| `[REFACTOR]` | Code refactoring | `[REFACTOR] Simplify exception builder` |
| `[TEST]` | Adding/updating tests | `[TEST] Add GlobalExceptionHandler tests` |
| `[DOCS]` | Documentation | `[DOCS] Update security module README` |
| `[CHORE]` | Maintenance | `[CHORE] Update dependencies` |
| `[BREAKING]` | Breaking changes | `[BREAKING] Remove deprecated ErrorCode methods` |

### Examples

**Good:**
```
[FIX] Loss of header during thread execution

Fixed an issue where request headers were lost when
processing requests in separate threads. Added proper
thread-local context propagation.

Fixes #42
```

**Good:**
```
[FEATURE] Add discovery module

New aidji-discovery module for service discovery integration.
Supports Consul and Eureka out of the box.
```

**Bad:**
```
fix bug
```

**Bad:**
```
updated some files
```

### Commit Message Best Practices

1. **Use imperative mood** - "Add feature" not "Added feature"
2. **Capitalize the first letter** after the prefix
3. **No period at the end** of the summary line
4. **Separate subject from body** with a blank line
5. **Wrap body at 72 characters**
6. **Reference issues** - `Fixes #123`, `Closes #456`, `Refs #789`

---

## Coding Standards

### Java Conventions

1. **Use Java 25 features**
   - Records for DTOs and configuration
   - Pattern matching where applicable
   - Text blocks for multi-line strings

2. **Code Style**
   - 4 spaces for indentation (no tabs)
   - Max line length: 120 characters
   - Opening brace on same line
   - One statement per line

3. **Naming**
   - Classes: `PascalCase`
   - Methods: `camelCase`
   - Constants: `UPPER_SNAKE_CASE`
   - Packages: `lowercase`

4. **Java Records**
   ```java
   // Use records for DTOs
   public record UserDto(
           Long id,
           String email,
           String name
   ) {
       // Compact constructor for validation
       public UserDto {
           Objects.requireNonNull(email, "Email is required");
       }
   }
   ```

5. **Optional Usage**
   - Use `Optional<T>` for return types
   - **Never** use `Optional` for parameters
   - **Never** use `Optional` for fields

6. **Exception Handling**
   - Use framework exceptions (`FunctionalException`, `TechnicalException`)
   - Always provide error codes
   - Add context for debugging
   ```java
   throw FunctionalException.builder(UserErrorCode.NOT_FOUND)
       .message("User with id %d not found", userId)
       .context("userId", userId)
       .build();
   ```

### Spring Boot Conventions

1. **No `@Component` on framework classes**
   - Use `@AutoConfiguration` classes
   - Register beans with `@Bean` methods

2. **Conditional Beans**
   ```java
   @Bean
   @ConditionalOnMissingBean
   public MyService myService() {
       return new MyServiceImpl();
   }
   ```

3. **Configuration Properties**
   ```java
   @ConfigurationProperties(prefix = "aidji.module")
   public record ModuleProperties(
           boolean enabled,
           String apiKey
   ) {}
   ```

### Documentation

1. **Javadoc Required For:**
   - All public classes
   - All public methods
   - Complex algorithms

2. **Javadoc Format:**
   ```java
   /**
    * Validates a JWT token using JWKS public keys.
    *
    * @param token the JWT token to validate
    * @return validated claims from the token
    * @throws SecurityException if token is invalid or expired
    */
   public Claims validateToken(String token) {
       // ...
   }
   ```

3. **Package Documentation**
   - Every package must have `package-info.java`
   ```java
   /**
    * Core exception handling for Aidji Boot.
    */
   package be.aidji.boot.core.exception;
   ```

4. **License Headers**
   - All Java files must have Apache 2.0 header
   - Use the standard format (already in existing files)

---

## Testing Guidelines

### Test Coverage Requirements

- **Minimum**: 75% code coverage
- **Target**: 85% code coverage
- **DTOs/Records**: 100% coverage expected

### Test Structure

Use JUnit 5 with nested test classes:

```java
@DisplayName("UserService")
class UserServiceTest {

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() {
            // Given
            Long userId = 1L;
            User expected = new User(userId, "test@example.com");

            // When
            User actual = userService.findById(userId);

            // Then
            assertThat(actual).isEqualTo(expected);
        }
    }
}
```

### Test Naming

- Test classes: `*Test.java`
- Integration tests: `*IT.java`
- Test methods: Descriptive names with `should` prefix

### Assertions

Use AssertJ for fluent assertions:

```java
assertThat(response.getData()).isNotNull();
assertThat(response.getErrors()).isEmpty();
assertThat(response.isSuccess()).isTrue();
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report

# Skip tests
mvn install -DskipTests
```

---

## Pull Request Process

### Before Submitting

1. ✅ Code compiles without errors
2. ✅ All tests pass locally
3. ✅ Code coverage ≥ 75%
4. ✅ No new compiler warnings
5. ✅ Documentation updated
6. ✅ CHANGELOG.md updated

### PR Title Format

Use the same prefix convention as commits:

```
[FEATURE] Add rate limiting support
[FIX] Resolve JWT validation issue
[DOCS] Update security module README
```

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Changes Made
- List of specific changes
- With bullet points

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Screenshots (if applicable)

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review performed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Tests added and pass locally
- [ ] CHANGELOG.md updated

## Related Issues
Fixes #123
Closes #456
```

### Review Process

1. **Automated Checks**: CI/CD must pass
2. **Code Review**: At least one approval required
3. **Testing**: All tests must pass
4. **Coverage**: Coverage must not decrease

---

## Release Process

### For Maintainers Only

1. **Update Versions**
   ```bash
   # Update all pom.xml files
   mvn versions:set -DnewVersion=1.0.5
   mvn versions:commit
   ```

2. **Update CHANGELOG.md**
   - Move "Unreleased" changes to new version section
   - Add release date
   - Create new "Unreleased" section

3. **Commit Version Bump**
   ```bash
   git add .
   git commit -m "Prepare next release"
   git push origin main
   ```

4. **Create and Push Tag**
   ```bash
   git tag -a v1.0.5 -m "Release 1.0.5"
   git push origin v1.0.5
   ```

5. **Create GitHub Release**
   - Go to GitHub Releases
   - Click "Draft a new release"
   - Select the tag
   - Copy CHANGELOG entry as release notes
   - Publish release

6. **Deploy to Maven**
   ```bash
   mvn clean deploy -P release
   ```

7. **Bump to Next SNAPSHOT**
   ```bash
   mvn versions:set -DnewVersion=1.0.6-SNAPSHOT
   mvn versions:commit
   git add .
   git commit -m "Prepare next release"
   git push origin main
   ```

---

## Questions?

If you have questions:

1. Check existing issues: https://github.com/henrigvs/aidji_boot/issues
2. Create a new issue with the `question` label
3. Join discussions: https://github.com/henrigvs/aidji_boot/discussions

---

## License

By contributing to Aidji Boot, you agree that your contributions will be licensed under the Apache License 2.0.
