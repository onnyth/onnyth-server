package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.LifeStatHistory;
import com.onnyth.onnythserver.models.StatCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LifeStatHistoryRepository extends JpaRepository<LifeStatHistory, UUID> {

    List<LifeStatHistory> findAllByUserIdOrderByChangedAtDesc(UUID userId);

    List<LifeStatHistory> findAllByUserIdAndCategoryOrderByChangedAtDesc(UUID userId, StatCategory category);
}
