package com.onnyth.onnythserver.user.adapter.out.persistence;

import com.onnyth.onnythserver.models.CosmeticItem;
import com.onnyth.onnythserver.ranking.domain.model.RankTier;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class UserEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", unique = true, length = 20)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = Integer.MAX_VALUE)
    private String email;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Builder.Default
    @Column(name = "profile_type", length = 10)
    private String profileType = "personal";

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
    @Column(name = "onnyth_coins", nullable = false)
    private Integer onnythCoins = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "rank_tier", nullable = false, length = 20)
    private RankTier rankTier = RankTier.BRONZE;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_displayed_achievements", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "achievement_id")
    private List<UUID> displayedAchievements = new ArrayList<>();

    @Column(name = "world_rank")
    private Integer worldRank;

    @Column(name = "country_rank")
    private Integer countryRank;

    @Column(name = "country", length = 2)
    private String country;

    @Builder.Default
    @Column(name = "vote_score", nullable = false)
    private Integer voteScore = 0;

    @Column(name = "active_background_color", length = 7)
    private String activeBackgroundColor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_frame_cosmetic_id")
    private CosmeticItem activeFrameCosmetic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_background_cosmetic_id")
    private CosmeticItem activeBackgroundCosmetic;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
