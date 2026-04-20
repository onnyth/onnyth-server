package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.RankProgressResponse;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.RankTier;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for calculating and persisting user rank tiers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankService {

    private final UserRepository userRepository;

    /**
     * Calculate the rank tier for a given score.
     *
     * @param score the total score
     * @return the corresponding RankTier
     */
    public RankTier calculateRankTier(long score) {
        return RankTier.fromScore(score);
    }

    /**
     * Recalculates and persists the user's rank tier based on their current
     * totalScore.
     * Only saves if the tier has actually changed.
     *
     * @param userId the user's ID
     * @return the new RankTier
     */
    @Transactional
    public RankTier updateUserRank(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        RankTier newTier = calculateRankTier(user.getTotalScore());
        RankTier oldTier = user.getRankTier();

        if (oldTier != newTier) {
            user.setRankTier(newTier);
            userRepository.save(user);
            log.info("Rank changed for user {}: {} → {}", userId, oldTier, newTier);
        }

        return newTier;
    }

    /**
     * Get rank progress information for a user.
     *
     * @param userId the user's ID
     * @return RankProgressResponse with current tier and progress
     */
    public RankProgressResponse getRankProgress(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        RankTier tier = user.getRankTier();
        long totalScore = user.getTotalScore();

        return RankProgressResponse.fromScoreAndTier(totalScore, tier);
    }
}
