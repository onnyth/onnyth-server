package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.*;
import com.onnyth.onnythserver.repository.*;
import com.onnyth.onnythserver.service.ScoreCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScoreCalculationService (domain-based architecture).
 */
@ExtendWith(MockitoExtension.class)
class ScoreCalculationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserOccupationRepository occupationRepository;
    @Mock
    private UserWealthRepository wealthRepository;
    @Mock
    private UserPhysiqueRepository physiqueRepository;
    @Mock
    private SportMedalRepository sportMedalRepository;
    @Mock
    private UserWisdomRepository wisdomRepository;
    @Mock
    private UserEducationRepository educationRepository;
    @Mock
    private UserXfactorRepository xfactorRepository;
    @Mock
    private UserCharismaRepository charismaRepository;
    @Mock
    private UserSocialAccountRepository socialAccountRepository;
    @Mock
    private ProfileLikeRepository profileLikeRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private ScoreHistoryRepository scoreHistoryRepository;

    @InjectMocks
    private ScoreCalculationService scoreCalculationService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    // ─── recalculateOccupation() ────────────────────────────────────────────

    @Nested
    @DisplayName("recalculateOccupation()")
    class RecalculateOccupation {

        @Test
        @DisplayName("returns 0 when no occupation exists")
        void returnsZero_whenNoOccupation() {
            when(occupationRepository.findByUserIdAndIsCurrentTrue(userId))
                    .thenReturn(Optional.empty());

            int score = scoreCalculationService.recalculateOccupation(userId);

            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("calculates score with CEO title, 10 years, 3 skills")
        void calculatesScore_withCeoTitle() {
            UserOccupation occ = UserOccupation.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .jobTitle("CEO")
                    .yearsExperience(10)
                    .skills(List.of("Leadership", "Strategy", "Finance"))
                    .isCurrent(true)
                    .score(0)
                    .build();

            when(occupationRepository.findByUserIdAndIsCurrentTrue(userId))
                    .thenReturn(Optional.of(occ));
            when(scoreHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            int score = scoreCalculationService.recalculateOccupation(userId);

            // CEO=40, company=15, exp=min(10*2,20)=20, skills=min(3*3,15)=9 → 84
            assertThat(score).isEqualTo(84);
        }

        @Test
        @DisplayName("caps experience at 20 points")
        void capsExperience() {
            UserOccupation occ = UserOccupation.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .jobTitle("Intern")
                    .yearsExperience(25)
                    .skills(List.of())
                    .isCurrent(true)
                    .score(0)
                    .build();

            when(occupationRepository.findByUserIdAndIsCurrentTrue(userId))
                    .thenReturn(Optional.of(occ));
            when(scoreHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            int score = scoreCalculationService.recalculateOccupation(userId);

            // intern=5, company=15, exp=20(capped), skills=0 → 40
            assertThat(score).isEqualTo(40);
        }
    }

    // ─── recalculatePhysique() ──────────────────────────────────────────────

    @Nested
    @DisplayName("recalculatePhysique()")
    class RecalculatePhysique {

        @Test
        @DisplayName("returns 0 when no physique data")
        void returnsZero_whenNoPhysique() {
            when(physiqueRepository.findByUserId(userId)).thenReturn(Optional.empty());

            int score = scoreCalculationService.recalculatePhysique(userId);

            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("calculates score for athlete with workouts and medals")
        void calculatesScore_forAthlete() {
            UserPhysique physique = UserPhysique.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .fitnessLevel(FitnessLevel.ATHLETE)
                    .weeklyWorkouts(5)
                    .score(0)
                    .build();

            when(physiqueRepository.findByUserId(userId)).thenReturn(Optional.of(physique));
            when(sportMedalRepository.findAllByUserId(userId)).thenReturn(List.of());
            when(scoreHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            int score = scoreCalculationService.recalculatePhysique(userId);

            // ATHLETE=30, bodyComp=10(no fat%), workouts=min(5*5,25)=25, medals=0 → 65
            assertThat(score).isEqualTo(65);
        }
    }

    // ─── recalculateAll() ───────────────────────────────────────────────────

    @Nested
    @DisplayName("recalculateAll()")
    class RecalculateAll {

        @Test
        @DisplayName("recalculates all domains and persists weighted total on user")
        void recalculatesAndPersistsTotal() {
            User user = User.builder().id(userId).email("test@test.com").totalScore(0L).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // All domain repos return empty → each domain = 0
            when(occupationRepository.findByUserIdAndIsCurrentTrue(userId)).thenReturn(Optional.empty());
            when(wealthRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(physiqueRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(physiqueRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(wisdomRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(educationRepository.findByUserIdAndIsHighestTrue(userId)).thenReturn(Optional.empty());
            when(xfactorRepository.findAllByUserId(userId)).thenReturn(List.of());
            when(charismaRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(socialAccountRepository.getTotalFollowerCount(userId)).thenReturn(0);
            when(followRepository.countByFollowingId(userId)).thenReturn(0L);
            when(profileLikeRepository.countByLikedId(userId)).thenReturn(0L);
            when(socialAccountRepository.findAllByUserId(userId)).thenReturn(List.of());

            long totalScore = scoreCalculationService.recalculateAll(userId);

            assertThat(totalScore).isEqualTo(0L);
            assertThat(user.getTotalScore()).isEqualTo(0L);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwsUserNotFound() {
            // All domain repos return empty so individual recalculations proceed
            when(occupationRepository.findByUserIdAndIsCurrentTrue(userId)).thenReturn(Optional.empty());
            when(occupationRepository.findByUserIdAndIsCurrentTrue(userId)).thenReturn(Optional.empty());

            // recalculateWealth calls userRepository.findById internally
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scoreCalculationService.recalculateAll(userId))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
