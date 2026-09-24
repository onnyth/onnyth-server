# Dependency Rules

These rules are enforceable and intentional. An AI agent (or human) proposing a change that violates
one of these should treat it as a deliberate architectural boundary to preserve, not an accident to
"helpfully" fix.

## 1. Dependency direction within a feature

```text
adapter → application → domain
```

- `domain` must never import `application`, `adapter`, Spring, or JPA classes.
- `application` (ports + use cases) may depend on `domain` and its own `port` interfaces. It must never
  depend on a concrete class from `adapter` (e.g. a use case must not import a `*JpaRepository` or
  `*Entity` directly — only the `port` interface).
- `adapter` may depend on `application` and `domain`. Persistence/REST/Kafka adapters are the only
  layer allowed to know about JPA, Spring MVC, or the Kafka client API.

## 2. Controllers must not contain business logic

REST/Kafka inbound adapters parse input, call exactly one use-case method, and map the result to a
response DTO. Validation beyond structural (`@Valid`/Jakarta annotations) and any decision-making
belongs in the use case, not the controller.

## 3. Never bypass a port

Never call another feature's `*JpaRepository` (or any other adapter-layer class) from a controller or
from a different feature's use case. Always go through the owning feature's `application/port`
interface. If two features need the same concept, consider whether it belongs in `shared` instead of
being duplicated or reached into directly.

## 4. New port/adapter only if genuinely needed

Don't introduce an interface with exactly one implementation "for future flexibility." Most features
do not need a strategy abstraction; a port exists because there's a real need to swap the
implementation (as with Redis idempotency or Kafka publishing) or to keep a feature's application layer
decoupled from a specific persistence technology for testability.

## 5. Transactions and event publishing

Mark use-case write methods `@Transactional`. When a use case both writes to the database and
publishes an event (Kafka or otherwise) in the same call, defer the publish until **after the
transaction commits** via `TransactionSynchronizationManager.registerSynchronization(...).afterCommit()`
— see `bookmark/application/usecase/BookmarkUseCaseService.java` for the reference pattern. This
avoids publishing an event for a write that is later rolled back.

## 6. Errors

Throw a feature-specific subclass of `shared.exception.ApiException` from `application/exception`.
`shared.exception.GlobalExceptionHandler` handles all `ApiException`s generically. Add a
feature-scoped `@RestControllerAdvice(assignableTypes = FooController.class)` only if a feature needs
custom mapping beyond what the generic handler provides (see
`bookmark/adapter/in/rest/BookmarkExceptionHandler.java`).

## 7. DTOs

DTOs are Java `record`s living next to the adapter that owns them (`adapter/in/rest/dto/`), with
Jakarta Validation annotations on request DTOs and static `fromDomain(...)`/`fromUser(...)` factories
on response DTOs.

## 8. Schema changes

Every entity that will run against production (`spring.jpa.hibernate.ddl-auto=validate`) needs a
matching Flyway migration `V{next}__description.sql` in `src/main/resources/db/migration/`. Check the
highest existing `V{N}` first (see `docs/data/database-schema.md`). Do not rely on the test profile's
`ddl-auto=update` to paper over a missing migration — `bookmark`'s missing migration
(`docs/development/known-issues.md` #1) is a real instance of this gap, not a pattern to repeat.

## 9. Routing

Use the `/api/v1/` prefix for new endpoints unless following an existing unprefixed contract
(`/api/users/**`).

## 10. Preserve the hexagonal shape

Do not reintroduce a top-level `controller/`, `service/`, `repository/`, or `models/` package for new
work. `models/` continues to exist only for the intentionally-unwired `Post`/`Comment`/`Like`
placeholders — it is not a home for new entities.

## Cross-cutting infra is feature-scoped, not general

Kafka and Redis are provisioned and used only by `bookmark` today. Do not assume any other feature has
an event bus or cache without checking that feature's `docs/features/*.md`.
