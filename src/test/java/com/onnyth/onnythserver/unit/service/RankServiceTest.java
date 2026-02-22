package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.dto.RankProgressResponse;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.RankTier;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.service.RankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RankService.
 */
@ExtendWith(MockitoExtension.class)
class RankServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RankService rankService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    // ─── calculateRankTier() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("calculateRankTier()")
    class CalculateRankTier {

        @Test
        @DisplayName("delegates to RankTier.fromScore")
        void delegatesToFromScore() {
            assertThat(rankService.calculateRankTier(0)).isEqualTo(RankTier.BRONZE);
            assertThat(rankService.calculateRankTier(100)).isEqualTo(RankTier.SILVER);
            assertThat(rankService.calculateRankTier(250)).isEqualTo(RankTier.GOLD);
            assertThat(rankService.calculateRankTier(500)).isEqualTo(RankTier.PLATINUM);
            assertThat(rankService.calculateRankTier(1000)).isEqualTo(RankTier.ELITE);
        }
    }

    // ─── updateUserRank() ────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateUserRank()")
    class UpdateUserRank {

        @Test
        @DisplayName("updates rank when tier changes")
        void updatesRankWhenChanged() {
            User user = User.builder().id(userId).email("test@test.com")
                    .totalScore(300L).rankTier(RankTier.BRONZE).build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            RankTier result = rankService.updateUserRank(userId);

            assertThat(result).isEqualTo(RankTier.GOLD);
            assertThat(user.getRankTier()).isEqualTo(RankTier.GOLD);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("does not save when tier unchanged")
        void doesNotSaveWhenUnchanged() {
            User user = User.builder().id(userId).email("test@test.com")
                    .totalScore(50L).rankTier(RankTier.BRONZE).build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            RankTier result = rankService.updateUserRank(userId);

            assertThat(result).isEqualTo(RankTier.BRONZE);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws UserNotFoundException when user missing")
        void throwsUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rankService.updateUserRank(userId))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    // ─── getRankProgress() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("getRankProgress()")
    class GetRankProgress {

        @Test
        @DisplayName("returns progress for mid-tier user")
        void returnsProgressForMidTier() {
            User user = User.builder().id(userId).email("test@test.com")
                    .totalScore(150L).rankTier(RankTier.SILVER).build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            RankProgressResponse result = rankService.getRankProgress(userId);

            assertThat(result.currentTier()).isEqualTo("Silver");
            assertThat(result.currentBadge()).isEqualTo("🥈");
            assertThat(result.currentScore()).isEqualTo(150);
            assertThat(result.nextTier()).isEqualTo("Gold");
            assertThat(result.nextBadge()).isEqualTo("🥇");
            assertThat(result.pointsToNextTier()).isEqualTo(100); // 250 - 150
            // progress: (150-100)/(250-100) * 100 = 33.3%
            assertThat(result.progressPercent()).isEqualTo(33.3);
        }

        @Test
        @DisplayName("returns 100% progress for ELITE user")
        void returnsFullProgressForElite() {
            User user = User.builder().id(userId).email("test@test.com")
                    .totalScore(5000L).rankTier(RankTier.ELITE).build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            RankProgressResponse result = rankService.getRankProgress(userId);

            assertThat(result.currentTier()).isEqualTo("Elite");
            assertThat(result.nextTier()).isNull();
            assertThat(result.pointsToNextTier()).isEqualTo(0);
            assertThat(result.progressPercent()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("throws UserNotFoundException when user missing")
        void throwsUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rankService.getRankProgress(userId))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
