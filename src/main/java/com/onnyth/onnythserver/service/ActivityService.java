package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.ActivityLogResponse;
import com.onnyth.onnythserver.dto.ActivityStatusResponse;
import com.onnyth.onnythserver.dto.ActivityTypeResponse;
import com.onnyth.onnythserver.exceptions.ActivityCooldownException;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.ActivityLog;
import com.onnyth.onnythserver.models.ActivityType;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.ActivityLogRepository;
import com.onnyth.onnythserver.repository.ActivityTypeRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final UserRepository userRepository;
    private final ActivityTypeService activityTypeService;
    private final XpService xpService;
    private final LevelService levelService;
    private final StreakService streakService;

    /**
     * Log an activity: validate cooldown, persist log, award XP, check level-up, update streak.
     */
    @Transactional
    public ActivityLogResponse logActivity(UUID userId, UUID activityTypeId) {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Validate activity type
        ActivityType activityType = activityTypeService.getActiveActivityType(activityTypeId);

        // Check cooldown
        Instant cooldownThreshold = Instant.now().minus(activityType.getCooldownHours(), ChronoUnit.HOURS);
        activityLogRepository.findFirstByUserIdAndActivityTypeIdAndLoggedAtAfterOrderByLoggedAtDesc(
                userId, activityTypeId, cooldownThreshold
        ).ifPresent(recentLog -> {
            Instant availableAt = recentLog.getLoggedAt().plus(activityType.getCooldownHours(), ChronoUnit.HOURS);
            throw new ActivityCooldownException(
                    "Activity '" + activityType.getName() + "' is on cooldown. Available at: " + availableAt);
        });

        // Persist activity log
        ActivityLog activityLog = ActivityLog.builder()
                .userId(userId)
                .activityTypeId(activityTypeId)
                .xpEarned(activityType.getXpReward())
                .loggedAt(Instant.now())
                .build();
        activityLogRepository.save(activityLog);

        // Award XP (triggers level check via event)
        long newTotalXp = xpService.awardXp(userId, activityType.getXpReward());

        // Refresh user to get updated level
        user = userRepository.findById(userId).orElse(user);

        // Update streak
        boolean streakUpdated = streakService.recordActivity(userId);

        log.info("Activity logged: userId={}, type={}, xp={}", userId, activityType.getName(), activityType.getXpReward());

        return ActivityLogResponse.builder()
                .id(activityLog.getId())
                .activityType(ActivityTypeResponse.fromEntity(activityType))
                .xpEarned(activityType.getXpReward())
                .loggedAt(activityLog.getLoggedAt())
                .newTotalXP(newTotalXp)
                .newLevel(user.getLevel())
                .levelTitle(LevelService.getTitle(user.getLevel()))
                .streakUpdated(streakUpdated)
                .build();
    }

    /**
     * Get activity history for a user.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getActivityHistory(UUID userId, Pageable pageable) {
        return activityLogRepository.findAllByUserIdOrderByLoggedAtDesc(userId, pageable)
                .map(log -> {
                    ActivityType type = activityTypeRepository.findById(log.getActivityTypeId()).orElse(null);
                    return ActivityLogResponse.builder()
                            .id(log.getId())
                            .activityType(type != null ? ActivityTypeResponse.fromEntity(type) : null)
                            .xpEarned(log.getXpEarned())
                            .loggedAt(log.getLoggedAt())
                            .build();
                });
    }

    /**
     * Get today's activity status: what was logged today + cooldowns.
     */
    @Transactional(readOnly = true)
    public ActivityStatusResponse getActivityStatus(UUID userId) {
        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<ActivityLog> todayLogs = activityLogRepository.findAllByUserIdAndLoggedAtBetween(userId, startOfDay, endOfDay);

        // Get activity types for today's logs
        Map<UUID, ActivityType> typeMap = activityTypeRepository.findAllByIsActiveTrue().stream()
                .collect(Collectors.toMap(ActivityType::getId, t -> t));

        List<ActivityTypeResponse> loggedToday = todayLogs.stream()
                .map(l -> typeMap.get(l.getActivityTypeId()))
                .filter(java.util.Objects::nonNull)
                .map(ActivityTypeResponse::fromEntity)
                .toList();

        // Calculate cooldowns
        List<ActivityStatusResponse.CooldownEntry> cooldowns = new ArrayList<>();
        for (ActivityLog todayLog : todayLogs) {
            ActivityType type = typeMap.get(todayLog.getActivityTypeId());
            if (type != null) {
                Instant availableAt = todayLog.getLoggedAt().plus(type.getCooldownHours(), ChronoUnit.HOURS);
                if (availableAt.isAfter(Instant.now())) {
                    cooldowns.add(ActivityStatusResponse.CooldownEntry.builder()
                            .activityTypeId(type.getId())
                            .availableAt(availableAt)
                            .build());
                }
            }
        }

        return ActivityStatusResponse.builder()
                .todayLogs(loggedToday)
                .cooldowns(cooldowns)
                .build();
    }
}
