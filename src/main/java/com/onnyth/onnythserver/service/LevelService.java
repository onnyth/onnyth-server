package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.LevelProgressResponse;
import com.onnyth.onnythserver.events.LevelUpEvent;
import com.onnyth.onnythserver.events.XpAwardedEvent;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LevelService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Progressive XP curve: xpForLevel(n) = 100 + floor((n-1) / 5) * 50
     * Level 1→2: 100, Level 5→6: 150, Level 10→11: 200, Level 20→21: 300, Level 50→51: 500
     */
    public static long xpForLevel(int level) {
        return 100L + ((long) (level - 1) / 5) * 50;
    }

    /**
     * Calculate the total cumulative XP required to reach a given level.
     */
    public static long totalXpForLevel(int level) {
        long total = 0;
        for (int i = 1; i < level; i++) {
            total += xpForLevel(i);
        }
        return total;
    }

    /**
     * Calculate the level from total XP.
     */
    public static int calculateLevel(long totalXp) {
        int level = 1;
        long remaining = totalXp;
        while (remaining >= xpForLevel(level)) {
            remaining -= xpForLevel(level);
            level++;
        }
        return level;
    }

    /**
     * Map level to a title string.
     */
    public static String getTitle(int level) {
        if (level >= 50) return "Grandmaster";
        if (level >= 40) return "Master";
        if (level >= 30) return "Expert";
        if (level >= 20) return "Adept";
        if (level >= 10) return "Journeyman";
        if (level >= 5) return "Apprentice";
        return "Novice";
    }

    /**
     * Get level progress for a user.
     */
    @Transactional(readOnly = true)
    public LevelProgressResponse getLevelProgress(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        int currentLevel = user.getLevel();
        long currentXp = user.getXp();
        long xpAtCurrentLevel = totalXpForLevel(currentLevel);
        long xpNeeded = xpForLevel(currentLevel);
        long xpIntoCurrentLevel = currentXp - xpAtCurrentLevel;

        double progressPercent = xpNeeded > 0
                ? Math.round((double) xpIntoCurrentLevel / xpNeeded * 1000.0) / 10.0
                : 100.0;

        return LevelProgressResponse.builder()
                .currentLevel(currentLevel)
                .title(getTitle(currentLevel))
                .currentXP(currentXp)
                .xpForNextLevel(xpAtCurrentLevel + xpNeeded)
                .progressPercent(progressPercent)
                .build();
    }

    /**
     * Check if a user should level up and update their level.
     * Publishes LevelUpEvent if the level changes.
     *
     * @return true if a level-up occurred
     */
    @Transactional
    public boolean checkAndUpdateLevel(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        int oldLevel = user.getLevel();
        int newLevel = calculateLevel(user.getXp());

        if (newLevel > oldLevel) {
            user.setLevel(newLevel);
            userRepository.save(user);

            String newTitle = getTitle(newLevel);
            log.info("Level up: userId={}, {} → {} ({})", userId, oldLevel, newLevel, newTitle);

            eventPublisher.publishEvent(new LevelUpEvent(userId, oldLevel, newLevel, newTitle));
            return true;
        }

        return false;
    }

    /**
     * Listen for XP changes and check for level-ups.
     */
    @EventListener
    @Transactional
    public void onXpAwarded(XpAwardedEvent event) {
        checkAndUpdateLevel(event.userId());
    }
}
