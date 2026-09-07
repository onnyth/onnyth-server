package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

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
public class ProfileLikeEntityId implements Serializable {
    @Serial
    private static final long serialVersionUID = -5343333523464095720L;

    @Column(name = "liker_id", nullable = false)
    private UUID likerId;

    @Column(name = "liked_id", nullable = false)
    private UUID likedId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        ProfileLikeEntityId entity = (ProfileLikeEntityId) o;
        return Objects.equals(this.likerId, entity.likerId)
                && Objects.equals(this.likedId, entity.likedId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(likerId, likedId);
    }
}
