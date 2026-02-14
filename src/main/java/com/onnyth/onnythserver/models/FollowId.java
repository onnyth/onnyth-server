package com.onnyth.onnythserver.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class FollowId implements Serializable {
    @Serial
    private static final long serialVersionUID = -2063795450169840265L;
    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    @Column(name = "following_id", nullable = false)
    private UUID followingId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        FollowId entity = (FollowId) o;
        return Objects.equals(this.followingId, entity.followingId) &&
                Objects.equals(this.followerId, entity.followerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(followingId, followerId);
    }

}