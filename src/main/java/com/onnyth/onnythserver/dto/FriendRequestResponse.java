package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.FriendRequest;
import com.onnyth.onnythserver.models.User;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record FriendRequestResponse(
        UUID requestId,
        UUID senderId,
        String senderUsername,
        String senderFullName,
        String senderProfilePic,
        String senderRankTier,
        UUID receiverId,
        String receiverUsername,
        String status,
        Instant createdAt) {

    public static FriendRequestResponse fromRequest(FriendRequest request, User sender, User receiver) {
        return FriendRequestResponse.builder()
                .requestId(request.getId())
                .senderId(sender.getId())
                .senderUsername(sender.getUsername())
                .senderFullName(sender.getFullName())
                .senderProfilePic(sender.getProfilePic())
                .senderRankTier(sender.getRankTier() != null ? sender.getRankTier().getDisplayName() : null)
                .receiverId(receiver.getId())
                .receiverUsername(receiver.getUsername())
                .status(request.getStatus().name())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
