package com.onnyth.onnythserver.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for submitting multiple life stats at once (e.g., during
 * onboarding).
 */
public record BulkStatInputRequest(
        @NotEmpty(message = "Stats list must not be empty") @Valid List<StatInputRequest> stats) {
}
