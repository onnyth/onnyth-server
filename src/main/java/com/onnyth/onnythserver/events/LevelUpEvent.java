package com.onnyth.onnythserver.events;

import java.util.UUID;

/**
 * Published when a user levels up.
 * Can trigger feed events and achievements.
 */
public record LevelUpEvent(UUID userId, int oldLevel, int newLevel, String newTitle) {
}
