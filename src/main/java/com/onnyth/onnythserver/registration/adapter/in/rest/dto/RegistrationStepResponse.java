package com.onnyth.onnythserver.registration.adapter.in.rest.dto;

import java.util.List;

/**
 * Response after saving a single registration step.
 */
public record RegistrationStepResponse(
        String currentStep,
        List<String> completedSteps,
        int version
) {}
