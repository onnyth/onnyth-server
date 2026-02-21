package com.onnyth.onnythserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating an existing life stat.
 */
public record StatUpdateRequest(
        @NotNull(message = "New value is required") @Min(value = 1, message = "Value must be at least 1") @Max(value = 100, message = "Value must be at most 100") Integer newValue,

        String reason) {
}
