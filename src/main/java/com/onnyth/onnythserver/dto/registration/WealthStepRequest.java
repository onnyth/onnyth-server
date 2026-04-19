package com.onnyth.onnythserver.dto.registration;

/**
 * Request body for the WEALTH registration step.
 */
public record WealthStepRequest(
        String incomeBracket,
        Integer monthlySavingPct,
        String incomeCurrency
) {}
