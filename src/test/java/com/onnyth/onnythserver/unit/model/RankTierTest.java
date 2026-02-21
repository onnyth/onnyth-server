package com.onnyth.onnythserver.unit.model;

import com.onnyth.onnythserver.models.RankTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RankTier enum.
 * Verifies fromScore() correctly maps scores to tiers.
 */
class RankTierTest {

    @Nested
    @DisplayName("fromScore()")
    class FromScore {

        @ParameterizedTest
        @CsvSource({
                "0, NOVICE",
                "50, NOVICE",
                "99, NOVICE",
                "100, APPRENTICE",
                "250, APPRENTICE",
                "499, APPRENTICE",
                "500, JOURNEYMAN",
                "1000, JOURNEYMAN",
                "1499, JOURNEYMAN",
                "1500, EXPERT",
                "3000, EXPERT",
                "4999, EXPERT",
                "5000, MASTER",
                "10000, MASTER",
                "14999, MASTER",
                "15000, GRANDMASTER",
                "30000, GRANDMASTER",
                "49999, GRANDMASTER",
                "50000, LEGEND",
                "100000, LEGEND",
                "999999, LEGEND"
        })
        @DisplayName("maps score to correct tier")
        void mapsScoreToCorrectTier(long score, RankTier expectedTier) {
            assertThat(RankTier.fromScore(score)).isEqualTo(expectedTier);
        }

        @Test
        @DisplayName("returns NOVICE for negative scores")
        void returnsNoviceForNegativeScore() {
            assertThat(RankTier.fromScore(-1)).isEqualTo(RankTier.NOVICE);
            assertThat(RankTier.fromScore(-100)).isEqualTo(RankTier.NOVICE);
        }

        @Test
        @DisplayName("each tier boundary returns the new tier")
        void eachBoundaryReturnsNewTier() {
            assertThat(RankTier.fromScore(0)).isEqualTo(RankTier.NOVICE);
            assertThat(RankTier.fromScore(100)).isEqualTo(RankTier.APPRENTICE);
            assertThat(RankTier.fromScore(500)).isEqualTo(RankTier.JOURNEYMAN);
            assertThat(RankTier.fromScore(1500)).isEqualTo(RankTier.EXPERT);
            assertThat(RankTier.fromScore(5000)).isEqualTo(RankTier.MASTER);
            assertThat(RankTier.fromScore(15000)).isEqualTo(RankTier.GRANDMASTER);
            assertThat(RankTier.fromScore(50000)).isEqualTo(RankTier.LEGEND);
        }
    }

    @Nested
    @DisplayName("enum properties")
    class EnumProperties {

        @Test
        @DisplayName("NOVICE has correct properties")
        void noviceProperties() {
            RankTier tier = RankTier.NOVICE;
            assertThat(tier.getMinScore()).isEqualTo(0);
            assertThat(tier.getDisplayName()).isEqualTo("Novice");
            assertThat(tier.getBadgeEmoji()).isEqualTo("🟤");
        }

        @Test
        @DisplayName("LEGEND has correct properties")
        void legendProperties() {
            RankTier tier = RankTier.LEGEND;
            assertThat(tier.getMinScore()).isEqualTo(50000);
            assertThat(tier.getDisplayName()).isEqualTo("Legend");
            assertThat(tier.getBadgeEmoji()).isEqualTo("⭐");
        }

        @Test
        @DisplayName("all 7 tiers exist")
        void allTiersExist() {
            assertThat(RankTier.values()).hasSize(7);
        }
    }
}
