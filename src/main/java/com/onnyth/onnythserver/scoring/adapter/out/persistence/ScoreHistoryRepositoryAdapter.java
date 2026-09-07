package com.onnyth.onnythserver.scoring.adapter.out.persistence;

import com.onnyth.onnythserver.models.StatDomain;
import com.onnyth.onnythserver.scoring.application.port.ScoreHistoryRepository;
import com.onnyth.onnythserver.scoring.domain.model.ScoreHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ScoreHistoryRepositoryAdapter implements ScoreHistoryRepository {

    private final ScoreHistoryJpaRepository scoreHistoryJpaRepository;

    @Override
    public ScoreHistory save(ScoreHistory scoreHistory) {
        ScoreHistoryEntity saved = scoreHistoryJpaRepository.save(ScoreHistoryPersistenceMapper.toEntity(scoreHistory));
        return ScoreHistoryPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<ScoreHistory> findAllByUserIdOrderByChangedAtDesc(UUID userId) {
        return scoreHistoryJpaRepository.findAllByUserIdOrderByChangedAtDesc(userId).stream()
                .map(ScoreHistoryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<ScoreHistory> findAllByUserIdAndDomainOrderByChangedAtDesc(UUID userId, StatDomain domain) {
        return scoreHistoryJpaRepository.findAllByUserIdAndDomainOrderByChangedAtDesc(userId, domain).stream()
                .map(ScoreHistoryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        scoreHistoryJpaRepository.deleteAllByUserId(userId);
    }
}
