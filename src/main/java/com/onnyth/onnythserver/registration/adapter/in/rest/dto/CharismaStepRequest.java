package com.onnyth.onnythserver.registration.adapter.in.rest.dto;

/**
 * Request body for the CHARISMA registration step.
 */
public record CharismaStepRequest(
        String relationshipStatus,
        Integer socialCircleSize
) {}
