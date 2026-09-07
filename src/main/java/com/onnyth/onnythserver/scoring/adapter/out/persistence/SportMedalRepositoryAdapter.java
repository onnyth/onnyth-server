package com.onnyth.onnythserver.scoring.adapter.out.persistence;

import com.onnyth.onnythserver.scoring.application.port.SportMedalRepository;
import com.onnyth.onnythserver.scoring.domain.model.SportMedal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SportMedalRepositoryAdapter implements SportMedalRepository {

    private final SportMedalJpaRepository sportMedalJpaRepository;

    @Override
    public SportMedal save(SportMedal sportMedal) {
        SportMedalEntity saved = sportMedalJpaRepository.save(SportMedalPersistenceMapper.toEntity(sportMedal));
        return SportMedalPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<SportMedal> findAllByUserId(UUID userId) {
        return sportMedalJpaRepository.findAllByUserId(userId).stream()
                .map(SportMedalPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(UUID userId) {
        return sportMedalJpaRepository.countByUserId(userId);
    }
}
