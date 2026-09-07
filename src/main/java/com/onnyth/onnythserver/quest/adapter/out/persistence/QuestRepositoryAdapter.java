package com.onnyth.onnythserver.quest.adapter.out.persistence;

import com.onnyth.onnythserver.quest.application.port.QuestRepository;
import com.onnyth.onnythserver.quest.domain.model.Quest;
import com.onnyth.onnythserver.quest.domain.model.QuestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class QuestRepositoryAdapter implements QuestRepository {

    private final QuestJpaRepository questJpaRepository;

    @Override
    public Quest save(Quest quest) {
        QuestEntity saved = questJpaRepository.save(QuestPersistenceMapper.toEntity(quest));
        return QuestPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Quest> findById(UUID id) {
        return questJpaRepository.findById(id).map(QuestPersistenceMapper::toDomain);
    }

    @Override
    public List<Quest> findAllByStatus(QuestStatus status) {
        return questJpaRepository.findAllByStatus(status).stream()
                .map(QuestPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Quest> findByIdAndStatus(UUID id, QuestStatus status) {
        return questJpaRepository.findByIdAndStatus(id, status).map(QuestPersistenceMapper::toDomain);
    }
}
