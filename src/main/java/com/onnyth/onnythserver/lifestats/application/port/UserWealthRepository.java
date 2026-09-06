package com.onnyth.onnythserver.lifestats.application.port;

import com.onnyth.onnythserver.lifestats.domain.model.UserWealth;

import java.util.Optional;
import java.util.UUID;

public interface UserWealthRepository {

    UserWealth save(UserWealth wealth);

    Optional<UserWealth> findById(UUID id);

    Optional<UserWealth> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
