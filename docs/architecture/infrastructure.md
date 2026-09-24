# Infrastructure

## Runtime dependencies

| Component | Role | Provisioning |
|---|---|---|
| **PostgreSQL** | System of record for all feature data | Hosted on Supabase; Flyway-migrated; `ddl-auto=validate` in every real environment |
| **Supabase Auth** | Identity provider (JWT issuance, signup/login/refresh/logout) | Supabase-hosted; consumed as an external HTTP API by `auth/` |
| **Supabase Storage** | Profile picture object storage | Supabase-hosted; consumed as an external HTTP API by `profile/` |
| **Redis** | `bookmark` idempotency-key cache only | `localhost:6379` expected; **not** started by `docker-compose.yml` — must be run separately for local dev (see Known gap below) |
| **Kafka** | `bookmark`-only domain event bus | Single-node KRaft broker (no Zookeeper) via `docker-compose.yml`, port `9092` |

## Local development

```bash
supabase start                       # Postgres + Auth + Storage (localhost, per Supabase CLI ports)
docker compose up                    # Kafka broker (+ optionally the app container)
# Redis must be started separately — not in docker-compose.yml, see Known gap below
./mvnw spring-boot:run                # run the app on the host
```

`docker-compose.yml` defines two services:
- **`kafka`** — `confluentinc/cp-kafka:7.6.1`, single-node KRaft mode, exposed on `localhost:9092`.
- **`app`** — builds from the repo `Dockerfile`, connects to Postgres/Supabase via
  `host.docker.internal` (reaching services started by `supabase start` on the host), and to Kafka via
  the compose network (`kafka:29092`). Config values are hardcoded to `host.docker.internal` rather
  than reusing `${DATABASE_URL:-...}` from `.env`, because `.env` points at `127.0.0.1` for the
  host-based (`./mvnw spring-boot:run`) workflow, which would resolve to the container itself if reused
  here.

**Known gap**: Redis is required for `bookmark`'s idempotency guarantee (see
`docs/decisions/ADR-0002-redis-idempotency.md`) but is not provisioned in `docker-compose.yml`. A
contributor running only `docker compose up` will have bookmark creation silently fall back to
"no idempotency" (best-effort fail-open) rather than an obvious error. See
`docs/development/known-issues.md`.

## Build & packaging

- **Build tool**: Maven (wrapped via `./mvnw`).
- **Docker image**: multi-stage build (`Dockerfile`) — `maven:3.9-eclipse-temurin-21` builder stage,
  `eclipse-temurin:21-jre-alpine` runtime stage, runs as a non-root `spring` user, exposes port 8080,
  Docker `HEALTHCHECK` against `/actuator/health`.

## CI/CD

`.github/workflows/ci-cd.yml`:
1. **`build-and-test`** job — on every push/PR to `main`: `./mvnw clean verify` on `ubuntu-latest`,
   Java 21 (Temurin), Maven dependency caching.
2. **`deploy`** job — only on push to `main` (not PRs), only after `build-and-test` passes: installs
   the Railway CLI, runs `railway up --service onnyth-server --detach` using `RAILWAY_TOKEN` from
   GitHub Actions secrets.

## Deployment target

**Railway** (`railway.toml`): builds from the repo `Dockerfile`; health check path
`/actuator/health`, 60s timeout; restart policy `ON_FAILURE`, max 5 retries; `numReplicas = 1`
(single instance — no horizontal scaling or leader election concerns today).

## Observability

Spring Boot Actuator is enabled with `health`, `info`, `metrics` endpoints exposed
(`management.endpoints.web.exposure.include=health,info,metrics`), `management.health.db.enabled=true`
(DB connectivity reflected in `/actuator/health`), and full health detail
(`management.endpoint.health.show-details=always`). There is no metrics backend (Prometheus/Datadog/etc.)
wired up, no distributed tracing, and no structured/JSON logging configured — logging is plain
`logging.level.*` at `INFO` for `root` and `com.onnyth`. See `docs/engineering/observability.md`.

## Secrets

`SUPABASE_*`, `DATABASE_*`, `RAILWAY_TOKEN` are supplied via environment variables / `.env` (local) /
GitHub Actions secrets (CI/CD) — never hardcoded in source or committed to the repo. `.env.example`
documents the expected variable names without real values.
