package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserOccupationJpaRepository extends JpaRepository<UserOccupationEntity, UUID> {

    Optional<UserOccupationEntity> findByUserIdAndIsCurrentTrue(UUID userId);

    List<UserOccupationEntity> findAllByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);
}
