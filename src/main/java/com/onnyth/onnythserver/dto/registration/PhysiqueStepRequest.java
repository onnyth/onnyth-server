package com.onnyth.onnythserver.dto.registration;

import java.math.BigDecimal;

/**
 * Request body for the PHYSIQUE registration step.
 */
public record PhysiqueStepRequest(
        BigDecimal heightCm,
        BigDecimal weightKg,
        String fitnessLevel
) {}
