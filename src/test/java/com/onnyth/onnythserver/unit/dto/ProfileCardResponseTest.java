package com.onnyth.onnythserver.unit.dto;

import com.onnyth.onnythserver.dto.ProfileCardResponse;
import com.onnyth.onnythserver.models.RankTier;
import com.onnyth.onnythserver.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ProfileCardResponse DTO.
 * Verifies the fromUser() factory method correctly maps User entity to DTO.
 */
class ProfileCardResponseTest {

    @Test
    @DisplayName("fromUser() maps user fields and BRONZE rank for score 0")
    void fromUser_mapsFieldsAndBronzeRank() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("hero42")
                .fullName("Test Hero")
                .profilePic("https://cdn.example.com/pic.jpg")
                .email("hero@example.com")
                .totalScore(0L)
                .rankTier(RankTier.BRONZE)
                .build();

        ProfileCardResponse card = ProfileCardResponse.fromUser(user);

        assertThat(card.userId()).isEqualTo(userId);
        assertThat(card.username()).isEqualTo("hero42");
        assertThat(card.fullName()).isEqualTo("Test Hero");
        assertThat(card.profilePic()).isEqualTo("https://cdn.example.com/pic.jpg");
        assertThat(card.totalScore()).isEqualTo(0);
        assertThat(card.rankTier()).isEqualTo("Bronze");
        assertThat(card.rankBadgeUrl()).isEqualTo("🥉");
    }

    @Test
    @DisplayName("fromUser() shows ELITE rank for high score")
    void fromUser_eliteRankForHighScore() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("elite_player")
                .fullName("Elite Player")
                .email("elite@example.com")
                .totalScore(5000L)
                .rankTier(RankTier.ELITE)
                .build();

        ProfileCardResponse card = ProfileCardResponse.fromUser(user);

        assertThat(card.totalScore()).isEqualTo(5000);
        assertThat(card.rankTier()).isEqualTo("Elite");
        assertThat(card.rankBadgeUrl()).isEqualTo("👑");
    }

    @Test
    @DisplayName("fromUser() handles null optional fields gracefully")
    void fromUser_handlesNullFields() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("minimal@example.com")
                .totalScore(0L)
                .rankTier(RankTier.BRONZE)
                .build();

        ProfileCardResponse card = ProfileCardResponse.fromUser(user);

        assertThat(card.username()).isNull();
        assertThat(card.fullName()).isNull();
        assertThat(card.profilePic()).isNull();
        assertThat(card.totalScore()).isEqualTo(0);
        assertThat(card.rankTier()).isEqualTo("Bronze");
    }

    @Test
    @DisplayName("fromUser() shows PLATINUM rank correctly")
    void fromUser_platinumRank() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("platinum_user")
                .fullName("Platinum User")
                .email("platinum@example.com")
                .totalScore(750L)
                .rankTier(RankTier.PLATINUM)
                .build();

        ProfileCardResponse card = ProfileCardResponse.fromUser(user);

        assertThat(card.totalScore()).isEqualTo(750);
        assertThat(card.rankTier()).isEqualTo("Platinum");
        assertThat(card.rankBadgeUrl()).isEqualTo("💎");
    }
}
