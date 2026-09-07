package com.onnyth.onnythserver.scoring.adapter.out.persistence;

import com.onnyth.onnythserver.scoring.domain.model.SportMedal;

public final class SportMedalPersistenceMapper {

    private SportMedalPersistenceMapper() {
    }

    public static SportMedal toDomain(SportMedalEntity entity) {
        if (entity == null) {
            return null;
        }
        return SportMedal.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .sport(entity.getSport())
                .medalType(entity.getMedalType())
                .eventName(entity.getEventName())
                .year(entity.getYear())
                .evidenceUrl(entity.getEvidenceUrl())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static SportMedalEntity toEntity(SportMedal domain) {
        if (domain == null) {
            return null;
        }
        return SportMedalEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .sport(domain.getSport())
                .medalType(domain.getMedalType())
                .eventName(domain.getEventName())
                .year(domain.getYear())
                .evidenceUrl(domain.getEvidenceUrl())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
