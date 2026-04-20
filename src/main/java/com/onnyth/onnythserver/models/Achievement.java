package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Master achievement definition. Seeded via migration, not user-created.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "icon", length = 50)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private AchievementCategory category;

    @Column(name = "requirement_type", nullable = false, length = 50)
    private String requirementType;

    @Column(name = "threshold", nullable = false)
    private int threshold;

    @Column(name = "points", nullable = false)
    private int points;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
