package com.onnyth.onnythserver.activity.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityLogJpaRepository extends JpaRepository<ActivityLogEntity, UUID> {

    Page<ActivityLogEntity> findAllByUserIdOrderByLoggedAtDesc(UUID userId, Pageable pageable);

    List<ActivityLogEntity> findAllByUserIdAndLoggedAtBetween(UUID userId, Instant start, Instant end);

    Optional<ActivityLogEntity> findFirstByUserIdAndActivityTypeIdAndLoggedAtAfterOrderByLoggedAtDesc(
            UUID userId, UUID activityTypeId, Instant after);

    void deleteAllByUserId(UUID userId);
}
