package com.onnyth.onnythserver.achievement.adapter.out.persistence;

import com.onnyth.onnythserver.achievement.domain.model.AchievementCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementJpaRepository extends JpaRepository<AchievementEntity, UUID> {

    List<AchievementEntity> findAllByCategory(AchievementCategory category);

    Optional<AchievementEntity> findByCode(String code);

    List<AchievementEntity> findAllByIsActiveTrue();
}
