package com.onnyth.onnythserver.lifestats.application.port;

import com.onnyth.onnythserver.lifestats.domain.model.UserEducation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserEducationRepository {

    UserEducation save(UserEducation education);

    Optional<UserEducation> findById(UUID id);

    List<UserEducation> findAllByUserId(UUID userId);

    Optional<UserEducation> findByUserIdAndIsHighestTrue(UUID userId);
}
