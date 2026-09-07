package com.onnyth.onnythserver.activity.adapter.out.persistence;

import com.onnyth.onnythserver.activity.domain.model.ActivityFrequency;
import com.onnyth.onnythserver.models.StatDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "activity_types")
public class ActivityTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon", length = 50)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private StatDomain category;

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    @Builder.Default
    private ActivityFrequency frequency = ActivityFrequency.DAILY;

    @Column(name = "cooldown_hours", nullable = false)
    @Builder.Default
    private Integer cooldownHours = 24;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
