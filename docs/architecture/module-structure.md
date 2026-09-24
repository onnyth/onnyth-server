# Module Structure

## Top-level package tree

```text
com.onnyth.onnythserver
├── OnnythServerApplication.java   # Spring Boot entry point
├── achievement    # badges, unlock progress, displayed badge slots
├── activity       # activity type catalog + logging with cooldowns
├── auth           # Supabase-backed signup/login/refresh/logout
├── bookmark       # bookmark CRUD — reference pattern for Redis idempotency + Kafka events
├── configuration  # application-wide bean config (e.g. RestTemplate)
├── feed           # friend activity feed timeline
├── friendship     # friend requests, friendships, profile comparison, (unwired) Follow
├── leaderboard    # overall + category leaderboards, friends-scoped
├── leveling       # level progression from XP, level-up events, titles
├── lifestats      # structured per-domain stat entities (occupation, wealth, physique, wisdom, charisma, social accounts, xfactors)
├── models         # placeholder social models — Post, Comment, Like (intentionally unwired)
├── profile        # profile CRUD, picture upload, profile card assembly, profile votes
├── quest          # quests catalog + completions
├── ranking        # rank tiers (RankTier) and rank progress
├── registration   # multi-step onboarding draft → commit flow, writes into lifestats
├── scoring        # weighted total-score calculation, event-driven recalculation
├── search         # user / general search
├── security       # SecurityConfig — the one global Spring Security filter chain
├── shared         # shared kernel: ApiException, StatDomain, StatChangedEvent, idempotency contracts
├── store          # cosmetics catalog, purchases, equips
├── streak         # daily streak tracking with milestones
├── system         # framework/system endpoints (health)
├── user           # core User aggregate, admin CRUD, account deletion
└── xp             # XP awarding workflow
```

19 feature modules follow the hexagonal shape described in
`docs/architecture/low-level-design.md`; `shared`, `configuration`, `security`, `system`, and `models`
are cross-cutting/placeholder and do not.

## Shared kernel (`shared`)

| Class | Purpose |
|---|---|
| `shared.exception.ApiException` | Abstract base for every feature-specific exception; declares `getHttpStatus()` |
| `shared.exception.ApiErrorResponse` | The standard error response record |
| `shared.exception.GlobalExceptionHandler` | `@RestControllerAdvice` handling every `ApiException` + common Spring exceptions |
| `shared.exception.LogoutFailedException` | The one exception that lives directly in `shared` rather than a feature package |
| `shared.domain.model.StatDomain` | Enum: the 5 real-world stat domains (`OCCUPATION`, `WEALTH`, `PHYSIQUE`, `WISDOM`, `CHARISMA`) + weight, used by `scoring` |
| `shared.domain.event.StatChangedEvent` | Spring `ApplicationEvent` contract that triggers score recalculation |
| `shared.idempotency.application.{IdempotencyService,IdempotencyResponse,IdempotencySerializer}` | Reusable idempotency port + contracts |
| `shared.idempotency.adapter.out.RedisIdempotencyService` | The (currently sole) Redis-backed implementation |
| `shared.utils.RequestHasher` | Canonical-string hashing helper used for idempotency keys |
| `shared.validation.{ValidUri,UriValidator}` | Custom Jakarta Validation constraint for URI fields |

## Placeholder / not-yet-wired code

- **`models/{Post,Comment,Like}`** — JPA-annotated entities exist with no accompanying
  `service/controller/repository`. Kept intentionally as a starting point for a possible future social
  feature (see `docs/development/future-work.md`). Do not
  wire these up without a scoped sprint/story — they are placeholders, not in-progress work.
- **`friendship.domain.model.Follow`** (+ `FollowId`, persistence adapter) — has a complete persistence
  adapter behind a port, but **no `application/usecase` or REST endpoint** uses it. Not user-facing
  despite living in a fully-hexagonal package. See `docs/features/friendships.md`.

## How to find a feature's documentation

Every feature module has a corresponding `docs/features/<name>.md`. See `docs/features/README.md` for
the index and current implementation status of each.
