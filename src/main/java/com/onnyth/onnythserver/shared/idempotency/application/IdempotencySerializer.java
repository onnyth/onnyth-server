package com.onnyth.onnythserver.shared.idempotency.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdempotencySerializer {

    private final ObjectMapper objectMapper;

    public IdempotencyResponse serialize(
            String requestHash,
            Object responseBody
    ) {
        try {
            String body = objectMapper.writeValueAsString(responseBody);
            return IdempotencyResponse.of(requestHash, body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize response body", e);
        }
    }

    public <T> T deserialize(String body, Class<T> responseType) {
        try {
            return objectMapper.readValue(body, responseType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize response body", e);
        }
    }
}
