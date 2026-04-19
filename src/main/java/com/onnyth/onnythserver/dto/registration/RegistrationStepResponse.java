package com.onnyth.onnythserver.dto.registration;

import java.util.List;

/**
 * Response after saving a single registration step.
 */
public record RegistrationStepResponse(
        String currentStep,
        List<String> completedSteps,
        int version
) {}
