package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.dto.AchievementResponse;
import com.onnyth.onnythserver.dto.AchievementStatsResponse;
import com.onnyth.onnythserver.dto.DisplayedBadgeResponse;
import com.onnyth.onnythserver.exceptions.BadgeNotFoundException;
import com.onnyth.onnythserver.exceptions.BadgeNotUnlockedException;
import com.onnyth.onnythserver.models.*;
import com.onnyth.onnythserver.repository.AchievementRepository;
import com.onnyth.onnythserver.repository.FriendshipRepository;
import com.onnyth.onnythserver.repository.UserAchievementRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.service.AchievementProgressCalculator;
import com.onnyth.onnythserver.service.AchievementService;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AchievementService")
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;
    @Mock
    private UserAchievementRepository userAchievementRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private AchievementProgressCalculator progressCalculator;

    @InjectMocks
    private AchievementService achievementService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ACH_1 = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID ACH_2 = UUID.fromString("00000000-0000-0000-0000-000000000020");

    private Achievement achievement1, achievement2;

    @BeforeEach
    void setUp() {
        achievement1 = Achievement.builder().id(ACH_1).code("SCORE_100").name("Century Club")
                .description("Reach 100 score").icon("💯").category(AchievementCategory.MILESTONE)
                .requirementType("TOTAL_SCORE").threshold(100).points(10).isActive(true).build();
        achievement2 = Achievement.builder().id(ACH_2).code("FIRST_STAT").name("First Step")
                .description("Input first stat").icon("🚀").category(AchievementCategory.SPECIAL)
                .requirementType("ANY_STAT_INPUT").threshold(1).points(5).isActive(true).build();
    }

    @Nested
    @DisplayName("getAllAchievements")
    class GetAllAchievements {
        @Test
        @DisplayName("returns all achievements with unlock status and progress")
        void returnsAll() {
            when(achievementRepository.findAllByIsActiveTrue()).thenReturn(List.of(achievement1, achievement2));
            UserAchievement ua = UserAchievement.builder().userId(USER_ID).achievementId(ACH_1)
                    .unlockedAt(Instant.now()).build();
            when(userAchievementRepository.findAllByUserId(USER_ID)).thenReturn(List.of(ua));
            when(progressCalculator.calculateProgress(USER_ID, achievement2)).thenReturn(50);

            List<AchievementResponse> result = achievementService.getAllAchievements(USER_ID);

            assertThat(result).hasSize(2);
            AchievementResponse unlocked = result.stream().filter(r -> r.id().equals(ACH_1)).findFirst().orElseThrow();
            assertThat(unlocked.isUnlocked()).isTrue();
            assertThat(unlocked.progress()).isEqualTo(100);

            AchievementResponse locked = result.stream().filter(r -> r.id().equals(ACH_2)).findFirst().orElseThrow();
            assertThat(locked.isUnlocked()).isFalse();
            assertThat(locked.progress()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("getAchievementStats")
    class GetStats {
        @Test
        @DisplayName("returns correct totals and earned points")
        void returnsStats() {
            when(achievementRepository.findAllByIsActiveTrue()).thenReturn(List.of(achievement1, achievement2));
            UserAchievement ua = UserAchievement.builder().userId(USER_ID).achievementId(ACH_1).build();
            when(userAchievementRepository.findAllByUserId(USER_ID)).thenReturn(List.of(ua));

            AchievementStatsResponse stats = achievementService.getAchievementStats(USER_ID);

            assertThat(stats.totalAchievements()).isEqualTo(2);
            assertThat(stats.unlockedCount()).isEqualTo(1);
            assertThat(stats.totalPoints()).isEqualTo(15); // 10 + 5
            assertThat(stats.earnedPoints()).isEqualTo(10); // only ACH_1 unlocked
        }
    }

    @Nested
    @DisplayName("updateDisplayedBadges")
    class UpdateBadges {
        @Test
        @DisplayName("throws BadgeNotUnlockedException for locked achievement")
        void throwsForLocked() {
            User user = TestDataFactory.aUser().id(USER_ID).build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(achievementRepository.findById(ACH_1)).thenReturn(Optional.of(achievement1));
            when(userAchievementRepository.existsByUserIdAndAchievementId(USER_ID, ACH_1)).thenReturn(false);

            assertThatThrownBy(() -> achievementService.updateDisplayedBadges(USER_ID, List.of(ACH_1)))
                    .isInstanceOf(BadgeNotUnlockedException.class);
        }

        @Test
        @DisplayName("throws BadgeNotFoundException for invalid ID")
        void throwsForInvalid() {
            User user = TestDataFactory.aUser().id(USER_ID).build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(achievementRepository.findById(ACH_1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> achievementService.updateDisplayedBadges(USER_ID, List.of(ACH_1)))
                    .isInstanceOf(BadgeNotFoundException.class);
        }
    }
}
