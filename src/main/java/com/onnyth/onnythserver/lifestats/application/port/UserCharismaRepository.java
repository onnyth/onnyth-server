package com.onnyth.onnythserver.lifestats.application.port;

import com.onnyth.onnythserver.lifestats.domain.model.UserCharisma;

import java.util.Optional;
import java.util.UUID;

public interface UserCharismaRepository {

    UserCharisma save(UserCharisma charisma);

    Optional<UserCharisma> findById(UUID id);

    Optional<UserCharisma> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
