package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.application.port.UserEducationRepository;
import com.onnyth.onnythserver.lifestats.domain.model.UserEducation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserEducationRepositoryAdapter implements UserEducationRepository {

    private final UserEducationJpaRepository userEducationJpaRepository;

    @Override
    public UserEducation save(UserEducation education) {
        UserEducationEntity saved = userEducationJpaRepository.save(UserEducationPersistenceMapper.toEntity(education));
        return UserEducationPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<UserEducation> findById(UUID id) {
        return userEducationJpaRepository.findById(id).map(UserEducationPersistenceMapper::toDomain);
    }

    @Override
    public List<UserEducation> findAllByUserId(UUID userId) {
        return userEducationJpaRepository.findAllByUserId(userId).stream()
                .map(UserEducationPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserEducation> findByUserIdAndIsHighestTrue(UUID userId) {
        return userEducationJpaRepository.findByUserIdAndIsHighestTrue(userId).map(UserEducationPersistenceMapper::toDomain);
    }
}
