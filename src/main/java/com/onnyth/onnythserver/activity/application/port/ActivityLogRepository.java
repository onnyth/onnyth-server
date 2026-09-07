package com.onnyth.onnythserver.activity.application.port;

import com.onnyth.onnythserver.activity.domain.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityLogRepository {

    ActivityLog save(ActivityLog activityLog);

    Page<ActivityLog> findAllByUserIdOrderByLoggedAtDesc(UUID userId, Pageable pageable);

    List<ActivityLog> findAllByUserIdAndLoggedAtBetween(UUID userId, Instant start, Instant end);

    Optional<ActivityLog> findFirstByUserIdAndActivityTypeIdAndLoggedAtAfterOrderByLoggedAtDesc(
            UUID userId, UUID activityTypeId, Instant after);

    void deleteAllByUserId(UUID userId);
}
