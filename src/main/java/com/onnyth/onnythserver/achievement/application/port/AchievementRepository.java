package com.onnyth.onnythserver.achievement.application.port;

import com.onnyth.onnythserver.achievement.domain.model.Achievement;
import com.onnyth.onnythserver.achievement.domain.model.AchievementCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementRepository {

    Achievement save(Achievement achievement);

    Optional<Achievement> findById(UUID id);

    List<Achievement> findAllById(Iterable<UUID> ids);

    List<Achievement> findAllByCategory(AchievementCategory category);

    Optional<Achievement> findByCode(String code);

    List<Achievement> findAllByIsActiveTrue();
}
