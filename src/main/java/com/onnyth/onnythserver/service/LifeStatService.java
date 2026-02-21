package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.LifeStatResponse;
import com.onnyth.onnythserver.dto.StatInputRequest;
import com.onnyth.onnythserver.exceptions.InvalidStatValueException;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.LifeStat;
import com.onnyth.onnythserver.repository.LifeStatRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserRepository userRepository;

    /**
     * Save or update a single life stat for a user.
     * Upserts: if the stat already exists for this user + category, it updates the
     * value.
     */
    @Transactional
    public LifeStatResponse saveStat(UUID userId, StatInputRequest request) {
        validateUserExists(userId);
        validateStatValue(request);

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

        return LifeStatResponse.fromEntity(saved);
    }

    /**
     * Save or update multiple life stats at once (bulk upsert for onboarding).
     */
    @Transactional
    public List<LifeStatResponse> saveStats(UUID userId, List<StatInputRequest> requests) {
        validateUserExists(userId);
        requests.forEach(this::validateStatValue);

        return requests.stream()
                .map(request -> saveSingleStat(userId, request))
                .toList();
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

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId.toString());
        }
    }

    private void validateStatValue(StatInputRequest request) {
        if (!request.category().isValidValue(request.value())) {
            throw new InvalidStatValueException(
                    String.format("Value %d is out of range for %s (allowed: %d-%d)",
                            request.value(),
                            request.category().getDisplayName(),
                            request.category().getMinValue(),
                            request.category().getMaxValue()));
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
