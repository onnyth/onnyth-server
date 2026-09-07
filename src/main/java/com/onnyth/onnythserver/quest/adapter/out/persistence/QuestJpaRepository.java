package com.onnyth.onnythserver.quest.adapter.out.persistence;

import com.onnyth.onnythserver.quest.domain.model.QuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestJpaRepository extends JpaRepository<QuestEntity, UUID> {

    List<QuestEntity> findAllByStatus(QuestStatus status);

    Optional<QuestEntity> findByIdAndStatus(UUID id, QuestStatus status);
}
