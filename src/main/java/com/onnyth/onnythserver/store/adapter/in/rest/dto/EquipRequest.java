package com.onnyth.onnythserver.store.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EquipRequest(
        @NotNull(message = "Item ID is required")
        UUID itemId
) {
}
