package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.Quest;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record QuestResponse(
        UUID id,
        String title,
        String description,
        int xpReward,
        String category,
        String status,
        Instant deadline,
        boolean completed) {

    public static QuestResponse fromQuest(Quest quest, boolean completed) {
        return QuestResponse.builder()
                .id(quest.getId())
                .title(quest.getTitle())
                .description(quest.getDescription())
                .xpReward(quest.getXpReward())
                .category(quest.getCategory().name())
                .status(quest.getStatus().name())
                .deadline(quest.getDeadline())
                .completed(completed)
                .build();
    }
}
