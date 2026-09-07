package com.onnyth.onnythserver.achievement.application.usecase;

import com.onnyth.onnythserver.achievement.application.AchievementProgressCalculator;
import com.onnyth.onnythserver.achievement.application.port.AchievementRepository;
import com.onnyth.onnythserver.achievement.application.port.UserAchievementRepository;
import com.onnyth.onnythserver.achievement.domain.model.Achievement;
import com.onnyth.onnythserver.achievement.domain.model.UserAchievement;
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
public class AchievementUnlockUseCaseService {

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

        Set<UUID> unlockedIds = userAchievementRepository.findAllByUserId(userId).stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        List<Achievement> newlyUnlocked = new ArrayList<>();

        for (Achievement achievement : allActive) {
            if (unlockedIds.contains(achievement.getId())) {
                continue;
            }

            int progress = progressCalculator.calculateProgress(userId, achievement);
            if (progress >= 100) {
                UserAchievement userAchievement = UserAchievement.builder()
                        .userId(userId)
                        .achievementId(achievement.getId())
                        .build();
                userAchievementRepository.save(userAchievement);
                newlyUnlocked.add(achievement);
                log.info("Achievement unlocked: {} for user {}", achievement.getCode(), userId);
            }
        }

        return newlyUnlocked;
    }
}
