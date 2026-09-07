package com.onnyth.onnythserver.achievement.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAchievementJpaRepository extends JpaRepository<UserAchievementEntity, UUID> {

    List<UserAchievementEntity> findAllByUserId(UUID userId);

    Optional<UserAchievementEntity> findByUserIdAndAchievementId(UUID userId, UUID achievementId);

    int countByUserId(UUID userId);

    boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);

    void deleteAllByUserId(UUID userId);
}
