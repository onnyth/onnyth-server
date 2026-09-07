package com.onnyth.onnythserver.registration.adapter.in.rest.dto;

import java.math.BigDecimal;

/**
 * Request body for the PHYSIQUE registration step.
 */
public record PhysiqueStepRequest(
        BigDecimal heightCm,
        BigDecimal weightKg,
        String fitnessLevel
) {}
