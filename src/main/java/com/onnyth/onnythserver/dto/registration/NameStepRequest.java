package com.onnyth.onnythserver.dto.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for the NAME registration step.
 */
public record NameStepRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        String username,

        @NotBlank(message = "Display name is required")
        @Size(max = 100, message = "Display name cannot exceed 100 characters")
        String displayName,

        String profileType
) {}
