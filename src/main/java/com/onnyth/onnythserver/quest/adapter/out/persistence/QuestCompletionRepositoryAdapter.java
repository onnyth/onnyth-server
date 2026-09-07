package com.onnyth.onnythserver.quest.adapter.out.persistence;

import com.onnyth.onnythserver.quest.application.port.QuestCompletionRepository;
import com.onnyth.onnythserver.quest.domain.model.QuestCompletion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class QuestCompletionRepositoryAdapter implements QuestCompletionRepository {

    private final QuestCompletionJpaRepository questCompletionJpaRepository;

    @Override
    public QuestCompletion save(QuestCompletion questCompletion) {
        QuestCompletionEntity saved = questCompletionJpaRepository.save(QuestCompletionPersistenceMapper.toEntity(questCompletion));
        return QuestCompletionPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<QuestCompletion> findAllByUserId(UUID userId) {
        return questCompletionJpaRepository.findAllByUserId(userId).stream()
                .map(QuestCompletionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUserIdAndQuestId(UUID userId, UUID questId) {
        return questCompletionJpaRepository.existsByUserIdAndQuestId(userId, questId);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        questCompletionJpaRepository.deleteAllByUserId(userId);
    }
}
