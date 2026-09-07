package com.onnyth.onnythserver.quest.application.port;

import com.onnyth.onnythserver.quest.domain.model.QuestCompletion;

import java.util.List;
import java.util.UUID;

public interface QuestCompletionRepository {

    QuestCompletion save(QuestCompletion questCompletion);

    List<QuestCompletion> findAllByUserId(UUID userId);

    boolean existsByUserIdAndQuestId(UUID userId, UUID questId);

    void deleteAllByUserId(UUID userId);
}
