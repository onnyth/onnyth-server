package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Composite PK for ProfileVote.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class ProfileVoteId implements Serializable {
    private UUID voterId;
    private UUID targetId;
}

/**
 * Tracks a user's upvote or downvote on another user's profile.
 * Each (voter, target) pair is unique — a user can only vote once per profile.
 * Changing the vote updates the existing record (upsert via service layer).
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profile_votes")
@IdClass(ProfileVoteId.class)
public class ProfileVote {

    @Id
    @Column(name = "voter_id", nullable = false)
    private UUID voterId;

    @Id
    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    /** true = upvote, false = downvote */
    @Column(name = "is_upvote", nullable = false)
    private Boolean isUpvote;

    @ColumnDefault("now()")
    @Builder.Default
    @Column(name = "voted_at", nullable = false)
    private Instant votedAt = Instant.now();
}
