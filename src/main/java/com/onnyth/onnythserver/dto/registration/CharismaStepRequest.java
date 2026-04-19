package com.onnyth.onnythserver.dto.registration;

/**
 * Request body for the CHARISMA registration step.
 */
public record CharismaStepRequest(
        String relationshipStatus,
        Integer socialCircleSize
) {}
