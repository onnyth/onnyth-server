package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.application.port.UserWisdomRepository;
import com.onnyth.onnythserver.lifestats.domain.model.UserWisdom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserWisdomRepositoryAdapter implements UserWisdomRepository {

    private final UserWisdomJpaRepository userWisdomJpaRepository;

    @Override
    public UserWisdom save(UserWisdom wisdom) {
        UserWisdomEntity saved = userWisdomJpaRepository.save(UserWisdomPersistenceMapper.toEntity(wisdom));
        return UserWisdomPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<UserWisdom> findById(UUID id) {
        return userWisdomJpaRepository.findById(id).map(UserWisdomPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserWisdom> findByUserId(UUID userId) {
        return userWisdomJpaRepository.findByUserId(userId).map(UserWisdomPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return userWisdomJpaRepository.existsByUserId(userId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        userWisdomJpaRepository.deleteByUserId(userId);
    }
}
