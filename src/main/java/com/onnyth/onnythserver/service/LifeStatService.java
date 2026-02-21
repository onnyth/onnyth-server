package com.onnyth.onnythserver.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LifeStatService {

    private final LifeStatRepository lifeStatRepository;
    private final LifeStatHistoryRepository lifeStatHistoryRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ScoreCalculationService scoreCalculationService;

    /**
     * Save or update a single life stat for a user.
     * Upserts: if the stat already exists for this user + category, it updates the
     * value.
     */
    @Transactional
    public LifeStatResponse saveStat(UUID userId, StatInputRequest request) {
        validateUserExists(userId);
        validateStatValue(request.category(), request.value());

        LifeStat stat = lifeStatRepository.findByUserIdAndCategory(userId, request.category())
                .map(existing -> {
                    existing.setValue(request.value());
                    existing.setMetadata(request.metadata());
                    existing.setLastUpdated(Instant.now());
                    return existing;
                })
                .orElseGet(() -> LifeStat.builder()
                        .userId(userId)
                        .category(request.category())
                        .value(request.value())
                        .metadata(request.metadata())
                        .lastUpdated(Instant.now())
                        .build());

        LifeStat saved = lifeStatRepository.save(stat);
        log.info("Saved stat {} = {} for user {}", request.category(), request.value(), userId);

        eventPublisher.publishEvent(new StatChangedEvent(userId));

        return LifeStatResponse.fromEntity(saved);
    }

    /**
     * Save or update multiple life stats at once (bulk upsert for onboarding).
     */
    @Transactional
    public List<LifeStatResponse> saveStats(UUID userId, List<StatInputRequest> requests) {
        validateUserExists(userId);
        requests.forEach(r -> validateStatValue(r.category(), r.value()));

        List<LifeStatResponse> results = requests.stream()
                .map(request -> saveSingleStat(userId, request))
                .toList();

        eventPublisher.publishEvent(new StatChangedEvent(userId));

        return results;
    }

    /**
     * Update an existing stat with history tracking and score recalculation.
     */
    @Transactional
    public StatUpdateResponse updateStat(UUID userId, StatCategory category, StatUpdateRequest request) {
        validateUserExists(userId);
        validateStatValue(category, request.newValue());

        LifeStat stat = lifeStatRepository.findByUserIdAndCategory(userId, category)
                .orElseThrow(() -> new StatNotFoundException(
                        String.format("Stat %s not found for user %s. Use POST /api/v1/stats to create it first.",
                                category.getDisplayName(), userId)));

        int oldValue = stat.getValue();

        // Update stat with previous value tracking
        stat.setPreviousValue(oldValue);
        stat.setValue(request.newValue());
        stat.setLastUpdated(Instant.now());
        lifeStatRepository.save(stat);

        // Record history
        LifeStatHistory history = LifeStatHistory.builder()
                .userId(userId)
                .category(category)
                .oldValue(oldValue)
                .newValue(request.newValue())
                .reason(request.reason())
                .changedAt(Instant.now())
                .build();
        lifeStatHistoryRepository.save(history);

        long totalScore = scoreCalculationService.calculateScore(
                lifeStatRepository.findAllByUserId(userId));
        long scoreChange = Math.round(request.newValue() * category.getWeight())
                - Math.round(oldValue * category.getWeight());

        log.info("Updated stat {} for user {}: {} → {} (delta: {})",
                category, userId, oldValue, request.newValue(), scoreChange);

        eventPublisher.publishEvent(new StatChangedEvent(userId));

        return StatUpdateResponse.builder()
                .category(category)
                .displayName(category.getDisplayName())
                .previousValue(oldValue)
                .newValue(request.newValue())
                .totalScore(totalScore)
                .scoreChange(scoreChange)
                .build();
    }

    /**
     * Get all life stats for a user.
     */
    public List<LifeStatResponse> getUserStats(UUID userId) {
        validateUserExists(userId);

        return lifeStatRepository.findAllByUserId(userId).stream()
                .map(LifeStatResponse::fromEntity)
                .toList();
    }

    /**
     * Calculate total weighted score for a user.
     * Delegates to ScoreCalculationService for the weighted formula.
     */
    public long calculateTotalScore(UUID userId) {
        return scoreCalculationService.calculateScore(
                lifeStatRepository.findAllByUserId(userId));
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId.toString());
        }
    }

    private void validateStatValue(StatCategory category, int value) {
        if (!category.isValidValue(value)) {
            throw new InvalidStatValueException(
                    String.format("Value %d is out of range for %s (allowed: %d-%d)",
                            value,
                            category.getDisplayName(),
                            category.getMinValue(),
                            category.getMaxValue()));
        }
    }

    /**
     * Internal upsert for a single stat (used within the bulk transaction).
     */
    private LifeStatResponse saveSingleStat(UUID userId, StatInputRequest request) {
        LifeStat stat = lifeStatRepository.findByUserIdAndCategory(userId, request.category())
                .map(existing -> {
                    existing.setValue(request.value());
                    existing.setMetadata(request.metadata());
                    existing.setLastUpdated(Instant.now());
                    return existing;
                })
                .orElseGet(() -> LifeStat.builder()
                        .userId(userId)
                        .category(request.category())
                        .value(request.value())
                        .metadata(request.metadata())
                        .lastUpdated(Instant.now())
                        .build());

        LifeStat saved = lifeStatRepository.save(stat);
        return LifeStatResponse.fromEntity(saved);
    }
}
