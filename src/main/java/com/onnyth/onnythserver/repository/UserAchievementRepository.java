package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {

    List<UserAchievement> findAllByUserId(UUID userId);

    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, UUID achievementId);

    int countByUserId(UUID userId);

    boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);
}
