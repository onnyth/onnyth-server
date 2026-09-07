package com.onnyth.onnythserver.registration.adapter.in.rest.dto;

/**
 * Request body for the WEALTH registration step.
 */
public record WealthStepRequest(
        String incomeBracket,
        Integer monthlySavingPct,
        String incomeCurrency
) {}
