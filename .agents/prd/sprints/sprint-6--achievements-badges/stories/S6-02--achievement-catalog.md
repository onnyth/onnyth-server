# S6-02 — Achievement Catalog: Service + Endpoints

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Depends on**: S6-01

## Goal

Implement achievement catalog endpoints — list all achievements (with unlock status), list unlocked only, filter by category.

## Tasks

### Service
- [ ] Create `AchievementService`
- [ ] `getAllAchievements(UUID userId)` → List<AchievementResponse> — all achievements with user's unlock status + progress (0–100)
- [ ] `getUnlockedAchievements(UUID userId)` → List<AchievementResponse> — only unlocked, sorted by unlockedAt DESC
- [ ] `getAchievementsByCategory(UUID userId, AchievementCategory)` → filtered list

### Controller
- [ ] Create `AchievementController` at `/api/v1/achievements`
- [ ] `GET /api/v1/achievements` — all achievements
- [ ] `GET /api/v1/achievements/category/{category}` — by category
- [ ] `GET /api/v1/achievements/unlocked` — unlocked only

### Tests
- [ ] `AchievementServiceTest` — unit tests for catalog methods
- [ ] `AchievementControllerTest` — WebMvc tests

## Related Skills
- `conventions` — controller/service patterns
