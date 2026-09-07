package com.onnyth.onnythserver.quest.domain.model;

/**
 * Status of a quest.
 * ACTIVE — currently available for completion
 * EXPIRED — past its deadline
 * ARCHIVED — administratively hidden
 */
public enum QuestStatus {
    ACTIVE,
    EXPIRED,
    ARCHIVED
}
