package com.onnyth.onnythserver.streak.adapter.out.persistence;

import com.onnyth.onnythserver.streak.application.port.UserStreakRepository;
import com.onnyth.onnythserver.streak.domain.model.UserStreak;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserStreakRepositoryAdapter implements UserStreakRepository {

    private final UserStreakJpaRepository userStreakJpaRepository;

    @Override
    public UserStreak save(UserStreak userStreak) {
        UserStreakEntity saved = userStreakJpaRepository.save(UserStreakPersistenceMapper.toEntity(userStreak));
        return UserStreakPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<UserStreak> findByUserId(UUID userId) {
        return userStreakJpaRepository.findByUserId(userId).map(UserStreakPersistenceMapper::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        userStreakJpaRepository.deleteByUserId(userId);
    }
}
