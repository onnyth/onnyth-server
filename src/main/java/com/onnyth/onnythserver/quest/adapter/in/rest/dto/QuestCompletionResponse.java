package com.onnyth.onnythserver.quest.adapter.in.rest.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record QuestCompletionResponse(
        UUID questId,
        String questTitle,
        int xpAwarded,
        long newTotalScore,
        String rankTier) {
}
