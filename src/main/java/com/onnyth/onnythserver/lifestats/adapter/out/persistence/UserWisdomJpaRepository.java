package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserWisdomJpaRepository extends JpaRepository<UserWisdomEntity, UUID> {

    Optional<UserWisdomEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
