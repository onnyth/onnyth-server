package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.dto.LifeStatResponse;
import com.onnyth.onnythserver.dto.StatInputRequest;
import com.onnyth.onnythserver.dto.StatUpdateRequest;
import com.onnyth.onnythserver.dto.StatUpdateResponse;
import com.onnyth.onnythserver.events.StatChangedEvent;
import com.onnyth.onnythserver.exceptions.InvalidStatValueException;
import com.onnyth.onnythserver.exceptions.StatNotFoundException;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.LifeStat;
import com.onnyth.onnythserver.models.LifeStatHistory;
import com.onnyth.onnythserver.models.StatCategory;
import com.onnyth.onnythserver.repository.LifeStatHistoryRepository;
import com.onnyth.onnythserver.repository.LifeStatRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.service.LifeStatService;
import com.onnyth.onnythserver.service.ScoreCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LifeStatService.
 */
@ExtendWith(MockitoExtension.class)
class LifeStatServiceTest {

    @Mock
    private LifeStatRepository lifeStatRepository;

    @Mock
    private LifeStatHistoryRepository lifeStatHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ScoreCalculationService scoreCalculationService;

    @InjectMocks
    private LifeStatService lifeStatService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    // ─── saveStat() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("saveStat()")
    class SaveStat {

        @Test
        @DisplayName("creates new stat when none exists")
        void createsNewStat_whenNoneExists() {
            StatInputRequest request = new StatInputRequest(StatCategory.CAREER, 75, null);

            when(userRepository.existsById(userId)).thenReturn(true);
            when(lifeStatRepository.findByUserIdAndCategory(userId, StatCategory.CAREER))
                    .thenReturn(Optional.empty());
            when(lifeStatRepository.save(any(LifeStat.class))).thenAnswer(inv -> {
                LifeStat stat = inv.getArgument(0);
                stat.setId(UUID.randomUUID());
                return stat;
            });

            LifeStatResponse result = lifeStatService.saveStat(userId, request);

            assertThat(result.category()).isEqualTo(StatCategory.CAREER);
            assertThat(result.value()).isEqualTo(75);
            assertThat(result.displayName()).isEqualTo("Career");
            verify(lifeStatRepository).save(any(LifeStat.class));
        }

        @Test
        @DisplayName("updates existing stat")
        void updatesExistingStat() {
            StatInputRequest request = new StatInputRequest(StatCategory.FITNESS, 90, "updated");
            LifeStat existing = LifeStat.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .category(StatCategory.FITNESS)
                    .value(50)
                    .lastUpdated(Instant.now().minusSeconds(3600))
                    .build();

            when(userRepository.existsById(userId)).thenReturn(true);
            when(lifeStatRepository.findByUserIdAndCategory(userId, StatCategory.FITNESS))
                    .thenReturn(Optional.of(existing));
            when(lifeStatRepository.save(any(LifeStat.class))).thenAnswer(inv -> inv.getArgument(0));

            LifeStatResponse result = lifeStatService.saveStat(userId, request);

            assertThat(result.value()).isEqualTo(90);
            assertThat(result.metadata()).isEqualTo("updated");
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwsUserNotFound() {
            when(userRepository.existsById(userId)).thenReturn(false);

            StatInputRequest request = new StatInputRequest(StatCategory.CAREER, 50, null);

            assertThatThrownBy(() -> lifeStatService.saveStat(userId, request))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("throws InvalidStatValueException for out-of-range value")
        void throwsInvalidStatValue_whenOutOfRange() {
            when(userRepository.existsById(userId)).thenReturn(true);

            StatInputRequest request = new StatInputRequest(StatCategory.CAREER, 0, null);

            assertThatThrownBy(() -> lifeStatService.saveStat(userId, request))
                    .isInstanceOf(InvalidStatValueException.class);
        }
    }

    // ─── saveStats() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("saveStats()")
    class SaveStats {

        @Test
        @DisplayName("saves multiple stats in bulk")
        void savesMultipleStats() {
            List<StatInputRequest> requests = List.of(
                    new StatInputRequest(StatCategory.CAREER, 75, null),
                    new StatInputRequest(StatCategory.FITNESS, 80, null),
                    new StatInputRequest(StatCategory.EDUCATION, 65, null));

            when(userRepository.existsById(userId)).thenReturn(true);
            when(lifeStatRepository.findByUserIdAndCategory(eq(userId), any(StatCategory.class)))
                    .thenReturn(Optional.empty());
            when(lifeStatRepository.save(any(LifeStat.class))).thenAnswer(inv -> {
                LifeStat stat = inv.getArgument(0);
                stat.setId(UUID.randomUUID());
                return stat;
            });

            List<LifeStatResponse> results = lifeStatService.saveStats(userId, requests);

            assertThat(results).hasSize(3);
            assertThat(results.get(0).category()).isEqualTo(StatCategory.CAREER);
            assertThat(results.get(1).category()).isEqualTo(StatCategory.FITNESS);
            assertThat(results.get(2).category()).isEqualTo(StatCategory.EDUCATION);
            verify(lifeStatRepository, times(3)).save(any(LifeStat.class));
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwsUserNotFound() {
            when(userRepository.existsById(userId)).thenReturn(false);

            List<StatInputRequest> requests = List.of(
                    new StatInputRequest(StatCategory.CAREER, 50, null)
            );

            assertThatThrownBy(() -> lifeStatService.saveStats(userId, requests))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("throws InvalidStatValueException if any value is invalid")
        void throwsInvalidStatValue_whenAnyInvalid() {
            when(userRepository.existsById(userId)).thenReturn(true);

            List<StatInputRequest> requests = List.of(
                    new StatInputRequest(StatCategory.CAREER, 50, null),
                    new StatInputRequest(StatCategory.FITNESS, 101, null) // invalid
            );

            assertThatThrownBy(() -> lifeStatService.saveStats(userId, requests))
                    .isInstanceOf(InvalidStatValueException.class);
        }
    }

    // ─── getUserStats() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserStats()")
    class GetUserStats {

        @Test
        @DisplayName("returns all stats for a user")
        void returnsAllStats() {
            List<LifeStat> stats = List.of(
                    LifeStat.builder().id(UUID.randomUUID()).userId(userId)
                            .category(StatCategory.CAREER).value(70).lastUpdated(Instant.now()).build(),
                    LifeStat.builder().id(UUID.randomUUID()).userId(userId)
                            .category(StatCategory.FITNESS).value(85).lastUpdated(Instant.now()).build());

            when(userRepository.existsById(userId)).thenReturn(true);
            when(lifeStatRepository.findAllByUserId(userId)).thenReturn(stats);

            List<LifeStatResponse> results = lifeStatService.getUserStats(userId);

            assertThat(results).hasSize(2);
            assertThat(results.get(0).category()).isEqualTo(StatCategory.CAREER);
            assertThat(results.get(1).category()).isEqualTo(StatCategory.FITNESS);
        }

        @Test
        @DisplayName("returns empty list when user has no stats")
        void returnsEmptyList_whenNoStats() {
            when(userRepository.existsById(userId)).thenReturn(true);
            when(lifeStatRepository.findAllByUserId(userId)).thenReturn(List.of());

            List<LifeStatResponse> results = lifeStatService.getUserStats(userId);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwsUserNotFound() {
            when(userRepository.existsById(userId)).thenReturn(false);

            assertThatThrownBy(() -> lifeStatService.getUserStats(userId))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    // ─── updateStat() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateStat()")
    class UpdateStat {

        @Test
        @DisplayName("updates stat and returns response with score change")
        void updatesStatSuccessfully() {
            LifeStat existing = LifeStat.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .category(StatCategory.CAREER)
                    .value(50)
                    .lastUpdated(Instant.now().minusSeconds(3600))
                    .build();

            when(userRepository.existsById(userId)).thenReturn(true);
            when(lifeStatRepository.findByUserIdAndCategory(userId, StatCategory.CAREER))
                    .thenReturn(Optional.of(existing));
            when(lifeStatRepository.save(any(LifeStat.class))).thenAnswer(inv -> inv.getArgument(0));
            when(lifeStatHistoryRepository.save(any(LifeStatHistory.class))).thenAnswer(inv -> inv.getArgument(0));
            when(lifeStatRepository.findAllByUserId(userId)).thenReturn(List.of(existing));
            when(scoreCalculationService.calculateScore(any())).thenReturn(90L);

            StatUpdateRequest request = new StatUpdateRequest(75, "promotion");
            StatUpdateResponse result = lifeStatService.updateStat(userId, StatCategory.CAREER, request);

            assertThat(result.category()).isEqualTo(StatCategory.CAREER);
            assertThat(result.previousValue()).isEqualTo(50);
            assertThat(result.newValue()).isEqualTo(75);
            // scoreChange = round(75*1.2) - round(50*1.2) = 90 - 60 = 30
            assertThat(result.scoreChange()).isEqualTo(30);
            verify(lifeStatHistoryRepository).save(any(LifeStatHistory.class));
            verify(eventPublisher).publishEvent(any(StatChangedEvent.class));
        }

        @Test
        @DisplayName("throws StatNotFoundException when stat not set")
        void throwsStatNotFound() {
            when(userRepository.existsById(userId)).thenReturn(true);
            when(lifeStatRepository.findByUserIdAndCategory(userId, StatCategory.CAREER))
                    .thenReturn(Optional.empty());

            StatUpdateRequest request = new StatUpdateRequest(75, null);

            assertThatThrownBy(() -> lifeStatService.updateStat(userId, StatCategory.CAREER, request))
                    .isInstanceOf(StatNotFoundException.class);
        }

        @Test
        @DisplayName("throws InvalidStatValueException for out-of-range")
        void throwsInvalidStatValue() {
            when(userRepository.existsById(userId)).thenReturn(true);

            StatUpdateRequest request = new StatUpdateRequest(0, null);

            assertThatThrownBy(() -> lifeStatService.updateStat(userId, StatCategory.CAREER, request))
                    .isInstanceOf(InvalidStatValueException.class);
        }

        @Test
        @DisplayName("sets previousValue on the entity")
        void setsPreviousValueOnEntity() {
            LifeStat existing = LifeStat.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .category(StatCategory.FITNESS)
                    .value(60)
                    .lastUpdated(Instant.now())
                    .build();

            when(userRepository.existsById(userId)).thenReturn(true);
            when(lifeStatRepository.findByUserIdAndCategory(userId, StatCategory.FITNESS))
                    .thenReturn(Optional.of(existing));
            when(lifeStatRepository.save(any(LifeStat.class))).thenAnswer(inv -> inv.getArgument(0));
            when(lifeStatHistoryRepository.save(any(LifeStatHistory.class))).thenAnswer(inv -> inv.getArgument(0));
            when(lifeStatRepository.findAllByUserId(userId)).thenReturn(List.of(existing));

            lifeStatService.updateStat(userId, StatCategory.FITNESS, new StatUpdateRequest(80, null));

            assertThat(existing.getPreviousValue()).isEqualTo(60);
            assertThat(existing.getValue()).isEqualTo(80);
        }
    }

    // ─── calculateTotalScore() ────────────────────────────────────────────────

    @Nested
    @DisplayName("calculateTotalScore()")
    class CalculateTotalScore {

        @Test
        @DisplayName("delegates to ScoreCalculationService")
        void delegatesToScoreCalculation() {
            List<LifeStat> stats = List.of(
                    LifeStat.builder().userId(userId).category(StatCategory.CAREER).value(70).lastUpdated(Instant.now())
                            .build(),
                    LifeStat.builder().userId(userId).category(StatCategory.FITNESS).value(85)
                            .lastUpdated(Instant.now()).build());

            when(lifeStatRepository.findAllByUserId(userId)).thenReturn(stats);
            when(scoreCalculationService.calculateScore(stats)).thenReturn(178L);

            long total = lifeStatService.calculateTotalScore(userId);

            assertThat(total).isEqualTo(178);
            verify(scoreCalculationService).calculateScore(stats);
        }

        @Test
        @DisplayName("returns 0 when no stats")
        void returnsZero_whenNoStats() {
            when(lifeStatRepository.findAllByUserId(userId)).thenReturn(List.of());
            when(scoreCalculationService.calculateScore(List.of())).thenReturn(0L);

            long total = lifeStatService.calculateTotalScore(userId);

            assertThat(total).isEqualTo(0);
        }
    }
}
