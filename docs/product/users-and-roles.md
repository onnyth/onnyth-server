# Users and Roles

## Who interacts with this system

**End user** — the only role that exists today. Every authenticated principal in this system is a
regular end user; there is no admin, moderator, or partner/event-organizer role modeled anywhere in
the JWT claims, `SecurityConfig`, or any feature's business logic. See
`docs/engineering/security.md`.

## Anonymous / unauthenticated visitors

A caller with no JWT at all can still reach a small, explicit set of public endpoints — most notably
viewing any user's public profile card (`GET /api/v1/users/{userId}/card`) and the auth endpoints
themselves. See `docs/api/overview.md` for the full public-path list.

## The "admin" surface that exists without an admin role

`/api/users/**` (list all users, fetch/update/delete any user by id or email) is reachable by *any*
authenticated user — there is no role check gating it. This is documented as a known gap in
`docs/features/users.md` and `docs/engineering/security.md`, not a designed admin role. If a real
admin/moderator role is introduced in the future, it should be recorded as an ADR (see
`docs/decisions/README.md`) since it would be a new authorization model, not a small tweak.

## Planned but not modeled

Nothing in `docs/development/future-work.md` or the current feature set describes any additional user
role either (no partner/business account, no moderator). If a distinct role is scoped in the future,
this document should be updated alongside the ADR that introduces it.
