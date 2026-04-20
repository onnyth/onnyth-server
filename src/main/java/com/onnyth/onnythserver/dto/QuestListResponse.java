package com.onnyth.onnythserver.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record QuestListResponse(
        List<QuestResponse> quests,
        int completedCount,
        int totalCount) {
}
