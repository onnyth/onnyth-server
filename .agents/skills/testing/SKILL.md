---
name: testing
description: Test strategy — unit, controller, integration, and repository tests with Testcontainers, WireMock, PITest, and support utilities
---

# Testing

Comprehensive test suite organized into unit, controller (WebMvcTest), integration, and repository tests. Uses Testcontainers for real PostgreSQL, WireMock for Supabase API mocking, and PITest for mutation testing.

## Test Structure

> [!NOTE]
> Tests now mirror the per-feature hexagonal `src/main/java` layout (see
> `docs/decisions/ADR-0001-hexagonal-architecture.md`), not the flat `controller/service/repository`
> tree shown in earlier versions of this skill. `src/test/java/com/onnyth/onnythserver/{controller,dto,models,repository}/`
> are empty leftover directories from before the migration — do not add new tests there.

```
src/test/java/com/onnyth/onnythserver/
├── OnnythServerApplicationTests.java              # Spring Boot context load test
├── {feature}/                                     # one subtree per feature module, mirroring src/main
│   ├── adapter/in/rest/                           # @WebMvcTest controller tests (+ dto/ validation tests)
│   ├── adapter/out/persistence/                   # @DataJpaTest repository/entity tests (where present)
│   ├── application/usecase/                       # unit tests for use-case services (Mockito, no Spring context)
│   └── domain/model/                              # unit tests for domain model logic (where present)
├── integration/                                    # cross-cutting full-context integration tests (e.g. ProfileIntegrationTest)
├── security/
│   └── SecurityConfigTest.java                    # Security rule tests
├── shared/                                         # tests for shared/ kernel (idempotency, validation, StatDomain, etc.)
└── support/                                        # Shared test utilities
    ├── MockJwtDecoderConfig.java                  # Test @Configuration for mock JWT decoder
    ├── MockJwtFactory.java                        # Creates test JWTs with configurable claims
    ├── PostgresTestContainer.java                 # Singleton Testcontainer for PostgreSQL
    └── TestDataFactory.java                       # Builds test entities (User, LifeStat, etc.)
```

Example feature subtrees today: `achievement`, `auth`, `bookmark` (adapter/in/rest + adapter/in/rest/dto
+ adapter/out/persistence + application/usecase), `friendship`, `leaderboard`, `profile`, `quest`,
`ranking`, `registration`, `scoring`, `search`, `user`.

## Test Types

### Unit Tests (`unit/`)
- Pure unit tests with mocked dependencies using Mockito
- Cover: services, DTOs, models
- No Spring context loaded

### Controller Tests (`controller/`)
- Use `@WebMvcTest` — loads only the web layer
- Mock service beans with `@MockBean`
- Use Spring Security Test's `jwt()` post-processor for auth
- Test HTTP status codes, response bodies, validation errors

### Integration Tests (`integration/`)
- Full Spring Boot context with real PostgreSQL via Testcontainers
- WireMock for Supabase API stubbing
- Test end-to-end flows

### Repository Tests (`repository/`)
- Use `@DataJpaTest` with Testcontainers PostgreSQL
- Test custom query methods and constraints

## Key Support Classes

### PostgresTestContainer
- Singleton Testcontainer pattern for PostgreSQL
- Shared across all integration/repository tests
- Sets datasource properties dynamically

### MockJwtFactory
- Creates JWTs for testing authenticated endpoints
- Configurable `sub` (user ID) and other claims

### TestDataFactory
- Builder methods for creating test `User`, `LifeStat`, and other entities
- Provides consistent test data across all tests

## PITest Mutation Testing

Configured in `pom.xml`:

```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.17.4</version>
</plugin>
```

> [!CAUTION]
> **This configuration is stale and currently matches almost nothing.** `targetClasses`
> (`com.onnyth.onnythserver.service.*`, `controller.*`, `exceptions.handler.*`) and `targetTests`
> (`com.onnyth.onnythserver.unit.*`, `controller.*`) reference packages that no longer exist after the
> hexagonal migration (ADR-0001) — verified empty via `find`. `models.*` still matches, but only the
> 3 unwired `Post`/`Comment`/`Like` placeholders, which have no tests. In practice, running
> `./mvnw pitest:mutationCoverage` today analyzes effectively none of the current feature code. See
> `docs/known-issues.md`. Fixing this requires updating `pom.xml`'s `targetClasses`/`targetTests` to
> per-feature `application.usecase.*` / `adapter.in.rest.*` patterns (or similar) — out of scope for a
> docs-only pass.

**Target classes (as configured, stale)**: `service.*`, `controller.*`, `models.*`, `exceptions.handler.*`
**Target tests (as configured, stale)**: `unit.*`, `controller.*`
**Excluded**: DTOs, security config, application class, configuration package

Run with: `./mvnw pitest:mutationCoverage`

Reports generated at: `target/pit-reports/` (HTML + XML)

## Test Configuration

- `src/test/resources/application-test.properties` — test-specific properties
- Tests excluded from PITest: integration tests (need Docker), repository tests (need DB)

## Running Tests

```bash
# All tests
./mvnw test

# Tests for a single feature (all layers)
./mvnw test -Dtest="com.onnyth.onnythserver.bookmark.**"

# Controller tests for a single feature
./mvnw test -Dtest="com.onnyth.onnythserver.bookmark.adapter.in.rest.**"

# Mutation testing (see PITest caution above — currently matches ~nothing)
./mvnw pitest:mutationCoverage
```

## Writing New Tests

1. **Use-case test**: Create in `{feature}/application/usecase/`, mock all dependencies (ports), verify behavior
2. **Controller test**: Create in `{feature}/adapter/in/rest/`, use `@WebMvcTest`, mock the use case bean, test HTTP
3. **DTO/validation test**: Create in `{feature}/adapter/in/rest/dto/`, test factory methods, field mapping, and `@Valid` constraints
4. **Repository/entity test**: Create in `{feature}/adapter/out/persistence/`, use `@DataJpaTest` + `PostgresTestContainer` (or plain Jakarta Validator tests for entity constraints, see `BookmarkEntityValidationTest`)
5. **Integration test**: Create in `integration/` for cross-cutting, full-context flows; use WireMock for Supabase
