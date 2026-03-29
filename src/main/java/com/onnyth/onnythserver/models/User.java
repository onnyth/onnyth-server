package com.onnyth.onnythserver.models;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Column(name = "username", unique = true, length = 20)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = Integer.MAX_VALUE)
    private String email;

    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "profile_pic", length = Integer.MAX_VALUE)
    private String profilePic;

    @Column(name = "email_verified", nullable = false, updatable = false)
    private Boolean emailVerified;

    @Builder.Default
    @Column(name = "profile_complete", nullable = false)
    private Boolean profileComplete = false;

    @Builder.Default
    @Column(name = "total_score", nullable = false)
    private Long totalScore = 0L;

    @Builder.Default
    @Column(name = "xp", nullable = false)
    private Long xp = 0L;

    @Builder.Default
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "rank_tier", nullable = false, length = 20)
    private RankTier rankTier = RankTier.BRONZE;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_displayed_achievements", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "achievement_id")
    private List<UUID> displayedAchievements = new ArrayList<>();

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Checks if all required profile fields are filled and updates profileComplete
     * status.
     */
    public void checkAndUpdateProfileCompletion() {
        this.profileComplete = username != null && !username.isBlank()
                && fullName != null && !fullName.isBlank()
                && profilePic != null && !profilePic.isBlank();
    }
}