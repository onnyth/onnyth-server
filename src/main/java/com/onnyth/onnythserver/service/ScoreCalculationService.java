package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.events.StatChangedEvent;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.LifeStat;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.LifeStatRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for calculating and persisting the user's weighted life score.
 * Listens for StatChangedEvent to auto-recalculate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreCalculationService {

    private final LifeStatRepository lifeStatRepository;
    private final UserRepository userRepository;
    private final RankService rankService;

    /**
     * Calculates the weighted score from a list of stats.
     * Formula: score = Σ(statValue × categoryWeight), rounded to nearest long.
     *
     * @param stats the list of life stats
     * @return the weighted total score
     */
    public long calculateScore(List<LifeStat> stats) {
        return Math.round(stats.stream()
                .mapToDouble(stat -> stat.getValue() * stat.getCategory().getWeight())
                .sum());
    }

    /**
     * Recalculates and persists the user's total score, then updates rank.
     *
     * @param userId the user ID
     * @return the new total score
     */
    @Transactional
    public long recalculateUserScore(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        List<LifeStat> stats = lifeStatRepository.findAllByUserId(userId);
        long newScore = calculateScore(stats);

        user.setTotalScore(newScore);
        userRepository.save(user);

        // Update rank tier after score change
        rankService.updateUserRank(userId);

        log.info("Recalculated score for user {}: {}", userId, newScore);
        return newScore;
    }

    /**
     * Event listener: auto-recalculates score when stats change.
     */
    @EventListener
    @Transactional
    public void onStatChanged(StatChangedEvent event) {
        log.debug("StatChangedEvent received for user {}", event.userId());
        recalculateUserScore(event.userId());
    }
}
