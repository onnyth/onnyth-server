package com.onnyth.onnythserver.activity.adapter.out.persistence;

import com.onnyth.onnythserver.activity.application.port.ActivityLogRepository;
import com.onnyth.onnythserver.activity.domain.model.ActivityLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ActivityLogRepositoryAdapter implements ActivityLogRepository {

    private final ActivityLogJpaRepository activityLogJpaRepository;

    @Override
    public ActivityLog save(ActivityLog activityLog) {
        ActivityLogEntity saved = activityLogJpaRepository.save(ActivityLogPersistenceMapper.toEntity(activityLog));
        return ActivityLogPersistenceMapper.toDomain(saved);
    }

    @Override
    public Page<ActivityLog> findAllByUserIdOrderByLoggedAtDesc(UUID userId, Pageable pageable) {
        return activityLogJpaRepository.findAllByUserIdOrderByLoggedAtDesc(userId, pageable)
                .map(ActivityLogPersistenceMapper::toDomain);
    }

    @Override
    public List<ActivityLog> findAllByUserIdAndLoggedAtBetween(UUID userId, Instant start, Instant end) {
        return activityLogJpaRepository.findAllByUserIdAndLoggedAtBetween(userId, start, end).stream()
                .map(ActivityLogPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ActivityLog> findFirstByUserIdAndActivityTypeIdAndLoggedAtAfterOrderByLoggedAtDesc(
            UUID userId, UUID activityTypeId, Instant after) {
        return activityLogJpaRepository.findFirstByUserIdAndActivityTypeIdAndLoggedAtAfterOrderByLoggedAtDesc(
                userId, activityTypeId, after
        ).map(ActivityLogPersistenceMapper::toDomain);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        activityLogJpaRepository.deleteAllByUserId(userId);
    }
}
