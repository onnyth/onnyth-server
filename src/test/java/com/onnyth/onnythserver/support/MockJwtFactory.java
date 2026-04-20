package com.onnyth.onnythserver.support;

import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.UUID;

/**
 * Factory for creating mock Jwt objects for use in unit and web-layer tests.
 * For @WebMvcTest, use SecurityMockMvcRequestPostProcessors.jwt() instead.
 */
public class MockJwtFactory {

    /**
     * Creates a mock JWT with the given user ID as the subject.
     */
    public static Jwt withUserId(UUID userId) {
        return withUserId(userId, "test@example.com");
    }

    /**
     * Creates a mock JWT with the given user ID and email.
     */
    public static Jwt withUserId(UUID userId, String email) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .header("typ", "JWT")
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", "authenticated")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
    }

    /**
     * Creates a mock JWT for a random user.
     */
    public static Jwt random() {
        return withUserId(UUID.randomUUID());
    }
}
