package com.onnyth.onnythserver.activity.adapter.out.persistence;

import com.onnyth.onnythserver.activity.application.port.ActivityTypeRepository;
import com.onnyth.onnythserver.activity.domain.model.ActivityType;
import com.onnyth.onnythserver.models.StatDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ActivityTypeRepositoryAdapter implements ActivityTypeRepository {

    private final ActivityTypeJpaRepository activityTypeJpaRepository;

    @Override
    public List<ActivityType> findAllByIsActiveTrue() {
        return activityTypeJpaRepository.findAllByIsActiveTrue().stream()
                .map(ActivityTypePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<ActivityType> findAllByCategoryAndIsActiveTrue(StatDomain category) {
        return activityTypeJpaRepository.findAllByCategoryAndIsActiveTrue(category).stream()
                .map(ActivityTypePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ActivityType> findByIdAndIsActiveTrue(UUID id) {
        return activityTypeJpaRepository.findByIdAndIsActiveTrue(id)
                .map(ActivityTypePersistenceMapper::toDomain);
    }

    @Override
    public Optional<ActivityType> findById(UUID id) {
        return activityTypeJpaRepository.findById(id)
                .map(ActivityTypePersistenceMapper::toDomain);
    }
}
