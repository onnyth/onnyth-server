package com.onnyth.onnythserver.friendship.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a friend request between two users.
 * Status transitions: PENDING → ACCEPTED or PENDING → REJECTED
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequest {

    private UUID id;
    private UUID senderId;
    private UUID receiverId;

    @Builder.Default
    private FriendRequestStatus status = FriendRequestStatus.PENDING;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}
