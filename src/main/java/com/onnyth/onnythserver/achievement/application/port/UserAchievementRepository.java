package com.onnyth.onnythserver.achievement.application.port;

import com.onnyth.onnythserver.achievement.domain.model.UserAchievement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAchievementRepository {

    UserAchievement save(UserAchievement userAchievement);

    List<UserAchievement> findAllByUserId(UUID userId);

    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, UUID achievementId);

    int countByUserId(UUID userId);

    boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);

    void deleteAllByUserId(UUID userId);
}
