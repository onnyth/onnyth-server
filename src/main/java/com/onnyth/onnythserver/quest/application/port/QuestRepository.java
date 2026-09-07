package com.onnyth.onnythserver.quest.application.port;

import com.onnyth.onnythserver.quest.domain.model.Quest;
import com.onnyth.onnythserver.quest.domain.model.QuestStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestRepository {

    Quest save(Quest quest);

    Optional<Quest> findById(UUID id);

    List<Quest> findAllByStatus(QuestStatus status);

    Optional<Quest> findByIdAndStatus(UUID id, QuestStatus status);
}
