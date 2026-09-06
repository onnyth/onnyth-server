package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserXfactorJpaRepository extends JpaRepository<UserXfactorEntity, UUID> {

    List<UserXfactorEntity> findAllByUserId(UUID userId);

    long countByUserId(UUID userId);
}
