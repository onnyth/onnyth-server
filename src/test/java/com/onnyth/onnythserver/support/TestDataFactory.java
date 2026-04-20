package com.onnyth.onnythserver.support;

import com.onnyth.onnythserver.models.User;

import java.util.UUID;

/**
 * Factory for building test data objects.
 * Provides sensible defaults — override fields as needed per test.
 */
public class TestDataFactory {

    // ─── User ────────────────────────────────────────────────────────────────

    public static User.UserBuilder aUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                .emailVerified(true)
                .profileComplete(false);
    }

    public static User aCompleteUser() {
        return aUser()
                .username("testuser")
                .fullName("Test User")
                .profilePic("https://example.com/pic.jpg")
                .profileComplete(true)
                .build();
    }

    public static User aUserWithId(UUID id) {
        return aUser().id(id).build();
    }

    public static User aUserWithEmail(String email) {
        return aUser().email(email).build();
    }
}
