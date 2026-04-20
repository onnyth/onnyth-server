package com.onnyth.onnythserver.dto.registration;

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
