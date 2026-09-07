package com.onnyth.onnythserver.registration.adapter.in.rest.dto;

import java.util.List;
import java.util.UUID;

/**
 * Response from POST /registration/complete.
 */
public record RegistrationCompleteResponse(
        boolean profileComplete,
        UUID userId,
        String username
) {}
