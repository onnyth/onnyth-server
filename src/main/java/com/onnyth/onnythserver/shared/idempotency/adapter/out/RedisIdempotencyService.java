package com.onnyth.onnythserver.shared.idempotency.adapter.out;

import com.onnyth.onnythserver.shared.idempotency.application.IdempotencyResponse;
import com.onnyth.onnythserver.shared.idempotency.application.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisIdempotencyService implements IdempotencyService {

    private static final String KEY_PREFIX = "idempotency:";

    private final RedisTemplate<String, IdempotencyResponse> redisTemplate;

    @Override
    public Optional<IdempotencyResponse> get(String key) {
        IdempotencyResponse response = redisTemplate.opsForValue().get(namespacedKey(key));
        return Optional.ofNullable(response);
    }

    @Override
    public void save(String key, IdempotencyResponse response) {
        redisTemplate.opsForValue().set(namespacedKey(key), response, Duration.ofDays(1));
    }

    private String namespacedKey(String key) {
        return KEY_PREFIX + key;
    }
}
