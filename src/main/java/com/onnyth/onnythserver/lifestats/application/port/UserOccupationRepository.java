package com.onnyth.onnythserver.lifestats.application.port;

import com.onnyth.onnythserver.lifestats.domain.model.UserOccupation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserOccupationRepository {

    UserOccupation save(UserOccupation occupation);

    Optional<UserOccupation> findById(UUID id);

    Optional<UserOccupation> findByUserIdAndIsCurrentTrue(UUID userId);

    List<UserOccupation> findAllByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);
}
