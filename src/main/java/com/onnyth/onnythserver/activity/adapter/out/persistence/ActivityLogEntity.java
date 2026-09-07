package com.onnyth.onnythserver.activity.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "activity_log")
public class ActivityLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "activity_type_id", nullable = false)
    private UUID activityTypeId;

    @Column(name = "xp_earned", nullable = false)
    private Integer xpEarned;

    @Column(name = "logged_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant loggedAt = Instant.now();
}
