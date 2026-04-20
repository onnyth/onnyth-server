package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.models.Achievement;
import com.onnyth.onnythserver.models.UserAchievement;
import com.onnyth.onnythserver.repository.AchievementRepository;
import com.onnyth.onnythserver.repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Evaluates all achievement conditions for a user and unlocks any that are met.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementUnlockService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementProgressCalculator progressCalculator;

    /**
     * Check all active achievements and unlock any where progress == 100.
     * Returns list of newly unlocked achievements.
     */
    @Transactional
    public List<Achievement> checkAndUnlockAchievements(UUID userId) {
        List<Achievement> allActive = achievementRepository.findAllByIsActiveTrue();

        // Get already-unlocked achievement IDs
        Set<UUID> unlockedIds = userAchievementRepository.findAllByUserId(userId).stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        List<Achievement> newlyUnlocked = new ArrayList<>();

        for (Achievement achievement : allActive) {
            if (unlockedIds.contains(achievement.getId()))
                continue;

            int progress = progressCalculator.calculateProgress(userId, achievement);
            if (progress >= 100) {
                UserAchievement ua = UserAchievement.builder()
                        .userId(userId)
                        .achievementId(achievement.getId())
                        .build();
                userAchievementRepository.save(ua);
                newlyUnlocked.add(achievement);
                log.info("Achievement unlocked: {} for user {}", achievement.getCode(), userId);
            }
        }

        return newlyUnlocked;
    }
}
