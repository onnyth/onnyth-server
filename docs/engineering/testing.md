# Testing

## Test structure

Tests mirror the per-feature hexagonal `src/main/java` layout:

```text
src/test/java/com/onnyth/onnythserver/
├── OnnythServerApplicationTests.java     # Spring Boot context load test
├── {feature}/                            # one subtree per feature, mirroring src/main
│   ├── adapter/in/rest/                  # @WebMvcTest controller tests (+ dto/ validation tests)
│   ├── adapter/out/persistence/          # @DataJpaTest repository/entity tests (where present)
│   ├── application/usecase/              # unit tests for use cases (Mockito, no Spring context)
│   └── domain/model/                     # unit tests for domain model logic (where present)
├── integration/                          # cross-cutting, full-context integration tests
├── security/SecurityConfigTest.java      # security rule tests
├── shared/                                # tests for the shared kernel (idempotency, validation, StatDomain)
└── support/                               # shared test utilities (see below)
```

Feature subtrees currently exist for (at least): `achievement`, `auth`, `bookmark`, `friendship`,
`leaderboard`, `profile`, `quest`, `ranking`, `registration`, `scoring`, `search`, `user`.

> [!NOTE]
> `src/test/java/com/onnyth/onnythserver/{controller,dto,models,repository}/` are **empty leftover
> directories** from before the hexagonal migration (`docs/decisions/ADR-0001-hexagonal-architecture.md`).
> Do not add new tests there — add them under the matching feature subtree instead.

## Test types

| Type | Location | Tooling | Notes |
|---|---|---|---|
| Unit | `{feature}/application/usecase/`, `{feature}/domain/model/` | Mockito, no Spring context | Mock every port; fastest, most numerous |
| Controller | `{feature}/adapter/in/rest/` | `@WebMvcTest`, `@MockBean` the use case, Spring Security Test's `jwt()` post-processor | Verifies HTTP status, response body, validation errors |
| Repository/entity | `{feature}/adapter/out/persistence/` | `@DataJpaTest` + `PostgresTestContainer`, or plain Jakarta Validator tests for entity constraints | Real PostgreSQL via Testcontainers |
| Integration | `integration/` | Full Spring context, Testcontainers Postgres, WireMock for Supabase | End-to-end flows across layers |

## Key support classes (`support/`)

| Class | Purpose |
|---|---|
| `PostgresTestContainer` | Singleton Testcontainer, shared across all integration/repository tests, sets datasource properties dynamically |
| `MockJwtFactory` | Builds test JWTs with configurable `sub` and other claims |
| `MockJwtDecoderConfig` | Test `@Configuration` providing a mock JWT decoder |
| `TestDataFactory` | Builder methods for consistent test `User` and other entities |

## Running tests

```bash
./mvnw test                                                          # all tests
./mvnw test -Dtest="com.onnyth.onnythserver.bookmark.**"              # one feature, all layers
./mvnw test -Dtest="com.onnyth.onnythserver.bookmark.adapter.in.rest.**"  # controller layer only
./mvnw pitest:mutationCoverage                                        # mutation testing (see caution below)
```

## Test configuration

`src/test/resources/application-test.properties` sets `spring.jpa.hibernate.ddl-auto=update`
(deliberately, so Hibernate creates/updates the schema for each Testcontainers run rather than
requiring every migration to be applied). This is also why a missing Flyway migration (e.g.
`bookmark`, or the structured-stats tables — see `docs/development/known-issues.md`) is never caught
by the test suite: tests don't rely on Flyway/Supabase migrations for schema at all.

## PITest mutation testing — currently misconfigured

`pom.xml`'s `pitest-maven` plugin (`v1.17.4`) is configured with pre-hexagonal-migration package
patterns that mostly no longer exist:

```xml
<targetClasses>com.onnyth.onnythserver.service.*, controller.*, models.*, exceptions.handler.*</targetClasses>
<targetTests>com.onnyth.onnythserver.unit.*, controller.*</targetTests>
<mutationThreshold>0</mutationThreshold>
```

`service.*`, `controller.*`, `exceptions.handler.*`, and `unit.*` are all **empty** post-migration.
`models.*` still resolves, but only to the three unwired `Post`/`Comment`/`Like` placeholders, which
have no tests. In practice, `./mvnw pitest:mutationCoverage` runs successfully but analyzes
**effectively none of the real feature code** — `mutationThreshold=0` means this passes silently in
CI (though PITest is not currently wired into `.github/workflows/ci-cd.yml` at all — only
`./mvnw clean verify` runs). Fixing this requires updating `targetClasses`/`targetTests` to per-feature
patterns, e.g. `com.onnyth.onnythserver.*.application.usecase.*`,
`com.onnyth.onnythserver.*.adapter.in.rest.*`. See `docs/development/known-issues.md`.

## Writing new tests

1. **Use-case test**: `{feature}/application/usecase/`, mock all ports with Mockito, assert behavior.
2. **Controller test**: `{feature}/adapter/in/rest/`, `@WebMvcTest`, mock the use case bean, assert HTTP.
3. **DTO/validation test**: `{feature}/adapter/in/rest/dto/`, test factory methods and `@Valid`
   constraints.
4. **Repository/entity test**: `{feature}/adapter/out/persistence/`, `@DataJpaTest` +
   `PostgresTestContainer`, or a plain Jakarta Validator test for entity-level constraints (see
   `BookmarkEntityValidationTest`).
5. **Integration test**: `integration/` for cross-cutting, full-context flows; use WireMock to stub
   Supabase.
