package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.User;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ProfileResponse(
        UUID id,
        String email,
        String username,
        String fullName,
        String profilePic,
        Boolean emailVerified,
        Boolean profileComplete,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Factory method to create ProfileResponse from User entity.
     */
    public static ProfileResponse fromUser(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .profilePic(user.getProfilePic())
                .emailVerified(user.getEmailVerified())
                .profileComplete(user.getProfileComplete())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

