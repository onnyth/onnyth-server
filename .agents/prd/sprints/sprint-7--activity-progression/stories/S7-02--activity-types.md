# S7-02 — Activity Types: Service + Seed Data + GET Endpoint

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Blocks**: S-031, S-032

## Goal

Create the activity type catalog service and endpoint. Seed 25+ activity types across all 5 stat categories.

## Tasks

### Service
- [ ] Create `ActivityTypeService` — getAll(), getByCategory(StatCategory), getById(UUID)

### Controller
- [ ] Create `ActivityController` (or add to existing)
- [ ] `GET /api/v1/activities/types` — list all active types, optional `?category=` query param
- [ ] OpenAPI annotations on all endpoints

### Seed Data (V20 migration)
- [ ] Seed 25+ activity types, at least 5 per stat category. Examples:
  - **FITNESS**: Walk 5000 steps (+20 XP), Run 3km (+25 XP), Workout 30 min (+30 XP), Yoga session (+15 XP), Drink 8 glasses of water (+10 XP)
  - **EDUCATION**: Read 10 pages (+15 XP), Learn something new (+20 XP), Complete a lesson (+25 XP), Practice a skill (+15 XP), Watch an educational video (+10 XP)
  - **SOCIAL_INFLUENCE**: Meet a friend (+10 XP), Help someone (+15 XP), Attend a social event (+20 XP), Volunteer (+25 XP), Network with someone new (+15 XP)
  - **CAREER**: Complete a work task (+20 XP), Upskill professionally (+25 XP), Mentor someone (+15 XP), Plan tomorrow's tasks (+10 XP), Review work goals (+15 XP)
  - **WEALTH**: Track expenses (+10 XP), Save money (+15 XP), Review budget (+15 XP), Learn about investing (+20 XP), Explore a side project (+25 XP)

## Related Skills
- `conventions` — controller pattern, service pattern
- `api-reference` — endpoint documentation
