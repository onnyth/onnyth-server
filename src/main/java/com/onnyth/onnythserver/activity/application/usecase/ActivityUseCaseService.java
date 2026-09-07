package com.onnyth.onnythserver.activity.application.usecase;

import com.onnyth.onnythserver.activity.adapter.in.rest.dto.ActivityLogResponse;
import com.onnyth.onnythserver.activity.adapter.in.rest.dto.ActivityStatusResponse;
import com.onnyth.onnythserver.activity.adapter.in.rest.dto.ActivityTypeResponse;
import com.onnyth.onnythserver.activity.application.exception.ActivityCooldownException;
import com.onnyth.onnythserver.activity.application.port.ActivityLogRepository;
import com.onnyth.onnythserver.activity.application.port.ActivityTypeRepository;
import com.onnyth.onnythserver.activity.domain.model.ActivityLog;
import com.onnyth.onnythserver.activity.domain.model.ActivityType;
import com.onnyth.onnythserver.leveling.application.usecase.LevelUseCaseService;
import com.onnyth.onnythserver.streak.application.usecase.StreakUseCaseService;
import com.onnyth.onnythserver.user.application.exception.UserNotFoundException;
import com.onnyth.onnythserver.user.application.port.UserRepository;
import com.onnyth.onnythserver.user.domain.model.User;
import com.onnyth.onnythserver.xp.application.usecase.XpUseCaseService;
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
public class ActivityUseCaseService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final UserRepository userRepository;
    private final ActivityTypeUseCaseService activityTypeUseCaseService;
    private final XpUseCaseService xpService;
    private final LevelUseCaseService levelService;
    private final StreakUseCaseService streakService;

    /**
     * Log an activity: validate cooldown, persist log, award XP, check level-up, update streak.
     */
    @Transactional
    public ActivityLogResponse logActivity(UUID userId, UUID activityTypeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        ActivityType activityType = activityTypeUseCaseService.getActiveActivityType(activityTypeId);

        Instant cooldownThreshold = Instant.now().minus(activityType.getCooldownHours(), ChronoUnit.HOURS);
        activityLogRepository.findFirstByUserIdAndActivityTypeIdAndLoggedAtAfterOrderByLoggedAtDesc(
                userId, activityTypeId, cooldownThreshold
        ).ifPresent(recentLog -> {
            Instant availableAt = recentLog.getLoggedAt().plus(activityType.getCooldownHours(), ChronoUnit.HOURS);
            throw new ActivityCooldownException(
                    "Activity '" + activityType.getName() + "' is on cooldown. Available at: " + availableAt);
        });

        ActivityLog activityLog = ActivityLog.builder()
                .userId(userId)
                .activityTypeId(activityTypeId)
                .xpEarned(activityType.getXpReward())
                .loggedAt(Instant.now())
                .build();
        activityLog = activityLogRepository.save(activityLog);

        long newTotalXp = xpService.awardXp(userId, activityType.getXpReward());
        user = userRepository.findById(userId).orElse(user);
        boolean streakUpdated = streakService.recordActivity(userId);

        log.info("Activity logged: userId={}, type={}, xp={}", userId, activityType.getName(), activityType.getXpReward());

        return ActivityLogResponse.builder()
                .id(activityLog.getId())
                .activityType(ActivityTypeResponse.fromEntity(activityType))
                .xpEarned(activityType.getXpReward())
                .loggedAt(activityLog.getLoggedAt())
                .newTotalXP(newTotalXp)
                .newLevel(user.getLevel())
                .levelTitle(LevelUseCaseService.getTitle(user.getLevel()))
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

        Map<UUID, ActivityType> typeMap = activityTypeRepository.findAllByIsActiveTrue().stream()
                .collect(Collectors.toMap(ActivityType::getId, type -> type));

        List<ActivityTypeResponse> loggedToday = todayLogs.stream()
                .map(log -> typeMap.get(log.getActivityTypeId()))
                .filter(java.util.Objects::nonNull)
                .map(ActivityTypeResponse::fromEntity)
                .toList();

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
