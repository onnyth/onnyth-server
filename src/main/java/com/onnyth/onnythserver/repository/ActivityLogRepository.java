package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    Page<ActivityLog> findAllByUserIdOrderByLoggedAtDesc(UUID userId, Pageable pageable);

    List<ActivityLog> findAllByUserIdAndLoggedAtBetween(UUID userId, Instant start, Instant end);

    Optional<ActivityLog> findFirstByUserIdAndActivityTypeIdAndLoggedAtAfterOrderByLoggedAtDesc(
            UUID userId, UUID activityTypeId, Instant after);
}
