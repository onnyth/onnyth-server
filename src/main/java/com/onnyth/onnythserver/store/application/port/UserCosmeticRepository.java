package com.onnyth.onnythserver.store.application.port;

import com.onnyth.onnythserver.store.domain.model.UserCosmetic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCosmeticRepository {

    UserCosmetic save(UserCosmetic userCosmetic);

    List<UserCosmetic> findAllByUserId(UUID userId);

    Optional<UserCosmetic> findByUserIdAndCosmeticItemId(UUID userId, UUID cosmeticItemId);

    boolean existsByUserIdAndCosmeticItemId(UUID userId, UUID cosmeticItemId);

    void deleteAllByUserId(UUID userId);
}
