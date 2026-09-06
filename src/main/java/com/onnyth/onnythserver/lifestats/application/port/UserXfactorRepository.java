package com.onnyth.onnythserver.lifestats.application.port;

import com.onnyth.onnythserver.lifestats.domain.model.UserXfactor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserXfactorRepository {

    UserXfactor save(UserXfactor xfactor);

    Optional<UserXfactor> findById(UUID id);

    List<UserXfactor> findAllByUserId(UUID userId);

    long countByUserId(UUID userId);
}
