package com.onnyth.onnythserver.events;

import java.util.UUID;

/**
 * Published when XP is awarded to a user.
 * Triggers level check.
 */
public record XpAwardedEvent(UUID userId, int xpAmount, long newTotalXp) {
}
