# Deployment

## Pipeline

```text
push/PR → main
   │
   ▼
GitHub Actions: build-and-test job
   │  ./mvnw clean verify  (Java 21 Temurin, Maven dependency cache)
   ▼
(only on push to main, only if build-and-test passed)
GitHub Actions: deploy job
   │  npm i -g @railway/cli
   │  railway up --service onnyth-server --detach   (RAILWAY_TOKEN from GH secrets)
   ▼
Railway (production)
```

Defined in `.github/workflows/ci-cd.yml`. Pull requests only run `build-and-test` — they never deploy.

## Build artifact

Multi-stage `Dockerfile`:
1. **Builder**: `maven:3.9-eclipse-temurin-21`, `mvn clean package -DskipTests -q` (tests are not
   re-run at image-build time — they already ran in the CI `build-and-test` job).
2. **Runtime**: `eclipse-temurin:21-jre-alpine`, runs as a non-root `spring` user, exposes port 8080,
   Docker `HEALTHCHECK` against `/actuator/health` every 30s.

## Runtime configuration (Railway)

`railway.toml`:
- Builds via the repo `Dockerfile`.
- `healthcheckPath = "/actuator/health"`, `healthcheckTimeout = 60`.
- `restartPolicyType = "ON_FAILURE"`, `restartPolicyMaxRetries = 5`.
- `numReplicas = 1` — single instance, no horizontal scaling today.

## Environment variables required in production

| Variable | Purpose |
|---|---|
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | PostgreSQL connection (Supabase-hosted) |
| `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY` | Supabase Auth + Storage access |
| `SUPABASE_STORAGE_BUCKET` | Profile picture bucket name (default `profile-pics`) |
| `RAILWAY_TOKEN` | CI/CD deploy credential (GitHub Actions secret only, not app runtime) |

See `.env.example` for the full documented list (no real values).

## Schema migrations in production

`spring.jpa.hibernate.ddl-auto=validate` — the application **never** creates or alters schema itself.
Flyway migrations run automatically on startup against whatever `DATABASE_URL` points to, but (per
`docs/data/overview.md`) Flyway alone does not cover the full schema — production's actual schema
state depends on Supabase-side migrations having already been applied to that Supabase project
independently. There is no automated step in `.github/workflows/ci-cd.yml` that runs
`supabase db push` — keeping the two histories in sync for a new schema change is currently a manual
step. See `docs/development/known-issues.md`.

## Local development parity

Local dev intentionally does not perfectly mirror production:
- `supabase start` provisions a **local** Supabase stack (Postgres/Auth/Storage) — separate from the
  hosted Supabase project used in production.
- `docker-compose.yml`'s `kafka` service is local-only; Kafka in production would need its own
  provisioning (not currently configured — `bookmark` is the only consumer, and there is no evidence
  of a production Kafka broker being provisioned via Railway).
- Redis is not provisioned anywhere (`docker-compose.yml` or Railway) — see
  `docs/development/known-issues.md`.
