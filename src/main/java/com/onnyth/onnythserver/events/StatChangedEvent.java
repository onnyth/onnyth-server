package com.onnyth.onnythserver.events;

import java.util.UUID;

/**
 * Spring Application Event published when a user's life stat changes.
 * Used to trigger score recalculation.
 */
public record StatChangedEvent(UUID userId) {
}
