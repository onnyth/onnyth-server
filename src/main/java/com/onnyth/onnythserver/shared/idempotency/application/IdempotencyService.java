package com.onnyth.onnythserver.shared.idempotency.application;

import java.util.Optional;

public interface IdempotencyService {
    Optional<IdempotencyResponse> get(String key);
    void save(String key, IdempotencyResponse response);
}
