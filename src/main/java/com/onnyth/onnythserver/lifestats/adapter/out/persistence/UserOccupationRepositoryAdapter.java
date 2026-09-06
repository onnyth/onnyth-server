package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.application.port.UserOccupationRepository;
import com.onnyth.onnythserver.lifestats.domain.model.UserOccupation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserOccupationRepositoryAdapter implements UserOccupationRepository {

    private final UserOccupationJpaRepository userOccupationJpaRepository;

    @Override
    public UserOccupation save(UserOccupation occupation) {
        UserOccupationEntity saved = userOccupationJpaRepository.save(UserOccupationPersistenceMapper.toEntity(occupation));
        return UserOccupationPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<UserOccupation> findById(UUID id) {
        return userOccupationJpaRepository.findById(id).map(UserOccupationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserOccupation> findByUserIdAndIsCurrentTrue(UUID userId) {
        return userOccupationJpaRepository.findByUserIdAndIsCurrentTrue(userId).map(UserOccupationPersistenceMapper::toDomain);
    }

    @Override
    public List<UserOccupation> findAllByUserId(UUID userId) {
        return userOccupationJpaRepository.findAllByUserId(userId).stream()
                .map(UserOccupationPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return userOccupationJpaRepository.existsByUserId(userId);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        userOccupationJpaRepository.deleteAllByUserId(userId);
    }
}
