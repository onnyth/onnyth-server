package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.UserCosmetic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCosmeticRepository extends JpaRepository<UserCosmetic, UUID> {

    List<UserCosmetic> findAllByUserId(UUID userId);

    Optional<UserCosmetic> findByUserIdAndCosmeticItemId(UUID userId, UUID cosmeticItemId);

    boolean existsByUserIdAndCosmeticItemId(UUID userId, UUID cosmeticItemId);
}
