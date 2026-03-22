package com.onnyth.onnythserver.dto;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record DisplayedBadgeRequest(
        @Size(max = 3, message = "You can display a maximum of 3 badges") List<UUID> achievementIds) {
}
