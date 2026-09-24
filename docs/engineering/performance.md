# Performance

There is no dedicated performance-testing or load-testing infrastructure in this repository today.
This document records the performance-relevant decisions that do exist, rather than aspirational
targets.

## Deliberate latency guards

- **Kafka producer** (`bookmark` only): `spring.kafka.producer.properties.max.block.ms=2000` — caps
  how long a broker/metadata outage can block the calling HTTP thread, far below Kafka's 60s default.
  See `docs/decisions/ADR-0003-kafka-domain-events.md`.
- **Outbound Supabase HTTP calls** (`auth`, `profile`): `RestTemplateConfig` sets a 10s connect / 30s
  read timeout on the shared `RestTemplate` bean — an unresponsive Supabase API cannot hang a request
  indefinitely.

## Query patterns

- Pagination (`Pageable`/`Page<T>`) is used for every list endpoint that could grow unbounded
  (bookmarks, leaderboard, feed, friend lists, search results) — see each feature's
  `docs/features/*.md` "API" section for the exact `page`/`size` bounds.
- A handful of composite indexes exist specifically to support hot query patterns, e.g.
  `activity_log`'s `(user_id, activity_type_id, logged_at)` index backing cooldown checks (see
  `docs/features/activities.md`), and `leaderboard_snapshots`' composite indexes for
  `(friend_owner_id, snapshot_date[, category])` lookups.

## No caching layer

Redis exists only for `bookmark`'s idempotency cache — it is not used as a general read-through cache
for any hot read path (profile card, leaderboard, feed all hit PostgreSQL directly on every request).
If any of these become a bottleneck, introducing a cache would be a new architectural decision (new
ADR), not an existing pattern to extend.

## Connection/resource limits

- `spring.servlet.multipart.max-file-size=5MB`, `max-request-size=10MB` — bounds profile picture
  upload size.
- `numReplicas = 1` in `railway.toml` — the production deployment is a single instance; there is no
  horizontal scaling, load balancing across instances, or sticky-session concern today.

## Known gap

No load/performance test suite exists. `docs/engineering/testing.md` covers unit/controller/
integration/mutation testing only.
