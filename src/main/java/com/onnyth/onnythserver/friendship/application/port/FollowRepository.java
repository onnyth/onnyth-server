package com.onnyth.onnythserver.friendship.application.port;

import com.onnyth.onnythserver.friendship.domain.model.FollowId;

import java.util.UUID;

public interface FollowRepository {

    long countByFollowingId(UUID userId);

    long countByFollowerId(UUID userId);

    boolean existsById(FollowId id);
}
