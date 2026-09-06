package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "user_charisma", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class UserCharismaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "onnyth_profile_likes", nullable = false)
    @Builder.Default
    private Integer onnythProfileLikes = 0;

    @Column(name = "score", nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "last_social_sync_at")
    private Instant lastSocialSyncAt;

    @Column(name = "relationship_status", length = 20)
    private String relationshipStatus;

    @Column(name = "social_circle_size")
    private Integer socialCircleSize;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
