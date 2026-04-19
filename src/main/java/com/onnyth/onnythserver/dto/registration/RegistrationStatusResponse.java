package com.onnyth.onnythserver.dto.registration;

import com.onnyth.onnythserver.models.RegistrationStep;

import java.util.List;
import java.util.Map;

/**
 * Response for GET /registration/status — tells the client where to resume.
 */
public record RegistrationStatusResponse(
        String currentStep,
        List<String> completedSteps,
        Map<String, Object> draftData,
        int version
) {
    public static RegistrationStatusResponse empty() {
        return new RegistrationStatusResponse(
                RegistrationStep.PHONE.name(),
                List.of(),
                Map.of(),
                0
        );
    }
}
