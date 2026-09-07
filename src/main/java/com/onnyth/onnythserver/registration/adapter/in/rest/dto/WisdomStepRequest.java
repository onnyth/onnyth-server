package com.onnyth.onnythserver.registration.adapter.in.rest.dto;

import java.util.List;

/**
 * Request body for the WISDOM registration step.
 */
public record WisdomStepRequest(
        String formalEducation,
        List<String> languages,
        String readingHabits
) {}
