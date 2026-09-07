package com.onnyth.onnythserver.registration.adapter.in.rest.dto;

import java.util.List;

/**
 * Request body for the OCCUPATION registration step.
 */
public record OccupationStepRequest(
        String jobTitle,
        String companyName,
        List<String> skills
) {}
