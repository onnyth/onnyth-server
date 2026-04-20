package com.onnyth.onnythserver.models;

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
