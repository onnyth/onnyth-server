package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "caption", length = Integer.MAX_VALUE)
    private String caption;

    @Column(name = "media_url", nullable = false, length = Integer.MAX_VALUE)
    private String mediaUrl;

    @Column(name = "media_type", length = Integer.MAX_VALUE)
    private String mediaType;

    @Column(name = "created_at")
    private Instant createdAt;

}