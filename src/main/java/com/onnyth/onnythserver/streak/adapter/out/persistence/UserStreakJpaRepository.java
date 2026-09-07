package com.onnyth.onnythserver.streak.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserStreakJpaRepository extends JpaRepository<UserStreakEntity, UUID> {

    Optional<UserStreakEntity> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
