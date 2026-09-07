package com.onnyth.onnythserver.friendship.adapter.out.persistence;

import com.onnyth.onnythserver.friendship.domain.model.FriendRequestStatus;
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

/**
 * Represents a friend request between two users.
 * Status transitions: PENDING → ACCEPTED or PENDING → REJECTED
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "friend_requests")
public class FriendRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FriendRequestStatus status = FriendRequestStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
