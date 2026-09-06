package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.application.port.UserWealthRepository;
import com.onnyth.onnythserver.lifestats.domain.model.UserWealth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserWealthRepositoryAdapter implements UserWealthRepository {

    private final UserWealthJpaRepository userWealthJpaRepository;

    @Override
    public UserWealth save(UserWealth wealth) {
        UserWealthEntity saved = userWealthJpaRepository.save(UserWealthPersistenceMapper.toEntity(wealth));
        return UserWealthPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<UserWealth> findById(UUID id) {
        return userWealthJpaRepository.findById(id).map(UserWealthPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserWealth> findByUserId(UUID userId) {
        return userWealthJpaRepository.findByUserId(userId).map(UserWealthPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return userWealthJpaRepository.existsByUserId(userId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        userWealthJpaRepository.deleteByUserId(userId);
    }
}
