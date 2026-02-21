package com.onnyth.onnythserver.unit.dto;

import com.onnyth.onnythserver.dto.ProfileCardResponse;
import com.onnyth.onnythserver.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ProfileCardResponse DTO.
 * Verifies the fromUser() factory method correctly maps User + score to the
 * DTO.
 */
class ProfileCardResponseTest {

    @Test
    @DisplayName("fromUser() maps user fields and calculates NOVICE rank for score 0")
    void fromUser_mapsFieldsAndNoviceRank() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("hero42")
                .fullName("Test Hero")
                .profilePic("https://cdn.example.com/pic.jpg")
                .email("hero@example.com")
                .build();

        ProfileCardResponse card = ProfileCardResponse.fromUser(user, 0);

        assertThat(card.userId()).isEqualTo(userId);
        assertThat(card.username()).isEqualTo("hero42");
        assertThat(card.fullName()).isEqualTo("Test Hero");
        assertThat(card.profilePic()).isEqualTo("https://cdn.example.com/pic.jpg");
        assertThat(card.totalScore()).isEqualTo(0);
        assertThat(card.rankTier()).isEqualTo("Novice");
        assertThat(card.rankBadgeUrl()).isEqualTo("🟤");
    }

    @Test
    @DisplayName("fromUser() calculates LEGEND rank for high score")
    void fromUser_legendRankForHighScore() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("legend_player")
                .fullName("Legend Player")
                .email("legend@example.com")
                .build();

        ProfileCardResponse card = ProfileCardResponse.fromUser(user, 75000);

        assertThat(card.totalScore()).isEqualTo(75000);
        assertThat(card.rankTier()).isEqualTo("Legend");
        assertThat(card.rankBadgeUrl()).isEqualTo("⭐");
    }

    @Test
    @DisplayName("fromUser() handles null optional fields gracefully")
    void fromUser_handlesNullFields() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("minimal@example.com")
                .build();

        ProfileCardResponse card = ProfileCardResponse.fromUser(user, 0);

        assertThat(card.username()).isNull();
        assertThat(card.fullName()).isNull();
        assertThat(card.profilePic()).isNull();
        assertThat(card.totalScore()).isEqualTo(0);
        assertThat(card.rankTier()).isEqualTo("Novice");
    }

    @Test
    @DisplayName("fromUser() calculates EXPERT rank correctly")
    void fromUser_expertRank() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("expert_user")
                .fullName("Expert User")
                .email("expert@example.com")
                .build();

        ProfileCardResponse card = ProfileCardResponse.fromUser(user, 2500);

        assertThat(card.totalScore()).isEqualTo(2500);
        assertThat(card.rankTier()).isEqualTo("Expert");
        assertThat(card.rankBadgeUrl()).isEqualTo("🟣");
    }
}
