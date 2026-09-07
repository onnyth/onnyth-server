package com.onnyth.onnythserver.registration.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the PHONE registration step.
 */
public record PhoneStepRequest(
        @NotBlank(message = "Phone number is required")
        String phone,

        boolean phoneVerified
) {}
