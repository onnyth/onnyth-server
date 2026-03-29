package com.onnyth.onnythserver.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LogActivityRequest(
        @NotNull(message = "Activity type ID is required")
        UUID activityTypeId
) {
}
