package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.LifeStat;
import com.onnyth.onnythserver.models.StatCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LifeStatRepository extends JpaRepository<LifeStat, UUID> {

    List<LifeStat> findAllByUserId(UUID userId);

    Optional<LifeStat> findByUserIdAndCategory(UUID userId, StatCategory category);

    List<LifeStat> findAllByUserIdInAndCategory(List<UUID> userIds, StatCategory category);
}
