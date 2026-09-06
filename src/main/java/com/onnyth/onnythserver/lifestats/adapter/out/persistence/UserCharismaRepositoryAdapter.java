package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.application.port.UserCharismaRepository;
import com.onnyth.onnythserver.lifestats.domain.model.UserCharisma;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserCharismaRepositoryAdapter implements UserCharismaRepository {

    private final UserCharismaJpaRepository userCharismaJpaRepository;

    @Override
    public UserCharisma save(UserCharisma charisma) {
        UserCharismaEntity saved = userCharismaJpaRepository.save(UserCharismaPersistenceMapper.toEntity(charisma));
        return UserCharismaPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<UserCharisma> findById(UUID id) {
        return userCharismaJpaRepository.findById(id).map(UserCharismaPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserCharisma> findByUserId(UUID userId) {
        return userCharismaJpaRepository.findByUserId(userId).map(UserCharismaPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return userCharismaJpaRepository.existsByUserId(userId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        userCharismaJpaRepository.deleteByUserId(userId);
    }
}
