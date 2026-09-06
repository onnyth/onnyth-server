package com.onnyth.onnythserver.lifestats.application.port;

import com.onnyth.onnythserver.lifestats.domain.model.UserPhysique;

import java.util.Optional;
import java.util.UUID;

public interface UserPhysiqueRepository {

    UserPhysique save(UserPhysique physique);

    Optional<UserPhysique> findById(UUID id);

    Optional<UserPhysique> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
