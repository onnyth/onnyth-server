package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "profile_likes")
public class ProfileLikeEntity {

    @EmbeddedId
    private ProfileLikeEntityId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    private Instant createdAt;
}
