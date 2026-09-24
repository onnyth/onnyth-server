# Observability

## Health checks

Spring Boot Actuator exposes `health`, `info`, and `metrics`
(`management.endpoints.web.exposure.include=health,info,metrics`), with full health detail
(`management.endpoint.health.show-details=always`) and database connectivity reflected in the health
check (`management.health.db.enabled=true`). `/actuator/health/**` is public (see
`docs/engineering/security.md`).

`/api/v1/health` (`system/adapter/in/rest/HealthController.java`) is a separate, simpler health
endpoint, also public — used interchangeably with `/actuator/health` depending on the caller
(Docker's `HEALTHCHECK` and Railway's `healthcheckPath` both target `/actuator/health` specifically;
see `docs/architecture/infrastructure.md`).

## Logging

- `logging.level.root=INFO`, `logging.level.com.onnyth=INFO` — the only logging configuration.
- Lombok `@Slf4j` is used ad hoc in use-case classes that log (e.g.
  `ProfileUseCaseService`, `BookmarkUseCaseService`'s idempotency/Kafka failure paths).
- **No structured (JSON) logging, no correlation/request-id propagation, and no log aggregation
  integration** (no Logstash/ELK/Datadog appender configured).

## Metrics

Actuator's `/actuator/metrics` exposes Micrometer's default metrics (JVM, HTTP request timings, DB
connection pool, etc.) but **there is no metrics backend wired up** — no Prometheus registry, no
Datadog/New Relic agent, nothing scrapes or ships these metrics anywhere today. They are only
inspectable by directly hitting the endpoint.

## Tracing

None. There is no distributed tracing (no Micrometer Tracing/Zipkin/OpenTelemetry integration).

## What this means in practice

Debugging a production issue today relies on: Railway's platform logs (stdout/stderr from the
container), `/actuator/health` for basic liveness, and manual reproduction — there is no way to trace
a single request across the Supabase HTTP calls, the DB queries, and (for `bookmark`) the Kafka publish
without reading application logs directly. If observability becomes a priority, adding structured
logging + a metrics backend + tracing would be a new, explicit piece of infrastructure work, not
something partially wired up already.
