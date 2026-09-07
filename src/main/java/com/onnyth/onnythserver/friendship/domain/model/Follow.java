package com.onnyth.onnythserver.friendship.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    private FollowId id;
    private UUID followerId;
    private UUID followingId;
    private Instant createdAt;
}
