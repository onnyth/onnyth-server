package com.onnyth.onnythserver.shared.idempotency.application;

public record IdempotencyResponse(
        String requestHash,
        String requestBody
) {
    public static IdempotencyResponse of(String requestHash, String requestBody) {
        return new IdempotencyResponse(requestHash, requestBody);
    }
}
