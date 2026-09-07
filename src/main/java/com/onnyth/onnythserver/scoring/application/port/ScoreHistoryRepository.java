package com.onnyth.onnythserver.scoring.application.port;

import com.onnyth.onnythserver.shared.domain.model.StatDomain;
import com.onnyth.onnythserver.scoring.domain.model.ScoreHistory;

import java.util.List;
import java.util.UUID;

public interface ScoreHistoryRepository {

    ScoreHistory save(ScoreHistory scoreHistory);

    List<ScoreHistory> findAllByUserIdOrderByChangedAtDesc(UUID userId);

    List<ScoreHistory> findAllByUserIdAndDomainOrderByChangedAtDesc(UUID userId, StatDomain domain);

    void deleteAllByUserId(UUID userId);
}
