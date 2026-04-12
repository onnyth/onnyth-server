package com.onnyth.onnythserver.models;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite primary key for ProfileLike entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProfileLikeId implements Serializable {
    private UUID likerId;
    private UUID likedId;
}
