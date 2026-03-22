package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.Achievement;
import com.onnyth.onnythserver.models.AchievementCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {

    List<Achievement> findAllByCategory(AchievementCategory category);

    Optional<Achievement> findByCode(String code);

    List<Achievement> findAllByIsActiveTrue();
}
