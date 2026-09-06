package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.application.port.UserPhysiqueRepository;
import com.onnyth.onnythserver.lifestats.domain.model.UserPhysique;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserPhysiqueRepositoryAdapter implements UserPhysiqueRepository {

    private final UserPhysiqueJpaRepository userPhysiqueJpaRepository;

    @Override
    public UserPhysique save(UserPhysique physique) {
        UserPhysiqueEntity saved = userPhysiqueJpaRepository.save(UserPhysiquePersistenceMapper.toEntity(physique));
        return UserPhysiquePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<UserPhysique> findById(UUID id) {
        return userPhysiqueJpaRepository.findById(id).map(UserPhysiquePersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserPhysique> findByUserId(UUID userId) {
        return userPhysiqueJpaRepository.findByUserId(userId).map(UserPhysiquePersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return userPhysiqueJpaRepository.existsByUserId(userId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        userPhysiqueJpaRepository.deleteByUserId(userId);
    }
}
