package com.onnyth.onnythserver.activity.adapter.out.persistence;

import com.onnyth.onnythserver.activity.domain.model.ActivityLog;

public final class ActivityLogPersistenceMapper {

    private ActivityLogPersistenceMapper() {
    }

    public static ActivityLog toDomain(ActivityLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return ActivityLog.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .activityTypeId(entity.getActivityTypeId())
                .xpEarned(entity.getXpEarned())
                .loggedAt(entity.getLoggedAt())
                .build();
    }

    public static ActivityLogEntity toEntity(ActivityLog domain) {
        if (domain == null) {
            return null;
        }
        return ActivityLogEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .activityTypeId(domain.getActivityTypeId())
                .xpEarned(domain.getXpEarned())
                .loggedAt(domain.getLoggedAt())
                .build();
    }
}
