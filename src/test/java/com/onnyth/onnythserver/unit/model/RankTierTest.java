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
 * Verifies fromScore(), nextTier(), and enum properties for the 5-tier system.
 */
class RankTierTest {

    @Nested
    @DisplayName("fromScore()")
    class FromScore {

        @ParameterizedTest
        @CsvSource({
                "0, BRONZE",
                "50, BRONZE",
                "99, BRONZE",
                "100, SILVER",
                "200, SILVER",
                "249, SILVER",
                "250, GOLD",
                "400, GOLD",
                "499, GOLD",
                "500, PLATINUM",
                "750, PLATINUM",
                "999, PLATINUM",
                "1000, ELITE",
                "5000, ELITE",
                "999999, ELITE"
        })
        @DisplayName("maps score to correct tier")
        void mapsScoreToCorrectTier(long score, RankTier expectedTier) {
            assertThat(RankTier.fromScore(score)).isEqualTo(expectedTier);
        }

        @Test
        @DisplayName("returns BRONZE for negative scores")
        void returnsBronzeForNegativeScore() {
            assertThat(RankTier.fromScore(-1)).isEqualTo(RankTier.BRONZE);
            assertThat(RankTier.fromScore(-100)).isEqualTo(RankTier.BRONZE);
        }

        @Test
        @DisplayName("each tier boundary returns the new tier")
        void eachBoundaryReturnsNewTier() {
            assertThat(RankTier.fromScore(0)).isEqualTo(RankTier.BRONZE);
            assertThat(RankTier.fromScore(100)).isEqualTo(RankTier.SILVER);
            assertThat(RankTier.fromScore(250)).isEqualTo(RankTier.GOLD);
            assertThat(RankTier.fromScore(500)).isEqualTo(RankTier.PLATINUM);
            assertThat(RankTier.fromScore(1000)).isEqualTo(RankTier.ELITE);
        }
    }

    @Nested
    @DisplayName("nextTier()")
    class NextTier {

        @Test
        @DisplayName("returns next tier for each non-max tier")
        void returnsNextTier() {
            assertThat(RankTier.BRONZE.nextTier()).isEqualTo(RankTier.SILVER);
            assertThat(RankTier.SILVER.nextTier()).isEqualTo(RankTier.GOLD);
            assertThat(RankTier.GOLD.nextTier()).isEqualTo(RankTier.PLATINUM);
            assertThat(RankTier.PLATINUM.nextTier()).isEqualTo(RankTier.ELITE);
        }

        @Test
        @DisplayName("returns null for ELITE (max tier)")
        void returnsNullForElite() {
            assertThat(RankTier.ELITE.nextTier()).isNull();
        }
    }

    @Nested
    @DisplayName("enum properties")
    class EnumProperties {

        @Test
        @DisplayName("BRONZE has correct properties")
        void bronzeProperties() {
            assertThat(RankTier.BRONZE.getMinScore()).isEqualTo(0);
            assertThat(RankTier.BRONZE.getDisplayName()).isEqualTo("Bronze");
            assertThat(RankTier.BRONZE.getBadgeEmoji()).isEqualTo("🥉");
        }

        @Test
        @DisplayName("ELITE has correct properties")
        void eliteProperties() {
            assertThat(RankTier.ELITE.getMinScore()).isEqualTo(1000);
            assertThat(RankTier.ELITE.getDisplayName()).isEqualTo("Elite");
            assertThat(RankTier.ELITE.getBadgeEmoji()).isEqualTo("👑");
        }

        @Test
        @DisplayName("all 5 tiers exist")
        void allTiersExist() {
            assertThat(RankTier.values()).hasSize(5);
        }
    }
}
