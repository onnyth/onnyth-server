package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.StatCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for submitting a single life stat.
 */
public record StatInputRequest(
        @NotNull(message = "Category is required") StatCategory category,

        @NotNull(message = "Value is required") @Min(value = 1, message = "Value must be at least 1") @Max(value = 100, message = "Value must be at most 100") Integer value,

        String metadata) {
}
