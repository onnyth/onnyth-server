package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.events.StatChangedEvent;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.LifeStat;
import com.onnyth.onnythserver.models.StatCategory;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.LifeStatRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.service.RankService;
import com.onnyth.onnythserver.service.ScoreCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScoreCalculationService.
 */
@ExtendWith(MockitoExtension.class)
class ScoreCalculationServiceTest {

    @Mock
    private LifeStatRepository lifeStatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RankService rankService;

    @InjectMocks
    private ScoreCalculationService scoreCalculationService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    // ─── calculateScore() ────────────────────────────────────────────────────

    @Nested
    @DisplayName("calculateScore()")
    class CalculateScore {

        @Test
        @DisplayName("returns weighted sum of all stats")
        void returnsWeightedSum() {
            List<LifeStat> stats = List.of(
                    LifeStat.builder().category(StatCategory.CAREER).value(100).lastUpdated(Instant.now()).build(),
                    LifeStat.builder().category(StatCategory.WEALTH).value(100).lastUpdated(Instant.now()).build(),
                    LifeStat.builder().category(StatCategory.FITNESS).value(100).lastUpdated(Instant.now()).build(),
                    LifeStat.builder().category(StatCategory.EDUCATION).value(100).lastUpdated(Instant.now()).build(),
                    LifeStat.builder().category(StatCategory.SOCIAL_INFLUENCE).value(100).lastUpdated(Instant.now())
                            .build());

            long score = scoreCalculationService.calculateScore(stats);

            // 100*1.2 + 100*1.0 + 100*1.1 + 100*1.3 + 100*0.9 = 550
            assertThat(score).isEqualTo(550);
        }

        @Test
        @DisplayName("returns 0 for empty stats list")
        void returnsZeroForEmptyList() {
            long score = scoreCalculationService.calculateScore(List.of());
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("applies correct weight per category")
        void appliesCorrectWeights() {
            // Career at 50: 50 * 1.2 = 60
            List<LifeStat> stats = List.of(
                    LifeStat.builder().category(StatCategory.CAREER).value(50).lastUpdated(Instant.now()).build());
            assertThat(scoreCalculationService.calculateScore(stats)).isEqualTo(60);

            // Education at 50: 50 * 1.3 = 65
            stats = List.of(
                    LifeStat.builder().category(StatCategory.EDUCATION).value(50).lastUpdated(Instant.now()).build());
            assertThat(scoreCalculationService.calculateScore(stats)).isEqualTo(65);

            // Social Influence at 50: 50 * 0.9 = 45
            stats = List.of(
                    LifeStat.builder().category(StatCategory.SOCIAL_INFLUENCE).value(50).lastUpdated(Instant.now())
                            .build());
            assertThat(scoreCalculationService.calculateScore(stats)).isEqualTo(45);
        }

        @Test
        @DisplayName("rounds correctly")
        void roundsCorrectly() {
            // Career at 55: 55 * 1.2 = 66.0 → 66
            List<LifeStat> stats = List.of(
                    LifeStat.builder().category(StatCategory.CAREER).value(55).lastUpdated(Instant.now()).build());
            assertThat(scoreCalculationService.calculateScore(stats)).isEqualTo(66);

            // Social Influence at 55: 55 * 0.9 = 49.5 → 50
            stats = List.of(
                    LifeStat.builder().category(StatCategory.SOCIAL_INFLUENCE).value(55).lastUpdated(Instant.now())
                            .build());
            assertThat(scoreCalculationService.calculateScore(stats)).isEqualTo(50);
        }
    }

    // ─── recalculateUserScore() ──────────────────────────────────────────────

    @Nested
    @DisplayName("recalculateUserScore()")
    class RecalculateUserScore {

        @Test
        @DisplayName("fetches stats, calculates score, and persists on User")
        void fetchesCalculatesAndPersists() {
            User user = User.builder().id(userId).email("test@test.com").build();
            List<LifeStat> stats = List.of(
                    LifeStat.builder().category(StatCategory.CAREER).value(80).lastUpdated(Instant.now()).build(),
                    LifeStat.builder().category(StatCategory.WEALTH).value(60).lastUpdated(Instant.now()).build());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(lifeStatRepository.findAllByUserId(userId)).thenReturn(stats);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            long score = scoreCalculationService.recalculateUserScore(userId);

            // 80*1.2 + 60*1.0 = 96 + 60 = 156
            assertThat(score).isEqualTo(156);
            assertThat(user.getTotalScore()).isEqualTo(156);
            verify(userRepository).save(user);
            verify(rankService).updateUserRank(userId);
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwsUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scoreCalculationService.recalculateUserScore(userId))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    // ─── onStatChanged() ────────────────────────────────────────────────────

    @Nested
    @DisplayName("onStatChanged()")
    class OnStatChanged {

        @Test
        @DisplayName("recalculates score when StatChangedEvent received")
        void recalculatesOnEvent() {
            User user = User.builder().id(userId).email("test@test.com").build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(lifeStatRepository.findAllByUserId(userId)).thenReturn(List.of());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            scoreCalculationService.onStatChanged(new StatChangedEvent(userId));

            verify(userRepository).save(user);
            verify(rankService).updateUserRank(userId);
            assertThat(user.getTotalScore()).isEqualTo(0);
        }
    }
}
