package com.onnyth.onnythserver.quest.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestCompletionJpaRepository extends JpaRepository<QuestCompletionEntity, UUID> {

    List<QuestCompletionEntity> findAllByUserId(UUID userId);

    boolean existsByUserIdAndQuestId(UUID userId, UUID questId);

    void deleteAllByUserId(UUID userId);
}
