package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.ScoreHistory;
import com.onnyth.onnythserver.models.StatDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, UUID> {

    List<ScoreHistory> findAllByUserIdOrderByChangedAtDesc(UUID userId);

    List<ScoreHistory> findAllByUserIdAndDomainOrderByChangedAtDesc(UUID userId, StatDomain domain);
}
