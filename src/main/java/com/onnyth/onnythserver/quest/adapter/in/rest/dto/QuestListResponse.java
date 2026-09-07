package com.onnyth.onnythserver.quest.adapter.in.rest.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record QuestListResponse(
        List<QuestResponse> quests,
        int completedCount,
        int totalCount) {
}
