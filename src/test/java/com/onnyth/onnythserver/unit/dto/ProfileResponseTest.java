package com.onnyth.onnythserver.unit.dto;

import com.onnyth.onnythserver.dto.ProfileResponse;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ProfileResponse.fromUser() factory method.
 */
class ProfileResponseTest {

    @Test
    @DisplayName("fromUser() maps all fields correctly from a complete User")
    void fromUser_mapsAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        User user = User.builder()
                .id(id)
                .email("john@example.com")
                .username("johndoe")
                .fullName("John Doe")
                .profilePic("https://example.com/pic.jpg")
                .emailVerified(true)
                .profileComplete(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ProfileResponse response = ProfileResponse.fromUser(user);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.username()).isEqualTo("johndoe");
        assertThat(response.fullName()).isEqualTo("John Doe");
        assertThat(response.profilePic()).isEqualTo("https://example.com/pic.jpg");
        assertThat(response.emailVerified()).isTrue();
        assertThat(response.profileComplete()).isTrue();
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("fromUser() handles null optional fields gracefully")
    void fromUser_handlesNullOptionalFields() {
        User user = TestDataFactory.aUser().build(); // username, fullName, profilePic are null

        ProfileResponse response = ProfileResponse.fromUser(user);

        assertThat(response.username()).isNull();
        assertThat(response.fullName()).isNull();
        assertThat(response.profilePic()).isNull();
        assertThat(response.profileComplete()).isFalse();
        assertThat(response.emailVerified()).isTrue();
    }

    @Test
    @DisplayName("fromUser() preserves the exact user ID")
    void fromUser_preservesId() {
        UUID specificId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        User user = TestDataFactory.aUserWithId(specificId);

        ProfileResponse response = ProfileResponse.fromUser(user);

        assertThat(response.id()).isEqualTo(specificId);
    }
}
