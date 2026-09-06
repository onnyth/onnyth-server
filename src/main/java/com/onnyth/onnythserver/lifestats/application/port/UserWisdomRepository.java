package com.onnyth.onnythserver.lifestats.application.port;

import com.onnyth.onnythserver.lifestats.domain.model.UserWisdom;

import java.util.Optional;
import java.util.UUID;

public interface UserWisdomRepository {

    UserWisdom save(UserWisdom wisdom);

    Optional<UserWisdom> findById(UUID id);

    Optional<UserWisdom> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
