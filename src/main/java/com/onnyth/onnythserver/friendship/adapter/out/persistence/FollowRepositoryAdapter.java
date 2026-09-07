package com.onnyth.onnythserver.friendship.adapter.out.persistence;

import com.onnyth.onnythserver.friendship.application.port.FollowRepository;
import com.onnyth.onnythserver.friendship.domain.model.FollowId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryAdapter implements FollowRepository {

    private final FollowJpaRepository followJpaRepository;

    @Override
    public long countByFollowingId(UUID userId) {
        return followJpaRepository.countByFollowingId(userId);
    }

    @Override
    public long countByFollowerId(UUID userId) {
        return followJpaRepository.countByFollowerId(userId);
    }

    @Override
    public boolean existsById(FollowId id) {
        return followJpaRepository.existsById(FollowPersistenceMapper.toEntityId(id));
    }
}
