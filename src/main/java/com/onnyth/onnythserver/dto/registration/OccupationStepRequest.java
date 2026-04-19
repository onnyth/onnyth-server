package com.onnyth.onnythserver.dto.registration;

import java.util.List;

/**
 * Request body for the OCCUPATION registration step.
 */
public record OccupationStepRequest(
        String jobTitle,
        String companyName,
        List<String> skills
) {}
