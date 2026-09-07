package com.onnyth.onnythserver.scoring.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SportMedalJpaRepository extends JpaRepository<SportMedalEntity, UUID> {

    List<SportMedalEntity> findAllByUserId(UUID userId);

    long countByUserId(UUID userId);
}
