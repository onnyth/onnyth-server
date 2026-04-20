package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.Quest;
import com.onnyth.onnythserver.models.QuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestRepository extends JpaRepository<Quest, UUID> {

    List<Quest> findAllByStatus(QuestStatus status);

    Optional<Quest> findByIdAndStatus(UUID id, QuestStatus status);
}
