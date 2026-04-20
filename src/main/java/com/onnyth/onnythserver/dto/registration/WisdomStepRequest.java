package com.onnyth.onnythserver.dto.registration;

import java.util.List;

/**
 * Request body for the WISDOM registration step.
 */
public record WisdomStepRequest(
        String formalEducation,
        List<String> languages,
        String readingHabits
) {}
