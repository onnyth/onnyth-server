package com.onnyth.onnythserver.store.adapter.out.persistence;

import com.onnyth.onnythserver.store.application.port.UserCosmeticRepository;
import com.onnyth.onnythserver.store.domain.model.UserCosmetic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserCosmeticRepositoryAdapter implements UserCosmeticRepository {

    private final UserCosmeticJpaRepository userCosmeticJpaRepository;

    @Override
    public UserCosmetic save(UserCosmetic userCosmetic) {
        UserCosmeticEntity saved = userCosmeticJpaRepository.save(UserCosmeticPersistenceMapper.toEntity(userCosmetic));
        return UserCosmeticPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<UserCosmetic> findAllByUserId(UUID userId) {
        return userCosmeticJpaRepository.findAllByUserId(userId).stream()
                .map(UserCosmeticPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserCosmetic> findByUserIdAndCosmeticItemId(UUID userId, UUID cosmeticItemId) {
        return userCosmeticJpaRepository.findByUserIdAndCosmeticItemId(userId, cosmeticItemId)
                .map(UserCosmeticPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByUserIdAndCosmeticItemId(UUID userId, UUID cosmeticItemId) {
        return userCosmeticJpaRepository.existsByUserIdAndCosmeticItemId(userId, cosmeticItemId);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        userCosmeticJpaRepository.deleteAllByUserId(userId);
    }
}
