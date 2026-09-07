package com.onnyth.onnythserver.activity.application.port;

import com.onnyth.onnythserver.activity.domain.model.ActivityType;
import com.onnyth.onnythserver.shared.domain.model.StatDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityTypeRepository {

    List<ActivityType> findAllByIsActiveTrue();

    List<ActivityType> findAllByCategoryAndIsActiveTrue(StatDomain category);

    Optional<ActivityType> findByIdAndIsActiveTrue(UUID id);

    Optional<ActivityType> findById(UUID id);
}
