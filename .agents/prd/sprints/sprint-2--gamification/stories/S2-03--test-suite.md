# S2-03 — Test Suite & Mutation Testing

> **Status**: `DONE`
> **Priority**: `HIGH`
> **Sprint**: Sprint 2

## User Story

> As a developer, I want comprehensive tests with mutation coverage so that I can refactor confidently and catch regressions.

## Acceptance Criteria

- [x] Unit tests for all 7 services with mocked dependencies
- [x] Unit tests for DTO factory methods (LifeStatResponse, ProfileCardResponse, ProfileResponse, StatUpdateResponse)
- [x] Unit tests for model logic (RankTier.fromScore, StatCategory.isValidValue, User.checkAndUpdateProfileCompletion)
- [x] Controller tests (WebMvcTest) for all 4 controllers with JWT auth mocking
- [x] Integration test with Testcontainers + WireMock (ProfileIntegrationTest)
- [x] Repository test with Testcontainers (UserRepositoryTest)
- [x] Security config test (SecurityConfigTest)
- [x] PITest mutation testing configured and runnable
- [x] Test support classes: MockJwtDecoderConfig, MockJwtFactory, PostgresTestContainer, TestDataFactory

## Delivered Components

| Type | Count | Tests |
|---|---|---|
| Unit (service) | 7 | LifeStatServiceTest, ProfileServiceTest, RankServiceTest, ScoreCalculationServiceTest, StorageServiceTest, SupabaseAuthServiceTest, UserServiceTest |
| Unit (DTO) | 4 | LifeStatResponseTest, ProfileCardResponseTest, ProfileResponseTest, StatUpdateResponseTest |
| Unit (model) | 3 | RankTierTest, StatCategoryTest, UserTest |
| Controller | 4 | AuthControllerTest, LifeStatControllerTest, ProfileControllerTest, UserControllerTest |
| Integration | 1 | ProfileIntegrationTest |
| Repository | 1 | UserRepositoryTest |
| Security | 1 | SecurityConfigTest |
| Support | 4 | MockJwtDecoderConfig, MockJwtFactory, PostgresTestContainer, TestDataFactory |

**Total**: 27 test files

## Skills Updated

- `testing/SKILL.md` — Created with full test structure, PITest config, support class docs
