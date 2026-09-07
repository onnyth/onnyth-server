package com.onnyth.onnythserver.store.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCosmeticJpaRepository extends JpaRepository<UserCosmeticEntity, UUID> {

    List<UserCosmeticEntity> findAllByUserId(UUID userId);

    Optional<UserCosmeticEntity> findByUserIdAndCosmeticItemId(UUID userId, UUID cosmeticItemId);

    boolean existsByUserIdAndCosmeticItemId(UUID userId, UUID cosmeticItemId);

    void deleteAllByUserId(UUID userId);
}
