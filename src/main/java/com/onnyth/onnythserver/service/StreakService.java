package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.StreakResponse;
import com.onnyth.onnythserver.models.UserStreak;
import com.onnyth.onnythserver.repository.UserStreakRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreakService {

    private static final List<Integer> MILESTONE_STREAKS = List.of(7, 14, 30, 50, 100);

    private final UserStreakRepository userStreakRepository;

    /**
     * Record an activity for streak tracking.
     * Returns true if the streak was updated (not already counted today).
     */
    @Transactional
    public boolean recordActivity(UUID userId) {
        LocalDate today = LocalDate.now();

        UserStreak streak = userStreakRepository.findByUserId(userId)
                .orElseGet(() -> UserStreak.builder()
                        .userId(userId)
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());

        // Already counted today
        if (streak.getLastActivityDate() != null && streak.getLastActivityDate().equals(today)) {
            return false;
        }

        // Consecutive day
        if (streak.getLastActivityDate() != null && streak.getLastActivityDate().equals(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            // Streak broken or first activity
            streak.setCurrentStreak(1);
        }

        // Update longest streak
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        streak.setLastActivityDate(today);
        userStreakRepository.save(streak);

        log.info("Streak updated: userId={}, currentStreak={}, longest={}",
                userId, streak.getCurrentStreak(), streak.getLongestStreak());

        return true;
    }

    /**
     * Check if the current streak is at a milestone.
     */
    public boolean isStreakMilestone(int currentStreak) {
        return MILESTONE_STREAKS.contains(currentStreak);
    }

    /**
     * Get streak data for a user.
     */
    @Transactional(readOnly = true)
    public StreakResponse getStreak(UUID userId) {
        UserStreak streak = userStreakRepository.findByUserId(userId)
                .orElse(UserStreak.builder()
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());

        LocalDate today = LocalDate.now();
        boolean isActive = streak.getLastActivityDate() != null &&
                (streak.getLastActivityDate().equals(today) ||
                 streak.getLastActivityDate().equals(today.minusDays(1)));

        return StreakResponse.builder()
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .lastActivityDate(streak.getLastActivityDate())
                .isActive(isActive)
                .build();
    }
}
