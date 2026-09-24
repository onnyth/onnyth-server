# Development Rules

The step-by-step contract for implementing a change in this repository.

## 1. Identify the feature boundary

Find the owning feature module (`src/main/java/com/onnyth/onnythserver/{feature}/`) for the change.
If it doesn't fit an existing feature, decide whether it's a new feature module (most likely) or truly
cross-cutting (rare — belongs in `shared`, `configuration`, or `security` only if it's genuinely
framework/infrastructure plumbing, not business logic).

## 2. Read before writing

- That feature's `docs/features/<feature>.md` — purpose, business rules, current API, known
  limitations.
- The nearest existing, similar code in the same feature (or a structurally similar feature, e.g.
  `bookmark/` for idempotency/events) — match its shape rather than inventing a new one.
- `docs/architecture/dependency-rules.md` and `docs/engineering/coding-standards.md`.

## 3. Follow the established shape

- Preserve the hexagonal per-feature layout (`docs/architecture/low-level-design.md`). Do not
  reintroduce a top-level `controller/`, `service/`, `repository/`, or `models/` package.
- Dependency direction: `adapter → application → domain`. Never call another feature's
  `*JpaRepository`/`*Entity` directly — go through its `application/port`.
- New port/adapter only if genuinely needed — don't add a one-implementation interface "for future
  flexibility."
- DTOs are records in `adapter/in/rest/dto/`, with Jakarta Validation + a `fromDomain(...)`/
  `fromUser(...)` factory.
- Errors are `ApiException` subclasses in `application/exception/` — no new top-level exception
  package.
- Write-path use-case methods are `@Transactional`; defer any event publish to after-commit if the
  method also writes to the DB (see `bookmark/application/usecase/BookmarkUseCaseService.java`).

## 4. Schema changes

Add a Flyway migration (`V{next}__description.sql`) **and** a mirrored `supabase/migrations/*.sql`
entry for any new/altered table — see `docs/data/overview.md` for why both currently matter, and
`docs/development/known-issues.md` for the concrete list of tables where this was skipped in the past.
Do not rely on the test profile's `ddl-auto=update` to mask a missing migration.

## 5. Tests

Add tests under the matching subtree in `src/test/java/com/onnyth/onnythserver/{feature}/`, mirroring
`src/main/java`'s layout — not the legacy empty `controller/`/`dto/`/`models/`/`repository/`
directories. See `docs/engineering/testing.md` for which test type (unit/controller/repository/
integration) fits the change.

## 6. Verify

```bash
./mvnw compile                       # build
./mvnw test -Dtest="com.onnyth.onnythserver.{feature}.**"   # targeted tests
./mvnw test                          # full suite before considering the change done
```

## 7. Update documentation

Follow `docs/ai/documentation-rules.md` — at minimum, update the affected feature's
`docs/features/<feature>.md` if its status, API, business rules, or data model changed as a result of
your work.

## 8. Do not silently fix unrelated, pre-existing issues

If you notice a documented gap in `docs/development/known-issues.md` while working nearby, leave it
alone unless it's the actual scope of your task — flag it if it's newly discovered, don't casually
"clean it up" as a side effect (this rule mirrors the general code-change instruction: fix bugs
directly caused by or tightly coupled to your change, not unrelated pre-existing issues).

## What NOT to do

- Don't invent business rules not evidenced by the code, a story, or an explicit user instruction.
- Don't assume a feature has Kafka/Redis available just because `bookmark` does — check that feature's
  own `docs/features/*.md` first (see `docs/architecture/dependency-rules.md`).
- Don't add a new architectural pattern (a new cross-cutting cache, a new event bus usage, a new auth
  role) without recording it as an ADR (`docs/decisions/`) — see `docs/ai/architecture-rules.md`.
