package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.QuestCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestCompletionRepository extends JpaRepository<QuestCompletion, UUID> {

    List<QuestCompletion> findAllByUserId(UUID userId);

    boolean existsByUserIdAndQuestId(UUID userId, UUID questId);
}
