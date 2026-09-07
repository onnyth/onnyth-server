package com.onnyth.onnythserver.scoring.adapter.out.persistence;

import com.onnyth.onnythserver.shared.domain.model.StatDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScoreHistoryJpaRepository extends JpaRepository<ScoreHistoryEntity, UUID> {

    List<ScoreHistoryEntity> findAllByUserIdOrderByChangedAtDesc(UUID userId);

    List<ScoreHistoryEntity> findAllByUserIdAndDomainOrderByChangedAtDesc(UUID userId, StatDomain domain);

    void deleteAllByUserId(UUID userId);
}
