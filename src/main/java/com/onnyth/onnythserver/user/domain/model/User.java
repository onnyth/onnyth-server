package com.onnyth.onnythserver.user.domain.model;

import com.onnyth.onnythserver.ranking.domain.model.RankTier;
import com.onnyth.onnythserver.store.domain.model.CosmeticItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core domain representation of a user, decoupled from persistence concerns.
 * Kept mutable (getters/setters) to match the widespread mutate-then-save
 * pattern used throughout the existing service layer.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phone;

    @Builder.Default
    private String profileType = "personal";

    private String profilePic;
    private Boolean emailVerified;

    @Builder.Default
    private Boolean profileComplete = false;

    @Builder.Default
    private Long totalScore = 0L;

    @Builder.Default
    private Long xp = 0L;

    @Builder.Default
    private Integer level = 1;

    @Builder.Default
    private Integer onnythCoins = 0;

    @Builder.Default
    private RankTier rankTier = RankTier.BRONZE;

    @Builder.Default
    private List<UUID> displayedAchievements = new ArrayList<>();

    // ─── Ranking (periodically computed by RankingService) ────────────────────

    /** Global rank among all users by totalScore. Null until first ranking job runs. */
    private Integer worldRank;

    /** Rank within the user's country. Null until country is set and ranking runs. */
    private Integer countryRank;

    /** ISO 3166-1 alpha-2 country code (e.g. "AE", "US", "IN"). */
    private String country;

    // ─── Social Votes ─────────────────────────────────────────────────────────

    /** Net vote score: upvotes minus downvotes from other users. */
    @Builder.Default
    private Integer voteScore = 0;

    // ─── Active Cosmetics ─────────────────────────────────────────────────────

    /** Solid hex background color (e.g. "#22162B"). Used when no background cosmetic is equipped. */
    private String activeBackgroundColor;

    /** Equipped frame cosmetic (decorative ring around profile pic). */
    private CosmeticItem activeFrameCosmetic;

    /** Equipped background cosmetic (full-page image/texture). Overrides activeBackgroundColor when set. */
    private CosmeticItem activeBackgroundCosmetic;

    private Instant createdAt;
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
