package com.onnyth.onnythserver.achievement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Master achievement definition. Seeded via migration, not user-created.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private AchievementCategory category;
    private String requirementType;
    private int threshold;
    private int points;

    @Builder.Default
    private boolean isActive = true;
}
