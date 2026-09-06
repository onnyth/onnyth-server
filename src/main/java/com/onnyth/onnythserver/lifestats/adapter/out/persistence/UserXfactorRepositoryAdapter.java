package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.application.port.UserXfactorRepository;
import com.onnyth.onnythserver.lifestats.domain.model.UserXfactor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserXfactorRepositoryAdapter implements UserXfactorRepository {

    private final UserXfactorJpaRepository userXfactorJpaRepository;

    @Override
    public UserXfactor save(UserXfactor xfactor) {
        UserXfactorEntity saved = userXfactorJpaRepository.save(UserXfactorPersistenceMapper.toEntity(xfactor));
        return UserXfactorPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<UserXfactor> findById(UUID id) {
        return userXfactorJpaRepository.findById(id).map(UserXfactorPersistenceMapper::toDomain);
    }

    @Override
    public List<UserXfactor> findAllByUserId(UUID userId) {
        return userXfactorJpaRepository.findAllByUserId(userId).stream()
                .map(UserXfactorPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByUserId(UUID userId) {
        return userXfactorJpaRepository.countByUserId(userId);
    }
}
